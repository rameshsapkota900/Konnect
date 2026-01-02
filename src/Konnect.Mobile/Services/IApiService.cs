using Konnect.Shared.DTOs;

namespace Konnect.Mobile.Services;

public interface IApiService
{
    // Users
    Task<ApiResponse<UserDto>> RegisterUserAsync(RegisterUserDto dto);
    Task<ApiResponse<UserDto>> GetCurrentUserAsync();
    Task<ApiResponse<UserDto>> UpdateUserAsync(UpdateUserDto dto);
    
    // Creator Profiles
    Task<ApiResponse<CreatorProfileDto>> GetCreatorProfileAsync(Guid userId);
    Task<ApiResponse<CreatorProfileDto>> CreateCreatorProfileAsync(CreateCreatorProfileDto dto);
    Task<ApiResponse<CreatorProfileDto>> UpdateCreatorProfileAsync(UpdateCreatorProfileDto dto);
    Task<ApiResponse<PagedResponse<CreatorProfileDto>>> SearchCreatorsAsync(CreatorFilterDto filter);
    
    // Campaigns
    Task<ApiResponse<CampaignDto>> CreateCampaignAsync(CreateCampaignDto dto);
    Task<ApiResponse<CampaignDto>> GetCampaignAsync(Guid id);
    Task<ApiResponse<CampaignDto>> UpdateCampaignAsync(Guid id, UpdateCampaignDto dto);
    Task<ApiResponse<PagedResponse<CampaignDto>>> GetCampaignsAsync(CampaignFilterDto filter);
    Task<ApiResponse<List<CampaignDto>>> GetMyCampaignsAsync();
    
    // Deals
    Task<ApiResponse<DealDto>> CreateDealAsync(CreateDealDto dto);
    Task<ApiResponse<DealDto>> GetDealAsync(Guid id);
    Task<ApiResponse<DealDto>> UpdateDealStatusAsync(Guid id, UpdateDealStatusDto dto);
    Task<ApiResponse<List<DealDto>>> GetMyDealsAsync();
    
    // Reviews
    Task<ApiResponse<ReviewDto>> CreateReviewAsync(CreateReviewDto dto);
    Task<ApiResponse<List<ReviewDto>>> GetUserReviewsAsync(Guid userId);
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
