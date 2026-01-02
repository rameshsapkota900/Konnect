using Konnect.Shared.Enums;

namespace Konnect.API.Models;

public class CreatorProfile
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
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UpdatedAt { get; set; }

    public User User { get; set; } = null!;
}
