package genki.utils;

import genki.models.Notification;
import genki.models.User;
import genki.models.Group;
import genki.controllers.clientSocketController;
import genki.models.Conversation;
import javafx.scene.layout.HBox;

import java.util.ArrayList;


public class UserSession {

    private static String username;
    private static String userId;
    private static String userRole;
    private static String imageUrl;
    private static ArrayList<User> listFriends = new ArrayList<>();
    private static ArrayList<Group> listGroups = new ArrayList<>();
    private static ArrayList<Conversation> listConversations = new ArrayList<>();
    private static ArrayList<Notification> listNotifications = new ArrayList<>();
    public  static ArrayList<User> ConnectedUsers = new ArrayList<>();
    public  static ArrayList<HBox> conversationItems = new ArrayList<>();
    private static clientSocketController ClientSocket; 

    private UserSession() {}

    public static void startSession(String username, String userId, String userRole, String imageUrl) {
        UserSession.username = username;
        UserSession.userId= userId;
        UserSession.userRole = userRole;
        UserSession.imageUrl = imageUrl;
        UserSession.ClientSocket = new clientSocketController(username);
    }


    public static clientSocketController getClientSocket() {
		return ClientSocket;
	}

	public static void setClientSocket(clientSocketController clientSocket) {
		ClientSocket = clientSocket;
	}

	public static String getImageUrl() {
		return imageUrl;
	}

	public static void setImageUrl(String imageUrl) {
		UserSession.imageUrl = imageUrl;
	}

	public static String getUsername() {
        return UserSession.username;
    }

    public static String getUserId() {
        return UserSession.userId;
    }

    public static String getUserRole() {
        return UserSession.userRole;
    }

    public static void setUsername(String username) {
        UserSession.username = username;
    }



    public static ArrayList<User> getConnectedUsers() {
		return ConnectedUsers;
	}

	public static void setConnectedUsers(ArrayList<User> connectedUsers) {
		ConnectedUsers = connectedUsers;
	}

	public static boolean isLoggedIn() {
        return UserSession.username != null;
    }

    public static void logout() {
        UserSession.username = null;
        UserSession.userId = null;
        UserSession.userRole = null;
        UserSession.imageUrl = null;
        UserSession.listGroups.clear();
        UserSession.listFriends.clear();
        UserSession.listConversations.clear();
        UserSession.listNotifications.clear();
    }

    public static ArrayList<Notification> getNotifications() {
        return UserSession.listNotifications;
    }

    public static void addNotification(Notification notification) {
        UserSession.listNotifications.add(notification);
    }

    public static void removeNotification(Notification notification) {
        UserSession.listNotifications.remove(notification);
    }

    public static void setNotificationsEmpty() {
        UserSession.listNotifications.clear();
    }

    public static ArrayList<User> getFriends() {
        return UserSession.listFriends;
    }


    public static ArrayList<Group> getGroups() {
        return UserSession.listGroups;
    }

    public static ArrayList<Conversation> getConversations() {
        return UserSession.listConversations;
    }

    public static ArrayList<HBox> getConversationItems() {
        return conversationItems;
    }

    public static void setConversationItems(ArrayList<HBox> items) {
        conversationItems = items;
    }

    public static void addConversationItem(HBox item) {
        conversationItems.add(item);
    }

    public static void removeConversationItem(HBox item) {
        conversationItems.remove(item);
    }

    public static void addFriend(User friend){
        UserSession.listFriends.add(friend);
    }

    public static void addGroup(Group group) {
        UserSession.listGroups.add(group);
    }

    public static void addConversation(Conversation conversation) {
        UserSession.listConversations.add(conversation);
    }

    public static void removeFriend(User friend) {
        UserSession.listFriends.remove(friend);
    }

    public static void removeGroup(Group group) {
        UserSession.listGroups.remove(group);
    }

    public static void removeConversation(Conversation conversation) {
        UserSession.listConversations.remove(conversation);
    }

    /**
     * Initializes the session's friends and conversations lists.
     * @param friends List of User objects representing friends.
     * @param conversations List of Conversation objects.
     */
    public static void loadConversations(ArrayList<User> friends, ArrayList<Conversation> conversations) {
        UserSession.listFriends = friends != null ? friends : new ArrayList<>();
        UserSession.listConversations = conversations != null ? conversations : new ArrayList<>();
    }

}
