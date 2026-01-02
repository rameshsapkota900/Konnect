using Konnect.API.Services;
using Konnect.Shared.DTOs;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Konnect.API.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class DealsController : ControllerBase
{
    private readonly IDealService _dealService;
    private readonly IUserService _userService;

    public DealsController(IDealService dealService, IUserService userService)
    {
        _dealService = dealService;
        _userService = userService;
    }

    [HttpGet("{id:guid}")]
    public async Task<ActionResult<ApiResponse<DealDto>>> GetById(Guid id)
    {
        var deal = await _dealService.GetByIdAsync(id);
        if (deal == null)
            return NotFound(ApiResponse<DealDto>.Fail("Deal not found"));

        return Ok(ApiResponse<DealDto>.Ok(deal));
    }

    [HttpGet("mine")]
    public async Task<ActionResult<ApiResponse<List<DealDto>>>> GetMyDeals()
    {
        var user = await GetCurrentUser();
        if (user == null) return Unauthorized();

        var deals = await _dealService.GetByUserIdAsync(user.Id);
        return Ok(ApiResponse<List<DealDto>>.Ok(deals));
    }

    [HttpPost]
    public async Task<ActionResult<ApiResponse<DealDto>>> Create([FromBody] CreateDealDto dto)
    {
        var user = await GetCurrentUser();
        if (user == null) return Unauthorized();

        var deal = await _dealService.CreateAsync(user.Id, dto);
        return Ok(ApiResponse<DealDto>.Ok(deal, "Deal created successfully"));
    }

    [HttpPut("{id:guid}/status")]
    public async Task<ActionResult<ApiResponse<DealDto>>> UpdateStatus(Guid id, [FromBody] UpdateDealStatusDto dto)
    {
        var deal = await _dealService.UpdateStatusAsync(id, dto);
        if (deal == null)
            return NotFound(ApiResponse<DealDto>.Fail("Deal not found"));

        return Ok(ApiResponse<DealDto>.Ok(deal));
    }

    private async Task<UserDto?> GetCurrentUser()
    {
        var firebaseUid = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(firebaseUid)) return null;
        return await _userService.GetByFirebaseUidAsync(firebaseUid);
    }
}
