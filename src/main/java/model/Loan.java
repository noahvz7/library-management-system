package model;

import java.time.LocalDate;

public class Loan {

    private String loanId;
    private String bookId;
    private String username;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public Loan(String loanId, String bookId, String username, LocalDate borrowDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.bookId = bookId;
        this.username = username;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = false;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markReturned() {
        this.returned = true;
    }

    @Override
    public String toString() {
        String status = returned ? "Returned" : (LocalDate.now().isAfter(dueDate) ? "OVERDUE" : "Active");
        return String.format("[%s] LoanID: %s | Book: %s | User: %s | Borrowed: %s | Due: %s",
                status, loanId, bookId, username, borrowDate, dueDate);
    }
}
