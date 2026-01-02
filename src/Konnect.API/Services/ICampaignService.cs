using Konnect.Shared.DTOs;

namespace Konnect.API.Services;

public interface ICampaignService
{
    Task<CampaignDto?> GetByIdAsync(Guid id);
    Task<PagedResponse<CampaignDto>> GetAllAsync(CampaignFilterDto filter);
    Task<List<CampaignDto>> GetByBusinessIdAsync(Guid businessId);
    Task<CampaignDto> CreateAsync(Guid businessId, CreateCampaignDto dto);
    Task<CampaignDto?> UpdateAsync(Guid id, UpdateCampaignDto dto);
}
