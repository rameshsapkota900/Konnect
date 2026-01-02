using Konnect.API.Services;
using Konnect.Shared.DTOs;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Konnect.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class CampaignsController : ControllerBase
{
    private readonly ICampaignService _campaignService;
    private readonly IUserService _userService;

    public CampaignsController(ICampaignService campaignService, IUserService userService)
    {
        _campaignService = campaignService;
        _userService = userService;
    }

    [HttpGet]
    public async Task<ActionResult<ApiResponse<PagedResponse<CampaignDto>>>> GetAll([FromQuery] CampaignFilterDto filter)
    {
        var result = await _campaignService.GetAllAsync(filter);
        return Ok(ApiResponse<PagedResponse<CampaignDto>>.Ok(result));
    }

    [HttpGet("{id:guid}")]
    public async Task<ActionResult<ApiResponse<CampaignDto>>> GetById(Guid id)
    {
        var campaign = await _campaignService.GetByIdAsync(id);
        if (campaign == null)
            return NotFound(ApiResponse<CampaignDto>.Fail("Campaign not found"));

        return Ok(ApiResponse<CampaignDto>.Ok(campaign));
    }

    [HttpGet("mine")]
    public async Task<ActionResult<ApiResponse<List<CampaignDto>>>> GetMyCampaigns()
    {
        var user = await GetCurrentUser();
        if (user == null) return Unauthorized();

        var campaigns = await _campaignService.GetByBusinessIdAsync(user.Id);
        return Ok(ApiResponse<List<CampaignDto>>.Ok(campaigns));
    }

    [HttpPost]
    public async Task<ActionResult<ApiResponse<CampaignDto>>> Create([FromBody] CreateCampaignDto dto)
    {
        var user = await GetCurrentUser();
        if (user == null) return Unauthorized();

        var campaign = await _campaignService.CreateAsync(user.Id, dto);
        return Ok(ApiResponse<CampaignDto>.Ok(campaign, "Campaign created successfully"));
    }

    [HttpPut("{id:guid}")]
    public async Task<ActionResult<ApiResponse<CampaignDto>>> Update(Guid id, [FromBody] UpdateCampaignDto dto)
    {
        var campaign = await _campaignService.UpdateAsync(id, dto);
        if (campaign == null)
            return NotFound(ApiResponse<CampaignDto>.Fail("Campaign not found"));

        return Ok(ApiResponse<CampaignDto>.Ok(campaign));
    }

    private async Task<UserDto?> GetCurrentUser()
    {
        var firebaseUid = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(firebaseUid)) return null;
        return await _userService.GetByFirebaseUidAsync(firebaseUid);
    }
}
