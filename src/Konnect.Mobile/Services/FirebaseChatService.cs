using Firebase.Database;
using Firebase.Database.Query;
using System.Reactive.Linq;

namespace Konnect.Mobile.Services;

public class FirebaseChatService : IFirebaseChatService
{
    private readonly FirebaseClient _firebaseClient;

    public FirebaseChatService()
    {
        _firebaseClient = new FirebaseClient(AppConfig.FirebaseDatabaseUrl);
    }

    public async Task<string> CreateChatRoomAsync(string user1Id, string user2Id, string dealId)
    {
        var chatRoom = new ChatRoom
        {
            Id = Guid.NewGuid().ToString(),
            DealId = dealId,
            Participants = new List<string> { user1Id, user2Id },
            CreatedAt = DateTime.UtcNow
        };

        await _firebaseClient
            .Child("chatRooms")
            .Child(chatRoom.Id)
            .PutAsync(chatRoom);

        return chatRoom.Id;
    }

    public async Task SendMessageAsync(string chatId, ChatMessage message)
    {
        message.Id = Guid.NewGuid().ToString();
        message.Timestamp = DateTime.UtcNow;

        await _firebaseClient
            .Child("messages")
            .Child(chatId)
            .Child(message.Id)
            .PutAsync(message);

        // Update last message in chat room
        await _firebaseClient
            .Child("chatRooms")
            .Child(chatId)
            .Child("lastMessage")
            .PutAsync(message);
    }

    public IObservable<ChatMessage> GetMessages(string chatId)
    {
        return _firebaseClient
            .Child("messages")
            .Child(chatId)
            .AsObservable<ChatMessage>()
            .Select(x => x.Object);
    }

    public async Task<List<ChatRoom>> GetUserChatsAsync(string userId)
    {
        var chatRooms = await _firebaseClient
            .Child("chatRooms")
            .OnceAsync<ChatRoom>();

        return chatRooms
            .Where(x => x.Object.Participants.Contains(userId))
            .Select(x => x.Object)
            .OrderByDescending(x => x.LastMessage?.Timestamp ?? x.CreatedAt)
            .ToList();
    }

    public async Task MarkAsReadAsync(string chatId, string userId)
    {
        var messages = await _firebaseClient
            .Child("messages")
            .Child(chatId)
            .OnceAsync<ChatMessage>();

        foreach (var msg in messages.Where(m => m.Object.SenderId != userId && !m.Object.IsRead))
        {
            await _firebaseClient
                .Child("messages")
                .Child(chatId)
                .Child(msg.Key)
                .Child("isRead")
                .PutAsync(true);
        }
    }
}
