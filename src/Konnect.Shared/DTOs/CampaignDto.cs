using Konnect.Shared.Enums;

namespace Konnect.Shared.DTOs;

public class CampaignDto
{
    public Guid Id { get; set; }
    public Guid BusinessId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public decimal Budget { get; set; }
    public DateTime? Deadline { get; set; }
    public Niche Niche { get; set; }
    public CampaignStatus Status { get; set; }
    public List<string> Deliverables { get; set; } = new();
    public DateTime CreatedAt { get; set; }
    public UserDto? Business { get; set; }
}

public class CreateCampaignDto
{
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public decimal Budget { get; set; }
    public DateTime? Deadline { get; set; }
    public Niche Niche { get; set; }
    public List<string> Deliverables { get; set; } = new();
}

public class UpdateCampaignDto
{
    public string? Title { get; set; }
    public string? Description { get; set; }
    public decimal? Budget { get; set; }
    public DateTime? Deadline { get; set; }
    public Niche? Niche { get; set; }
    public CampaignStatus? Status { get; set; }
    public List<string>? Deliverables { get; set; }
}

public class CampaignFilterDto
{
    public Niche? Niche { get; set; }
    public decimal? MinBudget { get; set; }
    public decimal? MaxBudget { get; set; }
    public string? Location { get; set; }
    public int Page { get; set; } = 1;
    public int PageSize { get; set; } = 20;
}
