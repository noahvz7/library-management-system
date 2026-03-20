package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import service.AuthService;
import service.LibraryService;
import util.AuditLogger;

// main screen after login, sidebar on the left and content on the right
public class Dashboard extends BorderPane {

    private final AuthService auth;
    private final LibraryService library;
    private final Stage stage;
    private final VBox sidebar;
    private Button activeButton;
    private Button booksButton;

    private final BooksPane booksPane;
    private final LoansPane loansPane;
    private final UsersPane usersPane;
    private final OverduePane overduePane;

    public Dashboard(AuthService auth, LibraryService library, Stage stage) {
        this.auth = auth;
        this.library = library;
        this.stage = stage;

        booksPane = new BooksPane(auth, library);
        loansPane = new LoansPane(auth, library);
        usersPane = new UsersPane(auth, library);
        overduePane = new OverduePane(auth, library);

        sidebar = buildSidebar();
        setLeft(sidebar);
        setTop(buildTopBar());

        // default to the books page on login
        showBooks();

        // reset the inactivity timer on any mouse or key event
        setOnMouseMoved(e -> auth.refreshActivity());
        setOnKeyPressed(e -> auth.refreshActivity());

        startSessionTimer();
    }

    // checks every 30 seconds if the session has expired
    private void startSessionTimer() {
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (auth.isSessionExpired()) {
                AuditLogger.log(auth.getCurrentUser().getUsername(), "SESSION_TIMEOUT", "auto-logout");
                auth.logout();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Session expired due to inactivity.");
                alert.showAndWait();

                LoginScreen loginScreen = new LoginScreen(auth, library, stage);
                Scene scene = new Scene(loginScreen, 900, 600);
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(scene);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private VBox buildSidebar() {
        VBox box = new VBox(1);
        box.getStyleClass().add("sidebar");

        Label brand = new Label("Library System");
        brand.getStyleClass().add("sidebar-brand");

        Label browseLabel = new Label("BROWSE");
        browseLabel.getStyleClass().add("sidebar-title");

        booksButton = createNavButton("Books", e -> showBooks());
        Button loansBtn = createNavButton("My Loans", e -> showLoans());
        Button overdueBtn = createNavButton("Overdue", e -> showOverdue());

        box.getChildren().addAll(brand, browseLabel, booksButton, loansBtn, overdueBtn);

        // admin gets extra nav items below a divider
        if (auth.getCurrentUser().isAdmin()) {
            loansBtn.setText("All Loans");

            Region divider = new Region();
            divider.getStyleClass().add("sidebar-divider");
            divider.setPrefWidth(170);

            Label adminLabel = new Label("MANAGE");
            adminLabel.getStyleClass().add("sidebar-title");

            Button usersBtn = createNavButton("Users", e -> showUsers());
            box.getChildren().addAll(divider, adminLabel, usersBtn);
        }

        return box;
    }

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_RIGHT);

        String name = auth.getCurrentUser().getName();
        String role = auth.getCurrentUser().getRole();
        Label userLabel = new Label(name + " (" + role + ")");
        userLabel.getStyleClass().add("user-label");

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setOnAction(e -> logout());

        // pushes logout button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(userLabel, spacer, logoutBtn);
        return bar;
    }

    private Button createNavButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-button");
        btn.setOnAction(e -> {
            handler.handle(e);
            setActiveButton(btn);
        });
        return btn;
    }

    // highlights the currently selected nav button
    private void setActiveButton(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-active");
        }
        btn.getStyleClass().add("sidebar-active");
        activeButton = btn;
    }

    private void showBooks() {
        booksPane.refresh();
        setCenter(booksPane);
        setActiveButton(booksButton);
    }

    private void showLoans() {
        loansPane.refresh();
        setCenter(loansPane);
    }

    private void showOverdue() {
        overduePane.refresh();
        setCenter(overduePane);
    }

    private void showUsers() {
        usersPane.refresh();
        setCenter(usersPane);
    }

    private void logout() {
        AuditLogger.log(auth.getCurrentUser().getUsername(), "LOGOUT", "session ended");
        auth.logout();

        LoginScreen loginScreen = new LoginScreen(auth, library, stage);
        Scene scene = new Scene(loginScreen, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }
}
