import model.Book;
import model.Member;
import model.Loan;
import service.AuthService;
import service.LibraryService;
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

        // if no users exist yet, prompt to create the first admin account
        if (!auth.hasUsers()) {
            System.out.println("No accounts found. Create an admin account to get started.");
            createAccount("ADMIN");
        }

        // login loop, user must authenticate before accessing the system
        while (!auth.isLoggedIn()) {
            loginMenu();
        }

        System.out.println("Welcome, " + auth.getCurrentUser().getUsername() + ".");

        // main menu loop
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addBook(); break;
                case "2": removeBook(); break;
                case "3": listBooks(); break;
                case "4": registerMember(); break;
                case "5": removeMember(); break;
                case "6": listMembers(); break;
                case "7": borrowBook(); break;
                case "8": returnBook(); break;
                case "9": listLoans(); break;
                case "10": listOverdue(); break;
                case "0":
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

    // authentication

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

        if (auth.register(username, password, role)) {
            System.out.println("Account created.");
        } else {
            System.out.println("Username already taken.");
        }
    }

    // main menu

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. List Books");
        System.out.println("4. Register Member");
        System.out.println("5. Remove Member");
        System.out.println("6. List Members");
        System.out.println("7. Borrow Book");
        System.out.println("8. Return Book");
        System.out.println("9. List Loans");
        System.out.println("10. View Overdue");
        System.out.println("0. Exit");
        System.out.print("> ");
    }

    // helper to check if a string is empty or blank

    private static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    // books

    private static void addBook() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();

        // make sure id isnt blank
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

        // basic range check for publication year
        if (year < 0 || year > 2026) {
            System.out.println("Year must be between 0 and 2026.");
            return;
        }

        library.addBook(new Book(id, title, author, genre, year));
        System.out.println("Book added.");
    }

    private static void removeBook() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();

        if (isEmpty(id)) {
            System.out.println("ID cannot be empty.");
            return;
        }

        if (library.removeBook(id)) {
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

    // members

    private static void registerMember() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();

        if (isEmpty(id)) {
            System.out.println("ID cannot be empty.");
            return;
        }

        if (library.findMemberById(id) != null) {
            System.out.println("That ID already exists.");
            return;
        }

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (isEmpty(name)) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (isEmpty(email)) {
            System.out.println("Email cannot be empty.");
            return;
        }

        // basic email check, just needs an @ and a dot after it
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Invalid email format.");
            return;
        }

        library.registerMember(new Member(id, name, email));
        System.out.println("Member registered.");
    }

    private static void removeMember() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();

        if (isEmpty(id)) {
            System.out.println("ID cannot be empty.");
            return;
        }

        if (library.removeMember(id)) {
            System.out.println("Member removed.");
        } else {
            System.out.println("Member not found.");
        }
    }

    private static void listMembers() {
        List<Member> members = library.getMembers();
        if (members.isEmpty()) {
            System.out.println("No members.");
            return;
        }
        for (Member m : members) {
            System.out.println(m);
        }
    }

    // loans

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

        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();
        if (isEmpty(memberId)) {
            System.out.println("Member ID cannot be empty.");
            return;
        }

        if (library.borrowBook(loanId, bookId, memberId)) {
            System.out.println("Book borrowed. Due in 14 days.");
        } else {
            System.out.println("Could not borrow. Check the book/member ID and availability.");
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
            System.out.println("Book returned.");
        } else {
            System.out.println("Could not return. Check the loan ID.");
        }
    }

    private static void listLoans() {
        List<Loan> loans = library.getLoans();
        if (loans.isEmpty()) {
            System.out.println("No loans.");
            return;
        }
        for (Loan l : loans) {
            System.out.println(l);
        }
    }

    // shows only loans that are past due and not yet returned
    private static void listOverdue() {
        List<Loan> overdue = library.getOverdueLoans();
        if (overdue.isEmpty()) {
            System.out.println("No overdue loans.");
            return;
        }
        System.out.println("Overdue loans:");
        for (Loan l : overdue) {
            System.out.println(l);
        }
    }
}
