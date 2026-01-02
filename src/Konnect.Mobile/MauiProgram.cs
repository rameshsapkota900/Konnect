using CommunityToolkit.Maui;
using Konnect.Mobile.Services;
using Microsoft.Extensions.Logging;

namespace Konnect.Mobile;

public static class MauiProgram
{
    public static MauiApp CreateMauiApp()
    {
        var builder = MauiApp.CreateBuilder();
        builder
            .UseMauiApp<App>()
            .UseMauiCommunityToolkit()
            .ConfigureFonts(fonts =>
            {
                fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
            });

        builder.Services.AddMauiBlazorWebView();

#if DEBUG
        builder.Services.AddBlazorWebViewDeveloperTools();
        builder.Logging.AddDebug();
#endif

        // Register Services
        builder.Services.AddSingleton<IFirebaseAuthService, FirebaseAuthService>();
        builder.Services.AddSingleton<IFirebaseChatService, FirebaseChatService>();
        builder.Services.AddSingleton<IApiService, ApiService>();
        builder.Services.AddSingleton<INavigationService, NavigationService>();
        builder.Services.AddSingleton<IEsewaPaymentService, EsewaPaymentService>();

        // HTTP Client for API
        builder.Services.AddHttpClient("KonnectAPI", client =>
        {
            client.BaseAddress = new Uri(AppConfig.ApiBaseUrl);
        });

        return builder.Build();
    }
}
