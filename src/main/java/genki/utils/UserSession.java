package genki.utils;

public class UserSession {
    private static String username;
    private static String userId;
    private static String userRole;

    private UserSession() {}

    public static void startSession(String username, String userId, String userRole) {
        UserSession.username = username;
        UserSession.userId= userId;
        UserSession.userRole = userRole;
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



    public static boolean isLoggedIn() {
        return UserSession.username != null;
    }

    public static void logout() {
        UserSession.username = null;
        UserSession.userId = null;
        UserSession.userRole = null;
    }

}
