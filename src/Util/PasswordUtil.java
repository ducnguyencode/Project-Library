// Util/PasswordUtil.java
package Util;

import org.mindrot.jbcrypt.BCrypt;

/** Hash & verify mật khẩu với BCrypt. */
public final class PasswordUtil {
    private PasswordUtil(){}
    public static String hash(String plain) {
        // cost 10-12 là hợp lý cho desktop app
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }
    public static boolean verify(String plain, String hash) {
        if (hash == null || hash.isBlank()) return false;
        return BCrypt.checkpw(plain, hash);
    }
}