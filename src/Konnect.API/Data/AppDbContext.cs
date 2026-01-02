using Konnect.API.Models;
using Microsoft.EntityFrameworkCore;

namespace Konnect.API.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<User> Users => Set<User>();
    public DbSet<CreatorProfile> CreatorProfiles => Set<CreatorProfile>();
    public DbSet<Campaign> Campaigns => Set<Campaign>();
    public DbSet<Deal> Deals => Set<Deal>();
    public DbSet<Review> Reviews => Set<Review>();
    public DbSet<Payment> Payments => Set<Payment>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<User>(entity =>
        {
            entity.HasIndex(e => e.FirebaseUid).IsUnique();
            entity.HasIndex(e => e.Email).IsUnique();
        });

        modelBuilder.Entity<CreatorProfile>(entity =>
        {
            entity.HasOne(e => e.User)
                  .WithOne()
                  .HasForeignKey<CreatorProfile>(e => e.UserId);
        });

        modelBuilder.Entity<Campaign>(entity =>
        {
            entity.HasOne(e => e.Business)
                  .WithMany()
                  .HasForeignKey(e => e.BusinessId);
        });

        modelBuilder.Entity<Deal>(entity =>
        {
            entity.HasOne(e => e.Campaign)
                  .WithMany()
                  .HasForeignKey(e => e.CampaignId);
            
            entity.HasOne(e => e.Creator)
                  .WithMany()
                  .HasForeignKey(e => e.CreatorId)
                  .OnDelete(DeleteBehavior.Restrict);
            
            entity.HasOne(e => e.Business)
                  .WithMany()
                  .HasForeignKey(e => e.BusinessId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        modelBuilder.Entity<Review>(entity =>
        {
            entity.HasOne(e => e.Deal)
                  .WithMany()
                  .HasForeignKey(e => e.DealId);
            
            entity.HasOne(e => e.FromUser)
                  .WithMany()
                  .HasForeignKey(e => e.FromUserId)
                  .OnDelete(DeleteBehavior.Restrict);
            
            entity.HasOne(e => e.ToUser)
                  .WithMany()
                  .HasForeignKey(e => e.ToUserId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        modelBuilder.Entity<Payment>(entity =>
        {
            entity.HasOne(e => e.Deal)
                  .WithMany()
                  .HasForeignKey(e => e.DealId);
        });
    }
}
