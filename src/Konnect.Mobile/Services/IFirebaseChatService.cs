namespace Konnect.Mobile.Services;

public interface IFirebaseChatService
{
    Task SendMessageAsync(string chatId, ChatMessage message);
    IObservable<ChatMessage> GetMessages(string chatId);
    Task<List<ChatRoom>> GetUserChatsAsync(string userId);
    Task<string> CreateChatRoomAsync(string user1Id, string user2Id, string dealId);
    Task MarkAsReadAsync(string chatId, string userId);
}

public class ChatMessage
{
    public string Id { get; set; } = string.Empty;
    public string SenderId { get; set; } = string.Empty;
    public string SenderName { get; set; } = string.Empty;
    public string Text { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
    public bool IsRead { get; set; }
    public string? AttachmentUrl { get; set; }
}

public class ChatRoom
{
    public string Id { get; set; } = string.Empty;
    public string DealId { get; set; } = string.Empty;
    public List<string> Participants { get; set; } = new();
    public ChatMessage? LastMessage { get; set; }
    public DateTime CreatedAt { get; set; }
    public int UnreadCount { get; set; }
}
