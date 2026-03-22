package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.Book;
import model.Loan;
import service.AuthService;
import service.LibraryService;
import util.AuditLogger;

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

        Button returnBtn = new Button("Return Book");
        returnBtn.getStyleClass().add("action-button");
        returnBtn.setOnAction(e -> returnSelected());

        HBox buttons = new HBox(10, returnBtn);
        getChildren().addAll(title, buttons, table);
    }

    @SuppressWarnings("unchecked")
    private TableView<Loan> buildTable() {
        TableView<Loan> tv = new TableView<>();

        TableColumn<Loan, String> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLoanId()));
        idCol.setPrefWidth(80);

        TableColumn<Loan, String> bookCol = new TableColumn<>("Book");
        bookCol.setCellValueFactory(data -> {
            Book book = library.findBookById(data.getValue().getBookId());
            String display = book != null ? book.getTitle() : data.getValue().getBookId();
            return new SimpleStringProperty(display);
        });
        bookCol.setPrefWidth(180);

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

        tv.setRowFactory(t -> {
            TableRow<Loan> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (row.isEmpty()) tv.getSelectionModel().clearSelection();
            });
            return row;
        });

        return tv;
    }

    private void returnSelected() {
        Loan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a loan first.");
            return;
        }

        // members can only return their own
        if (!auth.getCurrentUser().isAdmin() && !selected.getUsername().equals(auth.getCurrentUser().getUsername())) {
            showAlert("You can only return your own loans.");
            return;
        }

        if (library.returnBook(selected.getLoanId())) {
            AuditLogger.log(auth.getCurrentUser().getUsername(), "RETURN", "loan:" + selected.getLoanId());
            refresh();
        } else {
            showAlert("Could not return.");
        }
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }
}
