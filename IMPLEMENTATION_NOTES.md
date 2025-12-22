IMPLEMENTATION SUMMARY: Real-time Message Receiving and Display
================================================================

## What Was Added:

### 1. MessageData Class (genki/models/MessageData.java)
A new data structure to hold all message information:
- conversationId: Which conversation the message belongs to
- senderId: Who sent it
- senderName: Display name of sender
- messageText: The actual message content
- senderProfileImage: Sender's profile picture URL
- timestamp: When the message was sent

### 2. clientSocketController Updates
- Added: `Consumer<MessageData> onNewMessageCallback` field
- Added: `setOnNewMessageCallback()` method - allows HomeController to register a callback
- Updated: `onMessageReceived()` method to:
  - Distinguish between USERS_LIST messages and regular messages
  - Parse incoming JSON messages into MessageData objects
  - Call the callback with the message data

### 3. HomeController Updates
- Added import for MessageData
- Updated: `initialize()` method to register the message callback
- Updated: Message sending logic to:
  - Create a MessageData object with all message info (conversationId, senderId, senderName, etc.)
  - Convert it to JSON using Gson
  - Send the JSON via socket
- The callback now:
  - Only displays messages for the currently open conversation
  - Adds the message directly to the UI without refreshing

## How It Works:

1. **User sends message:**
   - User types message and clicks send
   - MessageData object is created with all info (including conversationId)
   - JSON is sent via socket to server

2. **User receives message:**
   - Message arrives at clientSocketController in background thread
   - onMessageReceived() parses the JSON into MessageData
   - Calls the registered callback with message data
   - HomeController's callback checks if message is for current conversation
   - If yes: Message is added directly to UI
   - If no: Message is ignored (user not viewing that conversation)

## Benefits:
- ✓ Fast: Only one message is added to UI, no refresh needed
- ✓ Efficient: Messages only display if conversation is open
- ✓ Clean: All message data is structured and easy to extend
- ✓ Real-time: Callback pattern allows instant UI updates
