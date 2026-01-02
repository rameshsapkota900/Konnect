using Konnect.Shared.Enums;

namespace Konnect.Shared.DTOs;

public class DealDto
{
    public Guid Id { get; set; }
    public Guid CampaignId { get; set; }
    public Guid CreatorId { get; set; }
    public Guid BusinessId { get; set; }
    public DealStatus Status { get; set; }
    public decimal AgreedPrice { get; set; }
    public string? Notes { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
    public CampaignDto? Campaign { get; set; }
    public UserDto? Creator { get; set; }
    public UserDto? Business { get; set; }
}

public class CreateDealDto
{
    public Guid CampaignId { get; set; }
    public Guid CreatorId { get; set; }
    public decimal AgreedPrice { get; set; }
    public string? Notes { get; set; }
}

public class UpdateDealStatusDto
{
    public DealStatus Status { get; set; }
    public string? Notes { get; set; }
}
