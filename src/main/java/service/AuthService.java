package service;

import model.User;
import util.Constants;
import util.DataManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthService {

    private List<User> users;
    private final DataManager dataManager;
    private User currentUser;
    private long lastActivityTime;

    // tracks failed login attempts per username for brute force protection
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, Long> lockoutTimes = new HashMap<>();

    public AuthService(DataManager dataManager) {
        this.dataManager = dataManager;
        this.users = dataManager.loadUsers();
        this.currentUser = null;
    }

    public boolean register(String username, String password, String role, String name, String email) {
        if (findUser(username) != null) return false;

        // hash the password so it isnt stored as plaintext
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        users.add(new User(username, hashed, role, name, email));
        dataManager.saveUsers(users);
        return true;
    }

    // returns a message describing the login result
    public String login(String username, String password) {
        if (isLockedOut(username)) {
            long remaining = getRemainingLockoutSeconds(username);
            return "Account locked. Try again in " + remaining + " seconds.";
        }

        User user = findUser(username);
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            recordFailedAttempt(username);
            int left = Constants.MAX_LOGIN_ATTEMPTS - getFailedAttempts(username);
            if (left <= 0) return "Account locked. Too many failed attempts.";
            return "Invalid username or password. " + left + " attempts remaining.";
        }

        // successful login resets attempts and starts the session
        failedAttempts.remove(username);
        lockoutTimes.remove(username);
        currentUser = user;
        refreshActivity();
        return null;
    }

    public void logout() {
        currentUser = null;
    }

    // call this on any user interaction to keep the session alive
    public void refreshActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    // checks if the session has been idle too long
    public boolean isSessionExpired() {
        if (currentUser == null) return true;
        return System.currentTimeMillis() - lastActivityTime > Constants.SESSION_TIMEOUT_MS;
    }

    private boolean isLockedOut(String username) {
        Long lockTime = lockoutTimes.get(username);
        if (lockTime == null) return false;
        if (System.currentTimeMillis() - lockTime > Constants.LOCKOUT_DURATION_MS) {
            // lockout expired, reset
            lockoutTimes.remove(username);
            failedAttempts.remove(username);
            return false;
        }
        return true;
    }

    private long getRemainingLockoutSeconds(String username) {
        Long lockTime = lockoutTimes.get(username);
        if (lockTime == null) return 0;
        long elapsed = System.currentTimeMillis() - lockTime;
        return Math.max(0, (Constants.LOCKOUT_DURATION_MS - elapsed) / 1000);
    }

    private void recordFailedAttempt(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        if (attempts >= Constants.MAX_LOGIN_ATTEMPTS) {
            lockoutTimes.put(username, System.currentTimeMillis());
        }
    }

    private int getFailedAttempts(String username) {
        return failedAttempts.getOrDefault(username, 0);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public List<User> getUsers() {
        return users;
    }

    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }

    public boolean removeUser(String username) {
        User user = findUser(username);
        if (user != null) {
            users.remove(user);
            dataManager.saveUsers(users);
            return true;
        }
        return false;
    }
}
