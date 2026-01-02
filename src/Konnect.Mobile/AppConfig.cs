namespace Konnect.Mobile;

public static class AppConfig
{
    // Firebase Configuration
    public const string FirebaseApiKey = "AIzaSyAj1_2B2E9sPzU5NhukfK-9LIK5irYbFdA";
    public const string FirebaseProjectId = "konnect-2c9ba";
    public const string FirebaseDatabaseUrl = "https://konnect-2c9ba-default-rtdb.asia-southeast1.firebasedatabase.app";
    public const string FirebaseStorageBucket = "konnect-2c9ba.firebasestorage.app";
    
    // API Configuration
    public const string ApiBaseUrl = "https://localhost:7001/api/"; // Update for production
    
    // eSewa Configuration
    public const string EsewaClientId = ""; // Add your eSewa client ID
    public const string EsewaSecretKey = ""; // Add your eSewa secret key
    public const bool EsewaTestMode = true; // Set to false for production
}
