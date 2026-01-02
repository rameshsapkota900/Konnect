using Konnect.Shared.Enums;

namespace Konnect.Shared.DTOs;

public class UserDto
{
    public Guid Id { get; set; }
    public string FirebaseUid { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public UserType UserType { get; set; }
    public string? ProfileImageUrl { get; set; }
    public string? Location { get; set; }
    public string? Phone { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class RegisterUserDto
{
    public string FirebaseUid { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public UserType UserType { get; set; }
    public string? Location { get; set; }
    public string? Phone { get; set; }
}

public class UpdateUserDto
{
    public string? FullName { get; set; }
    public string? ProfileImageUrl { get; set; }
    public string? Location { get; set; }
    public string? Phone { get; set; }
}
