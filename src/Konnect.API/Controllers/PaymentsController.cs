using Konnect.API.Services;
using Konnect.Shared.DTOs;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Konnect.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PaymentsController : ControllerBase
{
    private readonly IEsewaService _esewaService;

    public PaymentsController(IEsewaService esewaService)
    {
        _esewaService = esewaService;
    }

    /// <summary>
    /// Initiate eSewa payment for a deal
    /// </summary>
    [HttpPost("initiate")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<EsewaPaymentInitResponse>>> InitiatePayment([FromBody] InitiatePaymentDto dto)
    {
        try
        {
            var result = await _esewaService.InitiatePaymentAsync(dto.DealId, dto.Amount);
            return Ok(ApiResponse<EsewaPaymentInitResponse>.Ok(result, "Payment initiated"));
        }
        catch (Exception ex)
        {
            return BadRequest(ApiResponse<EsewaPaymentInitResponse>.Fail(ex.Message));
        }
    }

    /// <summary>
    /// eSewa callback after payment (success/failure)
    /// </summary>
    [HttpGet("esewa/callback")]
    public async Task<IActionResult> EsewaCallback([FromQuery] string data)
    {
        var callback = new EsewaCallbackDto { Data = data };
        var payment = await _esewaService.ProcessCallbackAsync(callback);

        if (payment == null)
        {
            return Redirect("/payment/failed");
        }

        if (payment.Status == Konnect.Shared.Enums.PaymentStatus.Escrow)
        {
            return Redirect($"/payment/success?dealId={payment.DealId}");
        }

        return Redirect("/payment/failed");
    }

    /// <summary>
    /// Verify payment status
    /// </summary>
    [HttpGet("verify/{transactionCode}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<bool>>> VerifyPayment(string transactionCode, [FromQuery] decimal amount)
    {
        var isValid = await _esewaService.VerifyPaymentAsync(transactionCode, amount);
        return Ok(ApiResponse<bool>.Ok(isValid));
    }
}

public class InitiatePaymentDto
{
    public Guid DealId { get; set; }
    public decimal Amount { get; set; }
}
