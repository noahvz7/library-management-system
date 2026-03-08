package model;

public class Book {

    private String id;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private boolean available;

    public Book(String id, String title, String author, String genre, int publicationYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public boolean isAvailable() {
        return available;
    }

    public void borrowBook() {
        available = false;
    }

    public void returnBook() {
        available = true;
    }

    @Override
    public String toString() {
        String status = available ? "Available" : "Borrowed";
        return String.format("[%s] ID: %s | \"%s\" by %s | Genre: %s | Year: %d", status, id, title, author, genre, publicationYear);
    }
}