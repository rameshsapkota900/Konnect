namespace Konnect.Shared.DTOs;

public class ReviewDto
{
    public Guid Id { get; set; }
    public Guid DealId { get; set; }
    public Guid FromUserId { get; set; }
    public Guid ToUserId { get; set; }
    public int Rating { get; set; }
    public string? Comment { get; set; }
    public DateTime CreatedAt { get; set; }
    public UserDto? FromUser { get; set; }
}

public class CreateReviewDto
{
    public Guid DealId { get; set; }
    public Guid ToUserId { get; set; }
    public int Rating { get; set; }
    public string? Comment { get; set; }
}
