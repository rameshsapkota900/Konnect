using System.Security.Cryptography;
using System.Text;
using Konnect.API.Data;
using Konnect.API.Models;
using Konnect.Shared.Enums;
using Microsoft.EntityFrameworkCore;

namespace Konnect.API.Services;

public interface IEsewaService
{
    Task<EsewaPaymentInitResponse> InitiatePaymentAsync(Guid dealId, decimal amount);
    Task<bool> VerifyPaymentAsync(string transactionCode, decimal amount);
    Task<Payment?> ProcessCallbackAsync(EsewaCallbackDto callback);
}

public class EsewaService : IEsewaService
{
    private readonly IConfiguration _config;
    private readonly AppDbContext _db;
    private readonly HttpClient _httpClient;

    public EsewaService(IConfiguration config, AppDbContext db, IHttpClientFactory httpClientFactory)
    {
        _config = config;
        _db = db;
        _httpClient = httpClientFactory.CreateClient();
    }

    public async Task<EsewaPaymentInitResponse> InitiatePaymentAsync(Guid dealId, decimal amount)
    {
        var deal = await _db.Deals.FindAsync(dealId);
        if (deal == null)
            throw new Exception("Deal not found");

        // Create payment record
        var payment = new Payment
        {
            Id = Guid.NewGuid(),
            DealId = dealId,
            Amount = amount,
            Status = PaymentStatus.Pending
        };

        _db.Payments.Add(payment);
        await _db.SaveChangesAsync();

        var isTestMode = _config.GetValue<bool>("Esewa:TestMode");
        var baseUrl = isTestMode 
            ? _config["Esewa:BaseUrl"] 
            : _config["Esewa:ProductionUrl"];
        var merchantId = _config["Esewa:MerchantId"];
        var secretKey = _config["Esewa:SecretKey"];

        // Generate signature for ePay v2
        var message = $"total_amount={amount},transaction_uuid={payment.Id},product_code={merchantId}";
        var signature = GenerateSignature(message, secretKey!);

        return new EsewaPaymentInitResponse
        {
            PaymentId = payment.Id,
            EsewaUrl = $"{baseUrl}/api/epay/main/v2/form",
            Amount = amount,
            TaxAmount = 0,
            TotalAmount = amount,
            TransactionUuid = payment.Id.ToString(),
            ProductCode = merchantId!,
            Signature = signature,
            SignedFieldNames = "total_amount,transaction_uuid,product_code"
        };
    }

    public async Task<bool> VerifyPaymentAsync(string transactionCode, decimal amount)
    {
        var isTestMode = _config.GetValue<bool>("Esewa:TestMode");
        var baseUrl = isTestMode 
            ? _config["Esewa:BaseUrl"] 
            : _config["Esewa:ProductionUrl"];
        var merchantId = _config["Esewa:MerchantId"];

        var verifyUrl = $"{baseUrl}/api/epay/transaction/status/?product_code={merchantId}&total_amount={amount}&transaction_uuid={transactionCode}";

        try
        {
            var response = await _httpClient.GetAsync(verifyUrl);
            if (response.IsSuccessStatusCode)
            {
                var content = await response.Content.ReadAsStringAsync();
                return content.Contains("\"status\":\"COMPLETE\"");
            }
        }
        catch
        {
            // Log error
        }

        return false;
    }

    public async Task<Payment?> ProcessCallbackAsync(EsewaCallbackDto callback)
    {
        // Decode base64 response
        var decodedData = DecodeBase64Response(callback.Data);
        
        if (!Guid.TryParse(decodedData.TransactionUuid, out var paymentId))
            return null;

        var payment = await _db.Payments
            .Include(p => p.Deal)
            .FirstOrDefaultAsync(p => p.Id == paymentId);

        if (payment == null) return null;

        // Verify the payment
        var isValid = await VerifyPaymentAsync(decodedData.TransactionCode, payment.Amount);

        if (isValid && decodedData.Status == "COMPLETE")
        {
            payment.Status = PaymentStatus.Escrow;
            payment.EsewaRefId = decodedData.TransactionCode;
            payment.PaidAt = DateTime.UtcNow;

            // Update deal status
            payment.Deal.Status = DealStatus.InProgress;

            await _db.SaveChangesAsync();
        }
        else
        {
            payment.Status = PaymentStatus.Failed;
            await _db.SaveChangesAsync();
        }

        return payment;
    }

    private static string GenerateSignature(string message, string secretKey)
    {
        using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secretKey));
        var hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(message));
        return Convert.ToBase64String(hash);
    }

    private static EsewaDecodedResponse DecodeBase64Response(string base64Data)
    {
        var jsonBytes = Convert.FromBase64String(base64Data);
        var json = Encoding.UTF8.GetString(jsonBytes);
        return System.Text.Json.JsonSerializer.Deserialize<EsewaDecodedResponse>(json) 
            ?? new EsewaDecodedResponse();
    }
}

public class EsewaPaymentInitResponse
{
    public Guid PaymentId { get; set; }
    public string EsewaUrl { get; set; } = string.Empty;
    public decimal Amount { get; set; }
    public decimal TaxAmount { get; set; }
    public decimal TotalAmount { get; set; }
    public string TransactionUuid { get; set; } = string.Empty;
    public string ProductCode { get; set; } = string.Empty;
    public string Signature { get; set; } = string.Empty;
    public string SignedFieldNames { get; set; } = string.Empty;
}

public class EsewaCallbackDto
{
    public string Data { get; set; } = string.Empty;
}

public class EsewaDecodedResponse
{
    public string TransactionCode { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public decimal TotalAmount { get; set; }
    public string TransactionUuid { get; set; } = string.Empty;
    public string ProductCode { get; set; } = string.Empty;
    public string SignedFieldNames { get; set; } = string.Empty;
    public string Signature { get; set; } = string.Empty;
}
