namespace Konnect.Mobile;

public static class AppConfig
{
    // Firebase Configuration
    public const string FirebaseApiKey = "AIzaSyAj1_2B2E9sPzU5NhukfK-9LIK5irYbFdA";
    public const string FirebaseProjectId = "konnect-2c9ba";
    public const string FirebaseDatabaseUrl = "https://konnect-2c9ba-default-rtdb.asia-southeast1.firebasedatabase.app";
    public const string FirebaseStorageBucket = "konnect-2c9ba.firebasestorage.app";
    
    // Google OAuth Configuration
    public const string GoogleClientId = "554107027448-vfaa3o9d3os67lhvp3vrrp4a0gpchb8u.apps.googleusercontent.com";
    public const string GoogleRedirectUri = "com.konnect:/oauth2callback";
    
    // API Configuration
    public const string ApiBaseUrl = "https://konnect-n9so.onrender.com/api/";
    
    // eSewa Configuration
    public const string EsewaMerchantId = "EPAYTEST";
    public const string EsewaSecretKey = "8gBm/:&EnhH.1/q";
    public const bool EsewaTestMode = true; // Set to false for production
    public const string EsewaTestUrl = "https://rc-epay.esewa.com.np/api/epay/main/v2/form";
    public const string EsewaProductionUrl = "https://epay.esewa.com.np/api/epay/main/v2/form";
}
