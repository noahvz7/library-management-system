package model;

// a library user, can be an admin or a member
public class User {

    private String username;
    private String passwordHash;
    private String role;
    private String name;
    private String email;

    public User(String username, String passwordHash, String role, String name, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    @Override
    public String toString() {
        return String.format("Username: %s | Name: %s | Email: %s | Role: %s", username, name, email, role);
    }
}
