FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 80

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["Konnect.API/Konnect.API.csproj", "Konnect.API/"]
COPY ["Konnect.Shared/Konnect.Shared.csproj", "Konnect.Shared/"]
RUN dotnet restore "Konnect.API/Konnect.API.csproj"
COPY . .
WORKDIR "/src/Konnect.API"
RUN dotnet build "Konnect.API.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "Konnect.API.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "Konnect.API.dll"]
