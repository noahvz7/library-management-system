import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import service.AuthService;
import service.LibraryService;
import util.DataManager;
import ui.LoginScreen;

public class Main extends Application {

    // shared services, same ones used across all screens
    private static final DataManager dataManager = new DataManager();
    private static final LibraryService library = new LibraryService(dataManager);
    private static final AuthService auth = new AuthService(dataManager);

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Library Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        showLogin();
        stage.show();
    }

    public void showLogin() {
        LoginScreen loginScreen = new LoginScreen(auth, library, stage);
        Scene scene = new Scene(loginScreen, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
