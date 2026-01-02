namespace Konnect.Mobile.Services;

public interface IFirebaseAuthService
{
    Task<AuthResult> SignInWithEmailAsync(string email, string password);
    Task<AuthResult> SignUpWithEmailAsync(string email, string password);
    Task<AuthResult> SignInWithGoogleAsync();
    Task SignOutAsync();
    Task<string?> GetCurrentUserTokenAsync();
    Task<bool> IsAuthenticatedAsync();
    string? GetCurrentUserId();
    event EventHandler<AuthStateChangedEventArgs>? AuthStateChanged;
}

public class AuthResult
{
    public bool Success { get; set; }
    public string? UserId { get; set; }
    public string? Email { get; set; }
    public string? Token { get; set; }
    public string? ErrorMessage { get; set; }

    public static AuthResult Ok(string userId, string email, string token) => new()
    {
        Success = true,
        UserId = userId,
        Email = email,
        Token = token
    };

    public static AuthResult Fail(string error) => new()
    {
        Success = false,
        ErrorMessage = error
    };
}

public class AuthStateChangedEventArgs : EventArgs
{
    public bool IsAuthenticated { get; set; }
    public string? UserId { get; set; }
}
