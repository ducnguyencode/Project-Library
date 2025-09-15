package Util;

public final class Session {
    private Session() {}

    public static Long currentUserId = null;
    public static String currentUsername = null;
    public static String currentRole = null;

    // setter khi đăng nhập
    public static void login(Long userId, String username, String role) {
        currentUserId = userId;
        currentUsername = username;
        currentRole = role;
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
        currentRole = null;
    }

    // getter
    public static Long getUserId() {
        return currentUserId;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static String getRole() {
        return currentRole;
    }
}
