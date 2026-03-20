package ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import service.AuthService;
import service.LibraryService;
import util.AuditLogger;
import util.Validator;

// first screen shown, handles login and self-registration
public class LoginScreen extends StackPane {

    private final AuthService auth;
    private final LibraryService library;
    private final Stage stage;
    private Label messageLabel;

    public LoginScreen(AuthService auth, LibraryService library, Stage stage) {
        this.auth = auth;
        this.library = library;
        this.stage = stage;

        // first launch with no users goes straight to admin setup
        if (!auth.hasUsers()) {
            getChildren().add(buildSetupCard());
        } else {
            getChildren().add(buildLoginCard());
        }
    }

    // forces creation of the first admin account
    private VBox buildSetupCard() {
        VBox card = createCard("First Time Setup", "Create an admin account to get started.");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("error-label");

        Button createBtn = new Button("Create Admin Account");
        createBtn.getStyleClass().add("action-button");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> {
            String msg = validateAndRegister(usernameField.getText(), passwordField.getText(), nameField.getText(), emailField.getText(), "ADMIN");
            if (msg == null) {
                getChildren().clear();
                getChildren().add(buildLoginCard());
            } else {
                messageLabel.setText(msg);
            }
        });

        card.getChildren().addAll(usernameField, passwordField, nameField, emailField, messageLabel, createBtn);
        return card;
    }

    private VBox buildLoginCard() {
        VBox card = createCard("Library Management System", "Sign in to continue");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("error-label");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("action-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Fields cannot be empty.");
                return;
            }

            // login now returns an error message or null on success
            String error = auth.login(username, password);
            if (error == null) {
                AuditLogger.log(username, "LOGIN", "session started");
                openDashboard();
            } else {
                AuditLogger.log(username, "FAILED_LOGIN", "invalid credentials");
                messageLabel.setText(error);
            }
        });

        // enter key submits the form
        passwordField.setOnAction(e -> loginBtn.fire());

        Hyperlink createLink = new Hyperlink("Create an account");
        createLink.setOnAction(e -> {
            getChildren().clear();
            getChildren().add(buildRegisterCard());
        });

        card.getChildren().addAll(usernameField, passwordField, messageLabel, loginBtn, createLink);
        return card;
    }

    // self-registration is locked to MEMBER only, admins are created from the dashboard
    private VBox buildRegisterCard() {
        VBox card = createCard("Create Account", "Member accounts only. Contact an admin for elevated access.");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        messageLabel = new Label();
        messageLabel.getStyleClass().add("error-label");

        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("action-button");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            String msg = validateAndRegister(usernameField.getText(), passwordField.getText(), nameField.getText(), emailField.getText(), "MEMBER");
            if (msg == null) {
                getChildren().clear();
                getChildren().add(buildLoginCard());
            } else {
                messageLabel.setText(msg);
            }
        });

        Hyperlink backLink = new Hyperlink("Back to login");
        backLink.setOnAction(e -> {
            getChildren().clear();
            getChildren().add(buildLoginCard());
        });

        card.getChildren().addAll(usernameField, passwordField, nameField, emailField, messageLabel, registerBtn, backLink);
        return card;
    }

    // returns an error message or null on success
    private String validateAndRegister(String username, String password, String name, String email, String role) {
        username = username.trim();
        password = password.trim();
        name = name.trim();
        email = email.trim();

        // run all validations through the centralized validator
        String error = Validator.validateRegistration(username, password, name, email);
        if (error != null) return error;

        if (!auth.register(username, password, role, name, email)) {
            return "Username already taken.";
        }

        AuditLogger.log(username, "REGISTER", "role:" + role);
        return null;
    }

    private VBox createCard(String title, String subtitle) {
        VBox card = new VBox(10);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(360);
        card.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("login-title");
        card.getChildren().add(titleLabel);

        if (subtitle != null) {
            Label subLabel = new Label(subtitle);
            subLabel.getStyleClass().add("login-subtitle");
            card.getChildren().add(subLabel);
        }

        setAlignment(Pos.CENTER);
        return card;
    }

    private void openDashboard() {
        Dashboard dashboard = new Dashboard(auth, library, stage);
        Scene scene = new Scene(dashboard, 1000, 650);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }
}
