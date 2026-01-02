using Konnect.Shared.DTOs;

namespace Konnect.API.Services;

public interface ICreatorService
{
    Task<CreatorProfileDto?> GetByUserIdAsync(Guid userId);
    Task<PagedResponse<CreatorProfileDto>> SearchAsync(CreatorFilterDto filter);
    Task<CreatorProfileDto> CreateAsync(Guid userId, CreateCreatorProfileDto dto);
    Task<CreatorProfileDto?> UpdateAsync(Guid userId, UpdateCreatorProfileDto dto);
}

public class CreatorFilterDto
{
    public Konnect.Shared.Enums.Niche? Niche { get; set; }
    public int? MinFollowers { get; set; }
    public int? MaxFollowers { get; set; }
    public decimal? MinRate { get; set; }
    public decimal? MaxRate { get; set; }
    public string? Location { get; set; }
    public int Page { get; set; } = 1;
    public int PageSize { get; set; } = 20;
}
