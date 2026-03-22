package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.Book;
import model.User;
import service.AuthService;
import service.LibraryService;
import util.AuditLogger;
import util.Constants;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// displays the books table with borrow, add, and remove actions
public class BooksPane extends VBox {

    private final AuthService auth;
    private final LibraryService library;
    private final TableView<Book> table;
    private final TextField searchField;

    public BooksPane(AuthService auth, LibraryService library) {
        this.auth = auth;
        this.library = library;

        setSpacing(15);
        setPadding(new Insets(20));
        getStyleClass().add("content-area");

        Label title = new Label("Books");
        title.getStyleClass().add("page-title");

        // search bar filters by title or author user types
        searchField = new TextField();
        searchField.setPromptText("Search by title or author...");
        searchField.setMaxWidth(300);
        searchField.textProperty().addListener((obs, old, text) -> applySearch(text));

        HBox topRow = new HBox(15, title, searchField);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox buttons = buildButtons();
        getChildren().addAll(topRow, buttons, table);
    }

    @SuppressWarnings("unchecked")
    private TableView<Book> buildTable() {
        TableView<Book> tv = new TableView<>();

        TableColumn<Book, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        idCol.setPrefWidth(80);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(200);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        authorCol.setPrefWidth(150);

        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));
        genreCol.setPrefWidth(120);

        TableColumn<Book, String> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getPublicationYear())));
        yearCol.setPrefWidth(60);

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().isAvailable() ? "Available" : "Borrowed";
            return new SimpleStringProperty(status);
        });
        statusCol.setPrefWidth(90);

        // colors the status text based on availability
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-available", "status-borrowed");
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status);
                    getStyleClass().add(status.equals("Available") ? "status-available" : "status-borrowed");
                }
            }
        });

        tv.getColumns().addAll(idCol, titleCol, authorCol, genreCol, yearCol, statusCol);
        tv.setPlaceholder(new Label("No books in the library."));

        // click empty space to deselect
        tv.setRowFactory(t -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (row.isEmpty()) tv.getSelectionModel().clearSelection();
            });
            return row;
        });

        return tv;
    }

    private HBox buildButtons() {
        HBox box = new HBox(10);

        Button borrowBtn = new Button("Borrow");
        borrowBtn.getStyleClass().add("action-button");
        borrowBtn.setOnAction(e -> borrowSelected());
        box.getChildren().add(borrowBtn);

        // only admins can add or remove books
        if (auth.getCurrentUser().isAdmin()) {
            Button addBtn = new Button("Add Book");
            addBtn.getStyleClass().add("action-button");
            addBtn.setOnAction(e -> showAddDialog());

            Button removeBtn = new Button("Remove Book");
            removeBtn.getStyleClass().add("danger-button");
            removeBtn.setOnAction(e -> removeSelected());

            box.getChildren().addAll(addBtn, removeBtn);
        }

        return box;
    }

    private void borrowSelected() {
        Book selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book first.");
            return;
        }
        if (!selected.isAvailable()) {
            showAlert("That book is already borrowed.");
            return;
        }

        // generate a unique loan id
        String prefix = Constants.LOAN_ID_PREFIX;
        String loanId = prefix + String.format("%04d", library.getLoans().size() + 1);
        while (library.findLoanById(loanId) != null) {
            loanId = prefix + String.format("%04d", Integer.parseInt(loanId.substring(prefix.length())) + 1);
        }

        // admins pick from a dropdown of registered users
        String borrower;
        if (auth.getCurrentUser().isAdmin()) {
            List<String> usernames = auth.getUsers().stream().map(User::getUsername).collect(Collectors.toList());

            if (usernames.isEmpty()) {
                showAlert("No users registered.");
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(usernames.get(0), usernames);
            dialog.setTitle("Borrow Book");
            dialog.setHeaderText("Borrowing: " + selected.getTitle());
            dialog.setContentText("Select borrower:");
            var result = dialog.showAndWait();
            if (result.isEmpty()) return;
            borrower = result.get();
        } else {
            borrower = auth.getCurrentUser().getUsername();
        }

        if (library.borrowBook(loanId, selected.getId(), borrower)) {
            AuditLogger.log(auth.getCurrentUser().getUsername(), "BORROW", "loan:" + loanId + " book:" + selected.getId() + " for:" + borrower);
            refresh();
        } else {
            showAlert("Could not borrow.");
        }
    }

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Book");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField();
        idField.setPromptText("e.g. B001");
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField genreField = new TextField();
        TextField yearField = new TextField();
        yearField.setPromptText("e.g. 2020");

        grid.add(new Label("Book ID:"), 0, 0);  grid.add(idField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);     grid.add(titleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);    grid.add(authorField, 1, 2);
        grid.add(new Label("Genre:"), 0, 3);     grid.add(genreField, 1, 3);
        grid.add(new Label("Year:"), 0, 4);      grid.add(yearField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String id = idField.getText().trim();
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String genre = genreField.getText().trim();
                String yearText = yearField.getText().trim();

                if (id.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty() || yearText.isEmpty()) {
                    showAlert("All fields are required.");
                    return;
                }
                if (library.findBookById(id) != null) {
                    showAlert("That ID already exists.");
                    return;
                }

                int year;
                try {
                    year = Integer.parseInt(yearText);
                } catch (NumberFormatException ex) {
                    showAlert("Year must be a number.");
                    return;
                }
                int currentYear = LocalDate.now().getYear();
                if (year < 1 || year > currentYear) {
                    showAlert("Year must be between 1 and " + currentYear + ".");
                    return;
                }

                library.addBook(new Book(id, title, author, genre, year));
                AuditLogger.log(auth.getCurrentUser().getUsername(), "ADD_BOOK", id);
                refresh();
            }
        });
    }

    private void removeSelected() {
        Book selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a book first.");
            return;
        }

        // cant remove books that are currently borrowed
        if (library.hasActiveLoans(selected.getId())) {
            showAlert("Cannot remove a book with active loans.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remove \"" + selected.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                library.removeBook(selected.getId());
                AuditLogger.log(auth.getCurrentUser().getUsername(), "REMOVE_BOOK", selected.getId());
                refresh();
            }
        });
    }

    // filters the table by title or author
    private void applySearch(String query) {
        if (query == null || query.isEmpty()) {
            table.getItems().setAll(library.getBooks());
            return;
        }

        String lower = query.toLowerCase();
        List<Book> filtered = library.getBooks().stream().filter(b -> b.getTitle().toLowerCase().contains(lower) || b.getAuthor().toLowerCase().contains(lower)).collect(Collectors.toList());
        table.getItems().setAll(filtered);
    }

    public void refresh() {
        applySearch(searchField.getText());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
    }
}
