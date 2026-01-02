namespace Konnect.Mobile.Services;

public interface INavigationService
{
    Task NavigateToAsync(string route);
    Task GoBackAsync();
}

public class NavigationService : INavigationService
{
    public Task NavigateToAsync(string route)
    {
        // Navigation will be handled by Blazor routing
        return Task.CompletedTask;
    }

    public Task GoBackAsync()
    {
        return Task.CompletedTask;
    }
}
