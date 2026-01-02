using System.Net.Http.Json;
using System.Text.Json;
using Konnect.Shared.DTOs;

namespace Konnect.Mobile.Services;

public class ApiService : IApiService
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IFirebaseAuthService _authService;
    private readonly JsonSerializerOptions _jsonOptions;

    public ApiService(IHttpClientFactory httpClientFactory, IFirebaseAuthService authService)
    {
        _httpClientFactory = httpClientFactory;
        _authService = authService;
        _jsonOptions = new JsonSerializerOptions { PropertyNameCaseInsensitive = true };
    }

    private async Task<HttpClient> GetClientAsync()
    {
        var client = _httpClientFactory.CreateClient("KonnectAPI");
        var token = await _authService.GetCurrentUserTokenAsync();
        if (!string.IsNullOrEmpty(token))
        {
            client.DefaultRequestHeaders.Authorization = 
                new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);
        }
        return client;
    }

    // Users
    public async Task<ApiResponse<UserDto>> RegisterUserAsync(RegisterUserDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PostAsJsonAsync("users/register", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<UserDto>>(_jsonOptions) 
            ?? ApiResponse<UserDto>.Fail("Failed to register");
    }

    public async Task<ApiResponse<UserDto>> GetCurrentUserAsync()
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync("users/me");
        return await response.Content.ReadFromJsonAsync<ApiResponse<UserDto>>(_jsonOptions)
            ?? ApiResponse<UserDto>.Fail("Failed to get user");
    }

    public async Task<ApiResponse<UserDto>> UpdateUserAsync(UpdateUserDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PutAsJsonAsync("users/me", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<UserDto>>(_jsonOptions)
            ?? ApiResponse<UserDto>.Fail("Failed to update user");
    }

    // Creator Profiles
    public async Task<ApiResponse<CreatorProfileDto>> GetCreatorProfileAsync(Guid userId)
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync($"creators/{userId}");
        return await response.Content.ReadFromJsonAsync<ApiResponse<CreatorProfileDto>>(_jsonOptions)
            ?? ApiResponse<CreatorProfileDto>.Fail("Failed to get profile");
    }

    public async Task<ApiResponse<CreatorProfileDto>> CreateCreatorProfileAsync(CreateCreatorProfileDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PostAsJsonAsync("creators", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<CreatorProfileDto>>(_jsonOptions)
            ?? ApiResponse<CreatorProfileDto>.Fail("Failed to create profile");
    }

    public async Task<ApiResponse<CreatorProfileDto>> UpdateCreatorProfileAsync(UpdateCreatorProfileDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PutAsJsonAsync("creators/me", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<CreatorProfileDto>>(_jsonOptions)
            ?? ApiResponse<CreatorProfileDto>.Fail("Failed to update profile");
    }

    public async Task<ApiResponse<PagedResponse<CreatorProfileDto>>> SearchCreatorsAsync(CreatorFilterDto filter)
    {
        var client = await GetClientAsync();
        var query = BuildQueryString(filter);
        var response = await client.GetAsync($"creators?{query}");
        return await response.Content.ReadFromJsonAsync<ApiResponse<PagedResponse<CreatorProfileDto>>>(_jsonOptions)
            ?? ApiResponse<PagedResponse<CreatorProfileDto>>.Fail("Failed to search creators");
    }

    // Campaigns
    public async Task<ApiResponse<CampaignDto>> CreateCampaignAsync(CreateCampaignDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PostAsJsonAsync("campaigns", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<CampaignDto>>(_jsonOptions)
            ?? ApiResponse<CampaignDto>.Fail("Failed to create campaign");
    }

    public async Task<ApiResponse<CampaignDto>> GetCampaignAsync(Guid id)
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync($"campaigns/{id}");
        return await response.Content.ReadFromJsonAsync<ApiResponse<CampaignDto>>(_jsonOptions)
            ?? ApiResponse<CampaignDto>.Fail("Failed to get campaign");
    }

    public async Task<ApiResponse<CampaignDto>> UpdateCampaignAsync(Guid id, UpdateCampaignDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PutAsJsonAsync($"campaigns/{id}", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<CampaignDto>>(_jsonOptions)
            ?? ApiResponse<CampaignDto>.Fail("Failed to update campaign");
    }

    public async Task<ApiResponse<PagedResponse<CampaignDto>>> GetCampaignsAsync(CampaignFilterDto filter)
    {
        var client = await GetClientAsync();
        var query = BuildQueryString(filter);
        var response = await client.GetAsync($"campaigns?{query}");
        return await response.Content.ReadFromJsonAsync<ApiResponse<PagedResponse<CampaignDto>>>(_jsonOptions)
            ?? ApiResponse<PagedResponse<CampaignDto>>.Fail("Failed to get campaigns");
    }

    public async Task<ApiResponse<List<CampaignDto>>> GetMyCampaignsAsync()
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync("campaigns/mine");
        return await response.Content.ReadFromJsonAsync<ApiResponse<List<CampaignDto>>>(_jsonOptions)
            ?? ApiResponse<List<CampaignDto>>.Fail("Failed to get campaigns");
    }

    // Deals
    public async Task<ApiResponse<DealDto>> CreateDealAsync(CreateDealDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PostAsJsonAsync("deals", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<DealDto>>(_jsonOptions)
            ?? ApiResponse<DealDto>.Fail("Failed to create deal");
    }

    public async Task<ApiResponse<DealDto>> GetDealAsync(Guid id)
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync($"deals/{id}");
        return await response.Content.ReadFromJsonAsync<ApiResponse<DealDto>>(_jsonOptions)
            ?? ApiResponse<DealDto>.Fail("Failed to get deal");
    }

    public async Task<ApiResponse<DealDto>> UpdateDealStatusAsync(Guid id, UpdateDealStatusDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PutAsJsonAsync($"deals/{id}/status", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<DealDto>>(_jsonOptions)
            ?? ApiResponse<DealDto>.Fail("Failed to update deal");
    }

    public async Task<ApiResponse<List<DealDto>>> GetMyDealsAsync()
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync("deals/mine");
        return await response.Content.ReadFromJsonAsync<ApiResponse<List<DealDto>>>(_jsonOptions)
            ?? ApiResponse<List<DealDto>>.Fail("Failed to get deals");
    }

    // Reviews
    public async Task<ApiResponse<ReviewDto>> CreateReviewAsync(CreateReviewDto dto)
    {
        var client = await GetClientAsync();
        var response = await client.PostAsJsonAsync("reviews", dto);
        return await response.Content.ReadFromJsonAsync<ApiResponse<ReviewDto>>(_jsonOptions)
            ?? ApiResponse<ReviewDto>.Fail("Failed to create review");
    }

    public async Task<ApiResponse<List<ReviewDto>>> GetUserReviewsAsync(Guid userId)
    {
        var client = await GetClientAsync();
        var response = await client.GetAsync($"reviews/user/{userId}");
        return await response.Content.ReadFromJsonAsync<ApiResponse<List<ReviewDto>>>(_jsonOptions)
            ?? ApiResponse<List<ReviewDto>>.Fail("Failed to get reviews");
    }

    private static string BuildQueryString(object filter)
    {
        var properties = filter.GetType().GetProperties()
            .Where(p => p.GetValue(filter) != null)
            .Select(p => $"{p.Name}={Uri.EscapeDataString(p.GetValue(filter)?.ToString() ?? "")}");
        return string.Join("&", properties);
    }
}
