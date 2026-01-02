using Konnect.Shared.DTOs;

namespace Konnect.API.Services;

public interface IUserService
{
    Task<UserDto?> GetByFirebaseUidAsync(string firebaseUid);
    Task<UserDto?> GetByIdAsync(Guid id);
    Task<UserDto> CreateAsync(RegisterUserDto dto);
    Task<UserDto?> UpdateAsync(Guid id, UpdateUserDto dto);
}
