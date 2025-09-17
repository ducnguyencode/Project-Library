package Util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class Session {
    private Session() {}

    public static Long currentUserId = null;
    public static String currentUsername = null;
    public static String currentRole = null;
    public static String lastPatronId = null; // remember last used patron in checkout

    // Lightweight checkout cart (keeps CallNumbers across searches)
    private static final LinkedHashSet<String> checkoutCart = new LinkedHashSet<>();

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
        checkoutCart.clear();
        lastPatronId = null;
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

    // Cart helpers
    public static void cartAdd(String callNumber) {
        if (callNumber != null && !callNumber.isBlank()) checkoutCart.add(callNumber.trim());
    }

    public static void cartAddAll(List<String> calls) {
        if (calls == null) return;
        for (String c : calls) cartAdd(c);
    }

    public static void cartRemove(String callNumber) {
        if (callNumber != null) checkoutCart.remove(callNumber.trim());
    }

    public static void cartClear() { checkoutCart.clear(); }

    public static List<String> cartItems() { return new ArrayList<>(checkoutCart); }

    public static int cartSize() { return checkoutCart.size(); }

    // Remember last patron used in checkout
    public static void setLastPatronId(String patronId) { lastPatronId = (patronId==null||patronId.isBlank())?null:patronId.trim(); }
    public static String getLastPatronId() { return lastPatronId; }
}
