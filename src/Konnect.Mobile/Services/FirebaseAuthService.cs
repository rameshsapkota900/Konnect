using Firebase.Auth;
using Firebase.Auth.Providers;

namespace Konnect.Mobile.Services;

public class FirebaseAuthService : IFirebaseAuthService
{
    private readonly FirebaseAuthClient _authClient;
    private UserCredential? _currentUser;

    public event EventHandler<AuthStateChangedEventArgs>? AuthStateChanged;

    public FirebaseAuthService()
    {
        var config = new FirebaseAuthConfig
        {
            ApiKey = AppConfig.FirebaseApiKey,
            AuthDomain = $"{AppConfig.FirebaseProjectId}.firebaseapp.com",
            Providers = new FirebaseAuthProvider[]
            {
                new EmailProvider(),
                new GoogleProvider()
            }
        };

        _authClient = new FirebaseAuthClient(config);
        _authClient.AuthStateChanged += OnAuthStateChanged;
    }

    private void OnAuthStateChanged(object? sender, UserEventArgs e)
    {
        AuthStateChanged?.Invoke(this, new AuthStateChangedEventArgs
        {
            IsAuthenticated = e.User != null,
            UserId = e.User?.Uid
        });
    }

    public async Task<AuthResult> SignInWithEmailAsync(string email, string password)
    {
        try
        {
            _currentUser = await _authClient.SignInWithEmailAndPasswordAsync(email, password);
            var token = await _currentUser.User.GetIdTokenAsync();
            return AuthResult.Ok(_currentUser.User.Uid, email, token);
        }
        catch (FirebaseAuthException ex)
        {
            return AuthResult.Fail(GetFriendlyErrorMessage(ex.Reason));
        }
        catch (Exception ex)
        {
            return AuthResult.Fail(ex.Message);
        }
    }

    public async Task<AuthResult> SignUpWithEmailAsync(string email, string password)
    {
        try
        {
            _currentUser = await _authClient.CreateUserWithEmailAndPasswordAsync(email, password);
            var token = await _currentUser.User.GetIdTokenAsync();
            return AuthResult.Ok(_currentUser.User.Uid, email, token);
        }
        catch (FirebaseAuthException ex)
        {
            return AuthResult.Fail(GetFriendlyErrorMessage(ex.Reason));
        }
        catch (Exception ex)
        {
            return AuthResult.Fail(ex.Message);
        }
    }

    public async Task<AuthResult> SignInWithGoogleAsync()
    {
        try
        {
            // Google sign-in on MAUI requires additional setup
            // For now, guide users to use email/password
            // To enable Google sign-in:
            // 1. Configure OAuth consent screen in Google Cloud Console
            // 2. Add SHA-1 fingerprint to Firebase
            // 3. Enable Google sign-in in Firebase Authentication
            
            await Task.CompletedTask;
            return AuthResult.Fail("Google sign-in requires additional setup. Please use email and password to sign in.");
        }
        catch (Exception ex)
        {
            return AuthResult.Fail($"Google sign-in failed: {ex.Message}");
        }
    }

    public async Task SignOutAsync()
    {
        _authClient.SignOut();
        _currentUser = null;
        await Task.CompletedTask;
    }

    public async Task<string?> GetCurrentUserTokenAsync()
    {
        if (_currentUser?.User == null) return null;
        return await _currentUser.User.GetIdTokenAsync();
    }

    public async Task<bool> IsAuthenticatedAsync()
    {
        return await Task.FromResult(_currentUser?.User != null);
    }

    public string? GetCurrentUserId() => _currentUser?.User?.Uid;

    private static string GetFriendlyErrorMessage(AuthErrorReason reason)
    {
        return reason switch
        {
            AuthErrorReason.InvalidEmailAddress => "Invalid email address",
            AuthErrorReason.WrongPassword => "Incorrect password",
            AuthErrorReason.UserNotFound => "No account found with this email",
            AuthErrorReason.EmailExists => "An account with this email already exists",
            AuthErrorReason.WeakPassword => "Password is too weak. Use at least 6 characters",
            AuthErrorReason.TooManyAttemptsTryLater => "Too many attempts. Please try again later",
            _ => "Authentication failed. Please try again"
        };
    }
}
