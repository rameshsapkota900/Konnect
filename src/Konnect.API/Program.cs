using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Konnect.API.Data;
using Konnect.API.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;

var builder = WebApplication.CreateBuilder(args);

// Add services
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Database
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// Redis Cache
builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration.GetConnectionString("Redis");
    options.InstanceName = "Konnect_";
});

// Firebase Admin SDK
var firebaseCredential = Environment.GetEnvironmentVariable("FIREBASE_CREDENTIALS");
var firebaseCredentialBase64 = Environment.GetEnvironmentVariable("FIREBASE_CREDENTIALS_BASE64");

if (!string.IsNullOrEmpty(firebaseCredentialBase64))
{
    // Decode Base64 encoded credentials (recommended for production)
    try
    {
        var jsonCredential = System.Text.Encoding.UTF8.GetString(Convert.FromBase64String(firebaseCredentialBase64));
        FirebaseApp.Create(new AppOptions
        {
            Credential = GoogleCredential.FromJson(jsonCredential)
        });
        Console.WriteLine("Firebase Admin SDK initialized from Base64 credentials.");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Warning: Failed to decode Firebase credentials: {ex.Message}");
    }
}
else if (!string.IsNullOrEmpty(firebaseCredential))
{
    // Use raw JSON credentials from environment variable
    try
    {
        FirebaseApp.Create(new AppOptions
        {
            Credential = GoogleCredential.FromJson(firebaseCredential)
        });
        Console.WriteLine("Firebase Admin SDK initialized from JSON credentials.");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Warning: Failed to parse Firebase credentials: {ex.Message}");
    }
}
else if (File.Exists("firebase-admin-sdk.json"))
{
    // Use local file for development
    FirebaseApp.Create(new AppOptions
    {
        Credential = GoogleCredential.FromFile("firebase-admin-sdk.json")
    });
    Console.WriteLine("Firebase Admin SDK initialized from local file.");
}
else
{
    // Skip Firebase Admin SDK initialization (auth still works via JWT)
    Console.WriteLine("Warning: Firebase Admin SDK not configured. Some features may be limited.");
}

// Firebase Authentication
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        var projectId = builder.Configuration["Firebase:ProjectId"];
        options.Authority = $"https://securetoken.google.com/{projectId}";
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = $"https://securetoken.google.com/{projectId}",
            ValidateAudience = true,
            ValidAudience = projectId,
            ValidateLifetime = true
        };
    });

// Services
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<ICampaignService, CampaignService>();
builder.Services.AddScoped<IDealService, DealService>();
builder.Services.AddScoped<ICreatorService, CreatorService>();
builder.Services.AddScoped<IEsewaService, EsewaService>();

// HTTP Client for eSewa
builder.Services.AddHttpClient();

// CORS
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// Configure pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Enable Swagger in all environments for now
app.UseSwagger();
app.UseSwaggerUI();

app.UseHttpsRedirection();
app.UseCors();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

// Auto-migrate database
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    db.Database.Migrate();
}

app.Run();
