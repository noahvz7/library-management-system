package service;

import model.User;
import util.DataManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

// handles user registration and login
public class AuthService {

    private List<User> users;
    private final DataManager dataManager;
    private User currentUser;

    public AuthService(DataManager dataManager) {
        this.dataManager = dataManager;
        this.users = dataManager.loadUsers();
        this.currentUser = null;
    }

    // creates a new user account with a hashed password
    public boolean register(String username, String password, String role) {
        if (findUser(username) != null) return false;

        // BCrypt.hashpw hashes the password with a random salt, the salt is stored inside the hash string itself
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        users.add(new User(username, hashed, role));
        dataManager.saveUsers(users);
        return true;
    }

    // checks password against the stored hash and logs the user in
    public boolean login(String username, String password) {
        User user = findUser(username);
        if (user == null) return false;

        // BCrypt.checkpw compares plaintext to the hash
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
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

    private User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) return user;
        }
        return null;
    }
}
