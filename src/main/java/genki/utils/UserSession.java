package genki.utils;

public class UserSession {
    private static String username;
    private static String userId;
    private static String userRole;
    private static String imageUrl;

    private UserSession() {}

    public static void startSession(String username, String userId, String userRole, String imageUrl) {
        UserSession.username = username;
        UserSession.userId= userId;
        UserSession.userRole = userRole;
        UserSession.imageUrl = imageUrl;
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

    public void setUsername(String username) {
        UserSession.username = username;
    }

    public void setUserId(String userId) {
        UserSession.userId = userId;
    }

    public void setUserRole(String userRole) {
        UserSession.userRole = userRole;
    }

    public static boolean isLoggedIn() {
        return UserSession.username != null;
    }

    public static void logout() {
        UserSession.username = null;
        UserSession.userId = null;
        UserSession.userRole = null;
    }

}
