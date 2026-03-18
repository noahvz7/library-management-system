package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.User;
import service.AuthService;
import util.AuditLogger;

// admin-only screen for managing user accounts
public class UsersPane extends VBox {

    private final AuthService auth;
    private final TableView<User> table;

    public UsersPane(AuthService auth) {
        this.auth = auth;

        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("content-area");

        Label title = new Label("Users");
        title.getStyleClass().add("page-title");

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        Button addBtn = new Button("Register User");
        addBtn.getStyleClass().add("action-button");
        addBtn.setOnAction(e -> showRegisterDialog());

        Button removeBtn = new Button("Remove User");
        removeBtn.getStyleClass().add("danger-button");
        removeBtn.setOnAction(e -> removeSelected());

        HBox buttons = new HBox(10, addBtn, removeBtn);
        getChildren().addAll(title, buttons, table);
    }

    @SuppressWarnings("unchecked")
    private TableView<User> buildTable() {
        TableView<User> tv = new TableView<>();

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        usernameCol.setPrefWidth(120);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        roleCol.setPrefWidth(80);

        tv.getColumns().addAll(usernameCol, nameCol, emailCol, roleCol);
        tv.setPlaceholder(new Label("No users registered."));
        return tv;
    }

    // admin can assign any role when registering users from here
    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register User");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField nameField = new TextField();
        TextField emailField = new TextField();

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("MEMBER", "ADMIN");
        roleBox.setValue("MEMBER");

        grid.add(new Label("Username:"), 0, 0);  grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);  grid.add(passwordField, 1, 1);
        grid.add(new Label("Name:"), 0, 2);      grid.add(nameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);     grid.add(emailField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);      grid.add(roleBox, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String role = roleBox.getValue();

                if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
                    showAlert("All fields are required.");
                    return;
                }
                if (password.length() < 4) {
                    showAlert("Password must be at least 4 characters.");
                    return;
                }
                if (!email.contains("@") || !email.contains(".")) {
                    showAlert("Invalid email.");
                    return;
                }

                if (auth.register(username, password, role, name, email)) {
                    AuditLogger.log(auth.getCurrentUser().getUsername(), "REGISTER_USER", username + " role:" + role);
                    refresh();
                } else {
                    showAlert("Username already taken.");
                }
            }
        });
    }

    private void removeSelected() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a user first.");
            return;
        }

        // can't delete yourself
        if (selected.getUsername().equals(auth.getCurrentUser().getUsername())) {
            showAlert("You can't remove your own account.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove user \"" + selected.getUsername() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                auth.removeUser(selected.getUsername());
                AuditLogger.log(auth.getCurrentUser().getUsername(), "REMOVE_USER", selected.getUsername());
                refresh();
            }
        });
    }

    public void refresh() {
        table.getItems().setAll(auth.getUsers());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }
}
