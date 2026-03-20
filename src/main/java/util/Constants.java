package util;

// central place for values used across the app
public class Constants {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";

    public static final int LOAN_DURATION_DAYS = 14;

    // input limits
    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 30;
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 64;
    public static final int NAME_MAX = 50;
    public static final int EMAIL_MAX = 100;

    // brute force protection
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    // session timeout after inactivity
    public static final long SESSION_TIMEOUT_MS = 15 * 60 * 1000; // 15 minutes
}
