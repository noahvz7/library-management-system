package service;

import model.Book;
import model.Member;
import model.Loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private List<Book> books;
    private List<Member> members;
    private List<Loan> loans;

    public LibraryService() {
        books = new ArrayList<>();
        members = new ArrayList<>();
        loans = new ArrayList<>();
    }

    public boolean borrowBook(String loanId, String bookId, String memberId) {
        Book book = findBookById(bookId);
        Member member = findMemberById(memberId);

        if (book == null || member == null) return false;
        if (!book.isAvailable()) return false;

        Loan loan = new Loan(loanId, bookId, memberId, LocalDate.now(), LocalDate.now().plusDays(14));
        loans.add(loan);
        book.borrowBook();
        return true;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void registerMember(Member member) {
        members.add(member);
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

    public boolean removeBook(String id) {
        Book book = findBookById(id);
        if (book != null) {
            books.remove(book);
            return true;
        }
        return false;
    }

    public boolean removeMember(String memberId) {
        Member member = findMemberById(memberId);
        if (member != null) {
            members.remove(member);
            return true;
        }
        return false;
    }
}