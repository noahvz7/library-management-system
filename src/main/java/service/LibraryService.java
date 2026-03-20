package service;

import model.Book;
import model.Loan;
import util.Constants;
import util.DataManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private List<Book> books;
    private List<Loan> loans;
    private final DataManager dataManager;

    public LibraryService(DataManager dataManager) {
        this.dataManager = dataManager;
        books = dataManager.loadBooks();
        loans = dataManager.loadLoans();
    }

    public boolean borrowBook(String loanId, String bookId, String username) {
        Book book = findBookById(bookId);
        if (book == null || !book.isAvailable()) return false;

        Loan loan = new Loan(loanId, bookId, username, LocalDate.now(), LocalDate.now().plusDays(Constants.LOAN_DURATION_DAYS));
        loans.add(loan);
        book.borrowBook();
        save();
        return true;
    }

    public boolean returnBook(String loanId) {
        Loan loan = findLoanById(loanId);
        if (loan == null || loan.isReturned()) return false;

        Book book = findBookById(loan.getBookId());
        if (book == null) return false;

        loan.markReturned();
        book.returnBook();
        save();
        return true;
    }

    public void addBook(Book book) {
        books.add(book);
        save();
    }

    // only removes if no active loans exist for this book
    public boolean removeBook(String id) {
        Book book = findBookById(id);
        if (book == null) return false;
        if (hasActiveLoans(id)) return false;

        books.remove(book);
        save();
        return true;
    }

    // checks if a book has any unreturned loans
    public boolean hasActiveLoans(String bookId) {
        for (Loan loan : loans) {
            if (loan.getBookId().equals(bookId) && !loan.isReturned()) return true;
        }
        return false;
    }

    // checks if a user has any unreturned loans
    public boolean userHasActiveLoans(String username) {
        for (Loan loan : loans) {
            if (loan.getUsername().equals(username) && !loan.isReturned()) return true;
        }
        return false;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public Book findBookById(String id) {
        for (Book book : books) {
            if (book.getId().equals(id)) return book;
        }
        return null;
    }

    public Loan findLoanById(String loanId) {
        for (Loan loan : loans) {
            if (loan.getLoanId().equals(loanId)) return loan;
        }
        return null;
    }

    public List<Loan> getLoansByUser(String username) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getUsername().equals(username)) result.add(loan);
        }
        return result;
    }

    // loans that are past the due date and not returned yet
    public List<Loan> getOverdueLoans() {
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned() && LocalDate.now().isAfter(loan.getDueDate())) {
                overdue.add(loan);
            }
        }
        return overdue;
    }

    public List<Loan> getOverdueLoansByUser(String username) {
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getUsername().equals(username) && !loan.isReturned() && LocalDate.now().isAfter(loan.getDueDate())) {
                overdue.add(loan);
            }
        }
        return overdue;
    }

    private void save() {
        dataManager.saveBooks(books);
        dataManager.saveLoans(loans);
    }
}
