package util;

// validates user input, returns an error message or null if valid
public class Validator {

    // only allows letters, numbers, underscores, and hyphens
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]+$";

    public static String validateUsername(String username) {
        if (username == null || username.isEmpty()) return "Username is required.";
        if (username.length() < Constants.USERNAME_MIN) return "Username must be at least " + Constants.USERNAME_MIN + " characters.";
        if (username.length() > Constants.USERNAME_MAX) return "Username cannot exceed " + Constants.USERNAME_MAX + " characters.";
        if (!username.matches(USERNAME_PATTERN)) return "Username can only contain letters, numbers, underscores, and hyphens.";
        return null;
    }

    // requires at least one uppercase letter and one number
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Password is required.";
        if (password.length() < Constants.PASSWORD_MIN) return "Password must be at least " + Constants.PASSWORD_MIN + " characters.";
        if (password.length() > Constants.PASSWORD_MAX) return "Password cannot exceed " + Constants.PASSWORD_MAX + " characters.";
        if (!password.matches(".*[A-Z].*")) return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[0-9].*")) return "Password must contain at least one number.";
        return null;
    }

    public static String validateName(String name) {
        if (name == null || name.isEmpty()) return "Name is required.";
        if (name.length() > Constants.NAME_MAX) return "Name cannot exceed " + Constants.NAME_MAX + " characters.";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.isEmpty()) return "Email is required.";
        if (email.length() > Constants.EMAIL_MAX) return "Email cannot exceed " + Constants.EMAIL_MAX + " characters.";
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) return "Invalid email format.";
        return null;
    }

    // runs all registration validations in order, returns the first error found
    public static String validateRegistration(String username, String password, String name, String email) {
        String error;
        error = validateUsername(username);
        if (error != null) return error;
        error = validatePassword(password);
        if (error != null) return error;
        error = validateName(name);
        if (error != null) return error;
        error = validateEmail(email);
        if (error != null) return error;
        return null;
    }
}
