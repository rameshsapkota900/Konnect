using Konnect.Shared.Enums;

namespace Konnect.API.Models;

public class Deal
{
    public Guid Id { get; set; }
    public Guid CampaignId { get; set; }
    public Guid CreatorId { get; set; }
    public Guid BusinessId { get; set; }
    public DealStatus Status { get; set; } = DealStatus.Pending;
    public decimal AgreedPrice { get; set; }
    public string? Notes { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? CompletedAt { get; set; }

    public Campaign Campaign { get; set; } = null!;
    public User Creator { get; set; } = null!;
    public User Business { get; set; } = null!;
}
