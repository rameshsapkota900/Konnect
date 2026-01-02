# Konnect

A mobile app connecting businesses with content creators for brand collaborations.

## Tech Stack

- **Frontend:** .NET MAUI Blazor Hybrid (iOS & Android)
- **Backend:** ASP.NET Core Web API
- **Database:** PostgreSQL
- **Cache:** Redis
- **Auth:** Firebase Authentication
- **Real-time Chat:** Firebase Realtime Database
- **Payments:** eSewa (Nepal)

## Project Structure

```
src/
├── Konnect.Mobile/          # MAUI Blazor mobile app
│   ├── Components/          # Razor components & pages
│   ├── Services/            # Firebase, API services
│   └── Resources/           # Icons, styles, fonts
├── Konnect.API/             # ASP.NET Core backend
│   ├── Controllers/         # API endpoints
│   ├── Services/            # Business logic
│   ├── Models/              # Entity models
│   └── Data/                # EF Core DbContext
└── Konnect.Shared/          # Shared DTOs & enums
```

## Prerequisites

- .NET 8 SDK
- Visual Studio 2022 with MAUI workload
- PostgreSQL
- Redis
- Firebase project

## Setup

### 1. Database

```bash
# Create PostgreSQL database
createdb konnect

# Update connection string in appsettings.json
```

### 2. Firebase

1. Create Firebase project at https://console.firebase.google.com
2. Enable Authentication (Email/Password, Google)
3. Enable Realtime Database
4. Download `google-services.json` for Android
5. Download Firebase Admin SDK JSON for backend

### 3. Backend

```bash
cd src/Konnect.API

# Update appsettings.json with your connection strings
# Add firebase-admin-sdk.json file

dotnet ef migrations add InitialCreate
dotnet ef database update
dotnet run
```

### 4. Mobile App

```bash
cd src/Konnect.Mobile

# Update AppConfig.cs with your Firebase config
# Update API base URL

dotnet build -t:Run -f net8.0-android
```

## Features

### For Businesses
- Browse and search creators by niche, followers, engagement
- Create campaigns with budget and deliverables
- Send offers to creators
- Real-time chat
- Escrow payments via eSewa

### For Creators
- Build profile with social media stats
- Browse available campaigns
- Accept/negotiate deals
- Submit content for approval
- Get paid securely

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/users/register` | POST | Register new user |
| `/api/users/me` | GET | Get current user |
| `/api/creators` | GET | Search creators |
| `/api/creators` | POST | Create creator profile |
| `/api/campaigns` | GET | List campaigns |
| `/api/campaigns` | POST | Create campaign |
| `/api/deals` | POST | Create deal |
| `/api/deals/{id}/status` | PUT | Update deal status |

## License

MIT
