using Konnect.Shared.DTOs;

namespace Konnect.API.Services;

public interface IDealService
{
    Task<DealDto?> GetByIdAsync(Guid id);
    Task<List<DealDto>> GetByUserIdAsync(Guid userId);
    Task<DealDto> CreateAsync(Guid businessId, CreateDealDto dto);
    Task<DealDto?> UpdateStatusAsync(Guid id, UpdateDealStatusDto dto);
}
