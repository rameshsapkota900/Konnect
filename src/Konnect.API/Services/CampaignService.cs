using Konnect.API.Data;
using Konnect.API.Models;
using Konnect.Shared.DTOs;
using Konnect.Shared.Enums;
using Microsoft.EntityFrameworkCore;

namespace Konnect.API.Services;

public class CampaignService : ICampaignService
{
    private readonly AppDbContext _db;

    public CampaignService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<CampaignDto?> GetByIdAsync(Guid id)
    {
        var campaign = await _db.Campaigns
            .Include(c => c.Business)
            .FirstOrDefaultAsync(c => c.Id == id);
        return campaign == null ? null : MapToDto(campaign);
    }

    public async Task<PagedResponse<CampaignDto>> GetAllAsync(CampaignFilterDto filter)
    {
        var query = _db.Campaigns
            .Include(c => c.Business)
            .Where(c => c.Status == CampaignStatus.Open)
            .AsQueryable();

        if (filter.Niche.HasValue)
            query = query.Where(c => c.Niche == filter.Niche.Value);
        if (filter.MinBudget.HasValue)
            query = query.Where(c => c.Budget >= filter.MinBudget.Value);
        if (filter.MaxBudget.HasValue)
            query = query.Where(c => c.Budget <= filter.MaxBudget.Value);

        var totalCount = await query.CountAsync();
        var items = await query
            .OrderByDescending(c => c.CreatedAt)
            .Skip((filter.Page - 1) * filter.PageSize)
            .Take(filter.PageSize)
            .ToListAsync();

        return new PagedResponse<CampaignDto>
        {
            Items = items.Select(MapToDto).ToList(),
            TotalCount = totalCount,
            Page = filter.Page,
            PageSize = filter.PageSize
        };
    }

    public async Task<List<CampaignDto>> GetByBusinessIdAsync(Guid businessId)
    {
        var campaigns = await _db.Campaigns
            .Include(c => c.Business)
            .Where(c => c.BusinessId == businessId)
            .OrderByDescending(c => c.CreatedAt)
            .ToListAsync();

        return campaigns.Select(MapToDto).ToList();
    }

    public async Task<CampaignDto> CreateAsync(Guid businessId, CreateCampaignDto dto)
    {
        var campaign = new Campaign
        {
            Id = Guid.NewGuid(),
            BusinessId = businessId,
            Title = dto.Title,
            Description = dto.Description,
            Budget = dto.Budget,
            Deadline = dto.Deadline,
            Niche = dto.Niche,
            Deliverables = dto.Deliverables,
            Status = CampaignStatus.Open
        };

        _db.Campaigns.Add(campaign);
        await _db.SaveChangesAsync();

        return await GetByIdAsync(campaign.Id) ?? MapToDto(campaign);
    }

    public async Task<CampaignDto?> UpdateAsync(Guid id, UpdateCampaignDto dto)
    {
        var campaign = await _db.Campaigns.FindAsync(id);
        if (campaign == null) return null;

        if (dto.Title != null) campaign.Title = dto.Title;
        if (dto.Description != null) campaign.Description = dto.Description;
        if (dto.Budget.HasValue) campaign.Budget = dto.Budget.Value;
        if (dto.Deadline.HasValue) campaign.Deadline = dto.Deadline;
        if (dto.Niche.HasValue) campaign.Niche = dto.Niche.Value;
        if (dto.Status.HasValue) campaign.Status = dto.Status.Value;
        if (dto.Deliverables != null) campaign.Deliverables = dto.Deliverables;
        campaign.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return await GetByIdAsync(id);
    }

    private static CampaignDto MapToDto(Campaign c) => new()
    {
        Id = c.Id,
        BusinessId = c.BusinessId,
        Title = c.Title,
        Description = c.Description,
        Budget = c.Budget,
        Deadline = c.Deadline,
        Niche = c.Niche,
        Status = c.Status,
        Deliverables = c.Deliverables,
        CreatedAt = c.CreatedAt,
        Business = c.Business == null ? null : new UserDto
        {
            Id = c.Business.Id,
            FullName = c.Business.FullName,
            Location = c.Business.Location
        }
    };
}
