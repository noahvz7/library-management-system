package service;

import model.Book;
import model.Member;
import model.Loan;
import util.DataManager;

import java.time.LocalDate;
import java.util.List;

public class LibraryService {

    private List<Book> books;
    private List<Member> members;
    private List<Loan> loans;
    private final DataManager dataManager;

    public LibraryService() {
        dataManager = new DataManager();
        books = dataManager.loadBooks();
        members = dataManager.loadMembers();
        loans = dataManager.loadLoans();
    }

    public boolean borrowBook(String loanId, String bookId, String memberId) {
        Book book = findBookById(bookId);
        Member member = findMemberById(memberId);

        if (book == null || member == null) return false;
        if (!book.isAvailable()) return false;

        Loan loan = new Loan(loanId, bookId, memberId, LocalDate.now(), LocalDate.now().plusDays(14));
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

    public void registerMember(Member member) {
        members.add(member);
        save();
    }

    public boolean removeBook(String id) {
        Book book = findBookById(id);
        if (book != null) {
            books.remove(book);
            save();
            return true;
        }
        return false;
    }

    public boolean removeMember(String memberId) {
        Member member = findMemberById(memberId);
        if (member != null) {
            members.remove(member);
            save();
            return true;
        }
        return false;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Member> getMembers() {
        return members;
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

    public Member findMemberById(String memberId) {
        for (Member member : members) {
            if (member.getMemberId().equals(memberId)) return member;
        }
        return null;
    }

    public Loan findLoanById(String loanId) {
        for (Loan loan : loans) {
            if (loan.getLoanId().equals(loanId)) return loan;
        }
        return null;
    }

    // saves all collections to disk
    private void save() {
        dataManager.saveBooks(books);
        dataManager.saveMembers(members);
        dataManager.saveLoans(loans);
    }
}
