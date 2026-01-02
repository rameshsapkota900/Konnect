using Konnect.API.Data;
using Konnect.API.Models;
using Konnect.Shared.DTOs;
using Microsoft.EntityFrameworkCore;

namespace Konnect.API.Services;

public class UserService : IUserService
{
    private readonly AppDbContext _db;

    public UserService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<UserDto?> GetByFirebaseUidAsync(string firebaseUid)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.FirebaseUid == firebaseUid);
        return user == null ? null : MapToDto(user);
    }

    public async Task<UserDto?> GetByIdAsync(Guid id)
    {
        var user = await _db.Users.FindAsync(id);
        return user == null ? null : MapToDto(user);
    }

    public async Task<UserDto> CreateAsync(RegisterUserDto dto)
    {
        var user = new User
        {
            Id = Guid.NewGuid(),
            FirebaseUid = dto.FirebaseUid,
            Email = dto.Email,
            FullName = dto.FullName,
            UserType = dto.UserType,
            Location = dto.Location,
            Phone = dto.Phone
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();

        return MapToDto(user);
    }

    public async Task<UserDto?> UpdateAsync(Guid id, UpdateUserDto dto)
    {
        var user = await _db.Users.FindAsync(id);
        if (user == null) return null;

        if (dto.FullName != null) user.FullName = dto.FullName;
        if (dto.ProfileImageUrl != null) user.ProfileImageUrl = dto.ProfileImageUrl;
        if (dto.Location != null) user.Location = dto.Location;
        if (dto.Phone != null) user.Phone = dto.Phone;
        user.UpdatedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return MapToDto(user);
    }

    private static UserDto MapToDto(User user) => new()
    {
        Id = user.Id,
        FirebaseUid = user.FirebaseUid,
        Email = user.Email,
        FullName = user.FullName,
        UserType = user.UserType,
        ProfileImageUrl = user.ProfileImageUrl,
        Location = user.Location,
        Phone = user.Phone,
        CreatedAt = user.CreatedAt
    };
}
