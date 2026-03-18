package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.Loan;
import service.AuthService;
import service.LibraryService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

// shows overdue loans, admins see all and members see only theirs
public class OverduePane extends VBox {

    private final AuthService auth;
    private final LibraryService library;
    private final TableView<Loan> table;

    public OverduePane(AuthService auth, LibraryService library) {
        this.auth = auth;
        this.library = library;

        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("content-area");

        Label title = new Label("Overdue Loans");
        title.getStyleClass().add("page-title");

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(title, table);
    }

    @SuppressWarnings("unchecked")
    private TableView<Loan> buildTable() {
        TableView<Loan> tv = new TableView<>();

        TableColumn<Loan, String> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLoanId()));
        idCol.setPrefWidth(80);

        TableColumn<Loan, String> bookCol = new TableColumn<>("Book ID");
        bookCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookId()));
        bookCol.setPrefWidth(80);

        TableColumn<Loan, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        userCol.setPrefWidth(100);

        TableColumn<Loan, String> dueCol = new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));
        dueCol.setPrefWidth(100);

        // calculates how many days past the due date
        TableColumn<Loan, String> daysCol = new TableColumn<>("Days Overdue");
        daysCol.setCellValueFactory(data -> {
            long days = ChronoUnit.DAYS.between(data.getValue().getDueDate(), LocalDate.now());
            return new SimpleStringProperty(days + " days");
        });
        daysCol.setPrefWidth(100);

        tv.getColumns().addAll(idCol, bookCol, userCol, dueCol, daysCol);
        tv.setPlaceholder(new Label("No overdue loans."));
        return tv;
    }

    public void refresh() {
        List<Loan> overdue;
        if (auth.getCurrentUser().isAdmin()) {
            overdue = library.getOverdueLoans();
        } else {
            overdue = library.getOverdueLoansByUser(auth.getCurrentUser().getUsername());
        }
        table.getItems().setAll(overdue);
    }
}
