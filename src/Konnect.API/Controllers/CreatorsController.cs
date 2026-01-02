using Konnect.API.Services;
using Konnect.Shared.DTOs;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Konnect.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class CreatorsController : ControllerBase
{
    private readonly ICreatorService _creatorService;
    private readonly IUserService _userService;

    public CreatorsController(ICreatorService creatorService, IUserService userService)
    {
        _creatorService = creatorService;
        _userService = userService;
    }

    [HttpGet]
    public async Task<ActionResult<ApiResponse<PagedResponse<CreatorProfileDto>>>> Search([FromQuery] CreatorFilterDto filter)
    {
        var result = await _creatorService.SearchAsync(filter);
        return Ok(ApiResponse<PagedResponse<CreatorProfileDto>>.Ok(result));
    }

    [HttpGet("{userId:guid}")]
    public async Task<ActionResult<ApiResponse<CreatorProfileDto>>> GetByUserId(Guid userId)
    {
        var profile = await _creatorService.GetByUserIdAsync(userId);
        if (profile == null)
            return NotFound(ApiResponse<CreatorProfileDto>.Fail("Creator profile not found"));

        return Ok(ApiResponse<CreatorProfileDto>.Ok(profile));
    }

    [HttpPost]
    public async Task<ActionResult<ApiResponse<CreatorProfileDto>>> Create([FromBody] CreateCreatorProfileDto dto)
    {
        var user = await GetCurrentUser();
        if (user == null) return Unauthorized();

        var existing = await _creatorService.GetByUserIdAsync(user.Id);
        if (existing != null)
            return BadRequest(ApiResponse<CreatorProfileDto>.Fail("Profile already exists"));

        var profile = await _creatorService.CreateAsync(user.Id, dto);
        return Ok(ApiResponse<CreatorProfileDto>.Ok(profile, "Profile created successfully"));
    }

    [HttpPut("me")]
    public async Task<ActionResult<ApiResponse<CreatorProfileDto>>> Update([FromBody] UpdateCreatorProfileDto dto)
    {
        var user = await GetCurrentUser();
        if (user == null) return Unauthorized();

        var profile = await _creatorService.UpdateAsync(user.Id, dto);
        if (profile == null)
            return NotFound(ApiResponse<CreatorProfileDto>.Fail("Profile not found"));

        return Ok(ApiResponse<CreatorProfileDto>.Ok(profile));
    }

    private async Task<UserDto?> GetCurrentUser()
    {
        var firebaseUid = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(firebaseUid)) return null;
        return await _userService.GetByFirebaseUidAsync(firebaseUid);
    }
}
