package service;

import model.User;
import util.DataManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class AuthService {

    private List<User> users;
    private final DataManager dataManager;
    private User currentUser;

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

    public boolean login(String username, String password) {
        User user = findUser(username);
        if (user == null) return false;

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
