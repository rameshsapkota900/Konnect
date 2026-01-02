using Konnect.Shared.Enums;

namespace Konnect.API.Models;

public class Campaign
{
    public Guid Id { get; set; }
    public Guid BusinessId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public decimal Budget { get; set; }
    public DateTime? Deadline { get; set; }
    public Niche Niche { get; set; }
    public CampaignStatus Status { get; set; } = CampaignStatus.Open;
    public List<string> Deliverables { get; set; } = new();
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }

    public User Business { get; set; } = null!;
}
