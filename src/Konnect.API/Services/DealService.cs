using Konnect.API.Data;
using Konnect.API.Models;
using Konnect.Shared.DTOs;
using Konnect.Shared.Enums;
using Microsoft.EntityFrameworkCore;

namespace Konnect.API.Services;

public class DealService : IDealService
{
    private readonly AppDbContext _db;

    public DealService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<DealDto?> GetByIdAsync(Guid id)
    {
        var deal = await _db.Deals
            .Include(d => d.Campaign)
            .Include(d => d.Creator)
            .Include(d => d.Business)
            .FirstOrDefaultAsync(d => d.Id == id);
        return deal == null ? null : MapToDto(deal);
    }

    public async Task<List<DealDto>> GetByUserIdAsync(Guid userId)
    {
        var deals = await _db.Deals
            .Include(d => d.Campaign)
            .Include(d => d.Creator)
            .Include(d => d.Business)
            .Where(d => d.CreatorId == userId || d.BusinessId == userId)
            .OrderByDescending(d => d.CreatedAt)
            .ToListAsync();

        return deals.Select(MapToDto).ToList();
    }

    public async Task<DealDto> CreateAsync(Guid businessId, CreateDealDto dto)
    {
        var campaign = await _db.Campaigns.FindAsync(dto.CampaignId);
        
        var deal = new Deal
        {
            Id = Guid.NewGuid(),
            CampaignId = dto.CampaignId,
            CreatorId = dto.CreatorId,
            BusinessId = businessId,
            AgreedPrice = dto.AgreedPrice,
            Notes = dto.Notes,
            Status = DealStatus.Pending
        };

        _db.Deals.Add(deal);
        await _db.SaveChangesAsync();

        return await GetByIdAsync(deal.Id) ?? throw new Exception("Failed to create deal");
    }

    public async Task<DealDto?> UpdateStatusAsync(Guid id, UpdateDealStatusDto dto)
    {
        var deal = await _db.Deals.FindAsync(id);
        if (deal == null) return null;

        deal.Status = dto.Status;
        if (dto.Notes != null) deal.Notes = dto.Notes;
        if (dto.Status == DealStatus.Completed) deal.CompletedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return await GetByIdAsync(id);
    }

    private static DealDto MapToDto(Deal d) => new()
    {
        Id = d.Id,
        CampaignId = d.CampaignId,
        CreatorId = d.CreatorId,
        BusinessId = d.BusinessId,
        Status = d.Status,
        AgreedPrice = d.AgreedPrice,
        Notes = d.Notes,
        CreatedAt = d.CreatedAt,
        CompletedAt = d.CompletedAt,
        Campaign = d.Campaign == null ? null : new CampaignDto
        {
            Id = d.Campaign.Id,
            Title = d.Campaign.Title,
            Budget = d.Campaign.Budget
        },
        Creator = d.Creator == null ? null : new UserDto
        {
            Id = d.Creator.Id,
            FullName = d.Creator.FullName
        },
        Business = d.Business == null ? null : new UserDto
        {
            Id = d.Business.Id,
            FullName = d.Business.FullName
        }
    };
}
