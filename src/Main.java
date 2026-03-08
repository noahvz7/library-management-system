import model.Book;
import model.Member;
import model.Loan;
import service.LibraryService;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final LibraryService library = new LibraryService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Library Management System");
        System.out.println("------------------------");

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
                case "0":
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }

        scanner.close();
    }

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
        System.out.println("0. Exit");
        System.out.print("> ");
    }

    // books

    private static void addBook() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();

        if (library.findBookById(id) != null) {
            System.out.println("That ID already exists.");
            return;
        }

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Year: ");
        String yearInput = scanner.nextLine().trim();

        int year;
        try {
            year = Integer.parseInt(yearInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid year.");
            return;
        }

        library.addBook(new Book(id, title, author, genre, year));
        System.out.println("Book added.");
    }

    private static void removeBook() {
        System.out.print("Book ID: ");
        String id = scanner.nextLine().trim();

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

        if (library.findMemberById(id) != null) {
            System.out.println("That ID already exists.");
            return;
        }

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        library.registerMember(new Member(id, name, email));
        System.out.println("Member registered.");
    }

    private static void removeMember() {
        System.out.print("Member ID: ");
        String id = scanner.nextLine().trim();

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

        if (library.findLoanById(loanId) != null) {
            System.out.println("That loan ID already exists.");
            return;
        }

        System.out.print("Book ID: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Member ID: ");
        String memberId = scanner.nextLine().trim();

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
}
