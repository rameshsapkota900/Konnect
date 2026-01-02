using Konnect.API.Services;
using Konnect.Shared.DTOs;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace Konnect.API.Controllers;

[ApiController]
[Route("api/[controller]")]
public class UsersController : ControllerBase
{
    private readonly IUserService _userService;

    public UsersController(IUserService userService)
    {
        _userService = userService;
    }

    [HttpPost("register")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<UserDto>>> Register([FromBody] RegisterUserDto dto)
    {
        var firebaseUid = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(firebaseUid))
            return Unauthorized(ApiResponse<UserDto>.Fail("Invalid token"));

        dto.FirebaseUid = firebaseUid;
        dto.Email = User.FindFirst(ClaimTypes.Email)?.Value ?? "";

        var existing = await _userService.GetByFirebaseUidAsync(firebaseUid);
        if (existing != null)
            return BadRequest(ApiResponse<UserDto>.Fail("User already exists"));

        var user = await _userService.CreateAsync(dto);
        return Ok(ApiResponse<UserDto>.Ok(user, "User registered successfully"));
    }

    [HttpGet("me")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<UserDto>>> GetCurrentUser()
    {
        var firebaseUid = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(firebaseUid))
            return Unauthorized(ApiResponse<UserDto>.Fail("Invalid token"));

        var user = await _userService.GetByFirebaseUidAsync(firebaseUid);
        if (user == null)
            return NotFound(ApiResponse<UserDto>.Fail("User not found"));

        return Ok(ApiResponse<UserDto>.Ok(user));
    }

    [HttpPut("me")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<UserDto>>> UpdateCurrentUser([FromBody] UpdateUserDto dto)
    {
        var firebaseUid = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(firebaseUid))
            return Unauthorized(ApiResponse<UserDto>.Fail("Invalid token"));

        var user = await _userService.GetByFirebaseUidAsync(firebaseUid);
        if (user == null)
            return NotFound(ApiResponse<UserDto>.Fail("User not found"));

        var updated = await _userService.UpdateAsync(user.Id, dto);
        return Ok(ApiResponse<UserDto>.Ok(updated!));
    }

    [HttpGet("{id:guid}")]
    [Authorize]
    public async Task<ActionResult<ApiResponse<UserDto>>> GetById(Guid id)
    {
        var user = await _userService.GetByIdAsync(id);
        if (user == null)
            return NotFound(ApiResponse<UserDto>.Fail("User not found"));

        return Ok(ApiResponse<UserDto>.Ok(user));
    }
}
