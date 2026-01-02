using Konnect.API.Data;
using Konnect.API.Models;
using Konnect.Shared.DTOs;
using Microsoft.EntityFrameworkCore;

namespace Konnect.API.Services;

public class CreatorService : ICreatorService
{
    private readonly AppDbContext _db;

    public CreatorService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<CreatorProfileDto?> GetByUserIdAsync(Guid userId)
    {
        var profile = await _db.CreatorProfiles
            .Include(p => p.User)
            .FirstOrDefaultAsync(p => p.UserId == userId);
        return profile == null ? null : MapToDto(profile);
    }

    public async Task<PagedResponse<CreatorProfileDto>> SearchAsync(CreatorFilterDto filter)
    {
        var query = _db.CreatorProfiles
            .Include(p => p.User)
            .AsQueryable();

        if (filter.Niche.HasValue)
            query = query.Where(p => p.Niches.Contains(filter.Niche.Value));
        if (filter.MinFollowers.HasValue)
            query = query.Where(p => p.FollowersCount >= filter.MinFollowers.Value);
        if (filter.MaxFollowers.HasValue)
            query = query.Where(p => p.FollowersCount <= filter.MaxFollowers.Value);
        if (filter.MinRate.HasValue)
            query = query.Where(p => p.HourlyRate >= filter.MinRate.Value);
        if (filter.MaxRate.HasValue)
            query = query.Where(p => p.HourlyRate <= filter.MaxRate.Value);
        if (!string.IsNullOrEmpty(filter.Location))
            query = query.Where(p => p.User.Location != null && 
                p.User.Location.ToLower().Contains(filter.Location.ToLower()));

        var totalCount = await query.CountAsync();
        var items = await query
            .OrderByDescending(p => p.FollowersCount)
            .Skip((filter.Page - 1) * filter.PageSize)
            .Take(filter.PageSize)
            .ToListAsync();

        return new PagedResponse<CreatorProfileDto>
        {
            Items = items.Select(MapToDto).ToList(),
            TotalCount = totalCount,
            Page = filter.Page,
            PageSize = filter.PageSize
        };
    }

    public async Task<CreatorProfileDto> CreateAsync(Guid userId, CreateCreatorProfileDto dto)
    {
        var profile = new CreatorProfile
        {
            Id = Guid.NewGuid(),
            UserId = userId,
            Bio = dto.Bio,
            InstagramUrl = dto.InstagramUrl,
            TikTokUrl = dto.TikTokUrl,
            YouTubeUrl = dto.YouTubeUrl,
            FollowersCount = dto.FollowersCount,
            HourlyRate = dto.HourlyRate,
            Niches = dto.Niches
        };

        _db.CreatorProfiles.Add(profile);
        await _db.SaveChangesAsync();

        return await GetByUserIdAsync(userId) ?? MapToDto(profile);
    }

    public async Task<CreatorProfileDto?> UpdateAsync(Guid userId, UpdateCreatorProfileDto dto)
    {
        var profile = await _db.CreatorProfiles.FirstOrDefaultAsync(p => p.UserId == userId);
        if (profile == null) return null;

        if (dto.Bio != null) profile.Bio = dto.Bio;
        if (dto.InstagramUrl != null) profile.InstagramUrl = dto.InstagramUrl;
        if (dto.TikTokUrl != null) profile.TikTokUrl = dto.TikTokUrl;
        if (dto.YouTubeUrl != null) profile.YouTubeUrl = dto.YouTubeUrl;
        if (dto.FollowersCount.HasValue) profile.FollowersCount = dto.FollowersCount.Value;
        if (dto.HourlyRate.HasValue) profile.HourlyRate = dto.HourlyRate;
        if (dto.Niches != null) profile.Niches = dto.Niches;
        if (dto.PortfolioUrls != null) profile.PortfolioUrls = dto.PortfolioUrls;
        profile.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return await GetByUserIdAsync(userId);
    }

    private static CreatorProfileDto MapToDto(CreatorProfile p) => new()
    {
        Id = p.Id,
        UserId = p.UserId,
        Bio = p.Bio,
        InstagramUrl = p.InstagramUrl,
        TikTokUrl = p.TikTokUrl,
        YouTubeUrl = p.YouTubeUrl,
        FollowersCount = p.FollowersCount,
        EngagementRate = p.EngagementRate,
        HourlyRate = p.HourlyRate,
        Niches = p.Niches,
        PortfolioUrls = p.PortfolioUrls,
        IsVerified = p.IsVerified,
        User = p.User == null ? null : new UserDto
        {
            Id = p.User.Id,
            FullName = p.User.FullName,
            ProfileImageUrl = p.User.ProfileImageUrl,
            Location = p.User.Location
        }
    };
}
