using System.Net.Http.Json;

namespace Konnect.Mobile.Services;

public interface IEsewaPaymentService
{
    Task<EsewaPaymentResult> InitiatePaymentAsync(Guid dealId, decimal amount);
}

public class EsewaPaymentService : IEsewaPaymentService
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IFirebaseAuthService _authService;

    public EsewaPaymentService(IHttpClientFactory httpClientFactory, IFirebaseAuthService authService)
    {
        _httpClientFactory = httpClientFactory;
        _authService = authService;
    }

    public async Task<EsewaPaymentResult> InitiatePaymentAsync(Guid dealId, decimal amount)
    {
        var client = _httpClientFactory.CreateClient("KonnectAPI");
        var token = await _authService.GetCurrentUserTokenAsync();
        
        if (!string.IsNullOrEmpty(token))
        {
            client.DefaultRequestHeaders.Authorization = 
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);
        }

        var response = await client.PostAsJsonAsync("payments/initiate", new
        {
            DealId = dealId,
            Amount = amount
        });

        if (response.IsSuccessStatusCode)
        {
            var result = await response.Content.ReadFromJsonAsync<ApiResponseWrapper<EsewaInitResponse>>();
            if (result?.Success == true && result.Data != null)
            {
                return new EsewaPaymentResult
                {
                    Success = true,
                    PaymentUrl = BuildEsewaFormUrl(result.Data)
                };
            }
        }

        return new EsewaPaymentResult { Success = false, ErrorMessage = "Failed to initiate payment" };
    }

    private static string BuildEsewaFormUrl(EsewaInitResponse data)
    {
        // Build the eSewa payment form URL with all required parameters
        var baseUrl = data.EsewaUrl;
        var queryParams = new Dictionary<string, string>
        {
            ["amount"] = data.Amount.ToString(),
            ["tax_amount"] = data.TaxAmount.ToString(),
            ["total_amount"] = data.TotalAmount.ToString(),
            ["transaction_uuid"] = data.TransactionUuid,
            ["product_code"] = data.ProductCode,
            ["product_service_charge"] = "0",
            ["product_delivery_charge"] = "0",
            ["success_url"] = $"{AppConfig.ApiBaseUrl}payments/esewa/callback",
            ["failure_url"] = $"{AppConfig.ApiBaseUrl}payments/esewa/callback",
            ["signed_field_names"] = data.SignedFieldNames,
            ["signature"] = data.Signature
        };

        var queryString = string.Join("&", queryParams.Select(kv => $"{kv.Key}={Uri.EscapeDataString(kv.Value)}"));
        return $"{baseUrl}?{queryString}";
    }
}

public class EsewaPaymentResult
{
    public bool Success { get; set; }
    public string? PaymentUrl { get; set; }
    public string? ErrorMessage { get; set; }
}

public class EsewaInitResponse
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

public class ApiResponseWrapper<T>
{
    public bool Success { get; set; }
    public string? Message { get; set; }
    public T? Data { get; set; }
}
