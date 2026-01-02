using Konnect.Shared.Enums;

namespace Konnect.Shared.DTOs;

public class CreatorProfileDto
{
    public Guid Id { get; set; }
    public Guid UserId { get; set; }
    public string? Bio { get; set; }
    public string? InstagramUrl { get; set; }
    public string? TikTokUrl { get; set; }
    public string? YouTubeUrl { get; set; }
    public int FollowersCount { get; set; }
    public decimal? EngagementRate { get; set; }
    public decimal? HourlyRate { get; set; }
    public List<Niche> Niches { get; set; } = new();
    public List<string> PortfolioUrls { get; set; } = new();
    public bool IsVerified { get; set; }
    public UserDto? User { get; set; }
}

public class CreateCreatorProfileDto
{
    public string? Bio { get; set; }
    public string? InstagramUrl { get; set; }
    public string? TikTokUrl { get; set; }
    public string? YouTubeUrl { get; set; }
    public int FollowersCount { get; set; }
    public decimal? HourlyRate { get; set; }
    public List<Niche> Niches { get; set; } = new();
}

public class UpdateCreatorProfileDto
{
    public string? Bio { get; set; }
    public string? InstagramUrl { get; set; }
    public string? TikTokUrl { get; set; }
    public string? YouTubeUrl { get; set; }
    public int? FollowersCount { get; set; }
    public decimal? HourlyRate { get; set; }
    public List<Niche>? Niches { get; set; }
    public List<string>? PortfolioUrls { get; set; }
}
