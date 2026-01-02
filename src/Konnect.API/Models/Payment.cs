using Konnect.Shared.Enums;

namespace Konnect.API.Models;

public class Payment
{
    public Guid Id { get; set; }
    public Guid DealId { get; set; }
    public decimal Amount { get; set; }
    public string? EsewaRefId { get; set; }
    public PaymentStatus Status { get; set; } = PaymentStatus.Pending;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? PaidAt { get; set; }

    public Deal Deal { get; set; } = null!;
}
