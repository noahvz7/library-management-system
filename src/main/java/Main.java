import model.Book;
import model.Loan;
import model.User;
import service.AuthService;
import service.LibraryService;
import util.AuditLogger;
import util.DataManager;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final DataManager dataManager = new DataManager();
    private static final LibraryService library = new LibraryService(dataManager);
    private static final AuthService auth = new AuthService(dataManager);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Library Management System");
        System.out.println("------------------------");

        // first time setup
        if (!auth.hasUsers()) {
            System.out.println("No accounts found. Create an admin account to get started.");
            createAccount("ADMIN");
        }

        while (!auth.isLoggedIn()) {
            loginMenu();
        }

        String user = auth.getCurrentUser().getUsername();
        System.out.println("Welcome, " + user + ".");
        AuditLogger.log(user, "LOGIN", "session started");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addBook(); break;
                case "2": removeBook(); break;
                case "3": listBooks(); break;
                case "4": registerUser(); break;
                case "5": removeUser(); break;
                case "6": listUsers(); break;
                case "7": borrowBook(); break;
                case "8": returnBook(); break;
                case "9": listLoans(); break;
                case "10": listOverdue(); break;
                case "0":
                    AuditLogger.log(user, "LOGOUT", "session ended");
                    auth.logout();
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        scanner.close();
    }

    private static void loginMenu() {
        System.out.println("\n1. Login");
        System.out.println("2. Create Account");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            login();
        } else if (choice.equals("2")) {
            System.out.println("Select role: 1. Admin  2. Member");
            System.out.print("> ");
            String roleChoice = scanner.nextLine().trim();
            String role = roleChoice.equals("1") ? "ADMIN" : "MEMBER";
            createAccount(role);
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (isEmpty(username) || isEmpty(password)) {
            System.out.println("Fields cannot be empty.");
            return;
        }

        if (auth.login(username, password)) {
            System.out.println("Login successful.");
        } else {
            AuditLogger.log(username, "FAILED_LOGIN", "invalid credentials");
            System.out.println("Invalid username or password.");
        }
    }

    private static void createAccount(String role) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (isEmpty(username) || isEmpty(password)) {
            System.out.println("Fields cannot be empty.");
            return;
        }

        if (password.length() < 4) {
            System.out.println("Password must be at least 4 characters.");
            return;
        }

        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        if (isEmpty(name)) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (isEmpty(email) || !email.contains("@") || !email.contains(".")) {
            System.out.println("Invalid email.");
            return;
        }

        if (auth.register(username, password, role, name, email)) {
            System.out.println("Account created.");
            AuditLogger.log(username, "REGISTER", "role:" + role);
        } else {
            System.out.println("Username already taken.");
        }
    }

    // blocks non-admin users from calling this action
    private static boolean requireAdmin() {
        if (!auth.getCurrentUser().isAdmin()) {
            System.out.println("Access denied. Admin only.");
            return false;
        }
        return true;
    }

    private static String currentUser() {
        return auth.getCurrentUser().getUsername();
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Add Book (admin)");
        System.out.println("2. Remove Book (admin)");
        System.out.println("3. List Books");
        System.out.println("4. Register User (admin)");
        System.out.println("5. Remove User (admin)");
        System.out.println("6. List Users (admin)");
        System.out.println("7. Borrow Book");
        System.out.println("8. Return Book");
        System.out.println("9. List Loans");
        System.out.println("10. View Overdue");
        System.out.println("0. Exit");
        System.out.print("> ");
    }

    private static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    private static void addBook() {
        if (!requireAdmin()) return;

        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();

        if (isEmpty(id)) {
            System.out.println("ID cannot be empty.");
            return;
        }

        if (library.findBookById(id) != null) {
            System.out.println("That ID already exists.");
            return;
        }

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (isEmpty(title)) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        if (isEmpty(author)) {
            System.out.println("Author cannot be empty.");
            return;
        }

        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        if (isEmpty(genre)) {
            System.out.println("Genre cannot be empty.");
            return;
        }

        System.out.print("Year: ");
        String yearInput = scanner.nextLine().trim();

        int year;
        try {
            year = Integer.parseInt(yearInput);
        } catch (NumberFormatException e) {
            System.out.println("Year must be a number.");
            return;
        }

        if (year < 0 || year > 2026) {
            System.out.println("Year must be between 0 and 2026.");
            return;
        }

        library.addBook(new Book(id, title, author, genre, year));
        AuditLogger.log(currentUser(), "ADD_BOOK", id);
        System.out.println("Book added.");
    }

    private static void removeBook() {
        if (!requireAdmin()) return;

        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();

        if (isEmpty(id)) {
            System.out.println("ID cannot be empty.");
            return;
        }

        if (library.removeBook(id)) {
            AuditLogger.log(currentUser(), "REMOVE_BOOK", id);
            System.out.println("Book removed.");
        } else {
            System.out.println("Book not found.");
        }
    }

    private static void listBooks() {
        List<Book> books = library.getBooks();
        if (books.isEmpty()) {
            System.out.println("No books.");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static void registerUser() {
        if (!requireAdmin()) return;

        System.out.println("Select role: 1. Admin  2. Member");
        System.out.print("> ");
        String roleChoice = scanner.nextLine().trim();
        String role = roleChoice.equals("1") ? "ADMIN" : "MEMBER";

        createAccount(role);
    }

    private static void removeUser() {
        if (!requireAdmin()) return;

        System.out.print("Username to remove: ");
        String username = scanner.nextLine().trim();

        if (isEmpty(username)) {
            System.out.println("Username cannot be empty.");
            return;
        }

        // cant delete yourself
        if (username.equals(currentUser())) {
            System.out.println("You can't remove your own account.");
            return;
        }

        if (auth.removeUser(username)) {
            AuditLogger.log(currentUser(), "REMOVE_USER", username);
            System.out.println("User removed.");
        } else {
            System.out.println("User not found.");
        }
    }

    private static void listUsers() {
        if (!requireAdmin()) return;

        List<User> users = auth.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users.");
            return;
        }
        for (User u : users) {
            System.out.println(u);
        }
    }

    private static void borrowBook() {
        System.out.print("Loan ID: ");
        String loanId = scanner.nextLine().trim();

        if (isEmpty(loanId)) {
            System.out.println("Loan ID cannot be empty.");
            return;
        }

        if (library.findLoanById(loanId) != null) {
            System.out.println("That loan ID already exists.");
            return;
        }

        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        if (isEmpty(bookId)) {
            System.out.println("Book ID cannot be empty.");
            return;
        }

        // admins pick who borrows, members borrow for themselves
        String borrower;
        if (auth.getCurrentUser().isAdmin()) {
            System.out.print("Username of borrower: ");
            borrower = scanner.nextLine().trim();
            if (isEmpty(borrower)) {
                System.out.println("Username cannot be empty.");
                return;
            }
            if (auth.findUser(borrower) == null) {
                System.out.println("User not found.");
                return;
            }
        } else {
            borrower = currentUser();
        }

        if (library.borrowBook(loanId, bookId, borrower)) {
            AuditLogger.log(currentUser(), "BORROW", "loan:" + loanId + " book:" + bookId + " for:" + borrower);
            System.out.println("Book borrowed. Due in 14 days.");
        } else {
            System.out.println("Could not borrow. Check the book ID and availability.");
        }
    }

    private static void returnBook() {
        listLoans();
        System.out.print("Loan ID to return: ");
        String loanId = scanner.nextLine().trim();

        if (isEmpty(loanId)) {
            System.out.println("Loan ID cannot be empty.");
            return;
        }

        if (library.returnBook(loanId)) {
            AuditLogger.log(currentUser(), "RETURN", "loan:" + loanId);
            System.out.println("Book returned.");
        } else {
            System.out.println("Could not return. Check the loan ID.");
        }
    }

    private static void listLoans() {
        List<Loan> loans;
        if (auth.getCurrentUser().isAdmin()) {
            loans = library.getLoans();
        } else {
            loans = library.getLoansByUser(currentUser());
        }

        if (loans.isEmpty()) {
            System.out.println("No loans.");
            return;
        }
        for (Loan l : loans) {
            System.out.println(l);
        }
    }

    private static void listOverdue() {
        List<Loan> overdue;
        if (auth.getCurrentUser().isAdmin()) {
            overdue = library.getOverdueLoans();
        } else {
            overdue = library.getOverdueLoansByUser(currentUser());
        }

        if (overdue.isEmpty()) {
            System.out.println("No overdue loans.");
            return;
        }
        for (Loan l : overdue) {
            System.out.println(l);
        }
    }
}
