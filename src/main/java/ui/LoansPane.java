package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.Loan;
import service.AuthService;
import service.LibraryService;
import util.AuditLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// admins see all loans, members only see their own
public class LoansPane extends VBox {

    private final AuthService auth;
    private final LibraryService library;
    private final TableView<Loan> table;
    private final ComboBox<String> filterBox;

    public LoansPane(AuthService auth, LibraryService library) {
        this.auth = auth;
        this.library = library;

        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("content-area");

        Label title = new Label(auth.getCurrentUser().isAdmin() ? "All Loans" : "My Loans");
        title.getStyleClass().add("page-title");

        // filter dropdown defaults to active loans since returned ones are rarely needed
        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Active", "Returned", "All");
        filterBox.setValue("Active");
        filterBox.setOnAction(e -> refresh());

        HBox topRow = new HBox(15, title, filterBox);
        topRow.setAlignment(Pos.CENTER_LEFT);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        Button returnBtn = new Button("Return Book");
        returnBtn.getStyleClass().add("action-button");
        returnBtn.setOnAction(e -> returnSelected());

        HBox buttons = new HBox(10, returnBtn);
        getChildren().addAll(topRow, buttons, table);
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

        TableColumn<Loan, String> borrowedCol = new TableColumn<>("Borrowed");
        borrowedCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBorrowDate().toString()));
        borrowedCol.setPrefWidth(100);

        TableColumn<Loan, String> dueCol = new TableColumn<>("Due");
        dueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));
        dueCol.setPrefWidth(100);

        TableColumn<Loan, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            Loan loan = data.getValue();
            String status;
            if (loan.isReturned()) status = "Returned";
            else if (LocalDate.now().isAfter(loan.getDueDate())) status = "OVERDUE";
            else status = "Active";
            return new SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(90);

        // colors the status text
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty ? null : status);
                getStyleClass().removeAll("status-active", "status-overdue", "status-returned");
                if (!empty && status != null) {
                    if (status.equals("OVERDUE")) getStyleClass().add("status-overdue");
                    else if (status.equals("Returned")) getStyleClass().add("status-returned");
                    else getStyleClass().add("status-active");
                }
            }
        });

        tv.getColumns().addAll(idCol, bookCol, userCol, borrowedCol, dueCol, statusCol);
        tv.setPlaceholder(new Label("No loans on record."));
        return tv;
    }

    private void returnSelected() {
        Loan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a loan first.");
            return;
        }
        if (selected.isReturned()) {
            showAlert("That loan is already returned.");
            return;
        }

        // members can only return their own loans
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
        List<Loan> loans;
        if (auth.getCurrentUser().isAdmin()) {
            loans = library.getLoans();
        } else {
            loans = library.getLoansByUser(auth.getCurrentUser().getUsername());
        }

        // apply the filter
        String filter = filterBox.getValue();
        if (filter.equals("Active")) {
            loans = loans.stream().filter(l -> !l.isReturned()).collect(Collectors.toList());
        } else if (filter.equals("Returned")) {
            loans = loans.stream().filter(Loan::isReturned).collect(Collectors.toList());
        }

        table.getItems().setAll(loans);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }
}
