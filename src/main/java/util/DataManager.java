package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import model.Book;
import model.Member;
import model.Loan;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// handles saving and loading library data as JSON files
public class DataManager {

    private static final String DATA_DIR = "data";
    private static final String BOOKS_FILE = DATA_DIR + "/books.json";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.json";
    private static final String LOANS_FILE = DATA_DIR + "/loans.json";

    private final Gson gson;

    public DataManager() {
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).setPrettyPrinting().create();

        // create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.out.println("Warning: could not create data directory.");
        }
    }

    // save methods

    public void saveBooks(List<Book> books) {
        writeFile(BOOKS_FILE, gson.toJson(books));
    }

    public void saveMembers(List<Member> members) {
        writeFile(MEMBERS_FILE, gson.toJson(members));
    }

    public void saveLoans(List<Loan> loans) {
        writeFile(LOANS_FILE, gson.toJson(loans));
    }

    // load methods

    public List<Book> loadBooks() {
        Type type = new TypeToken<List<Book>>() {}.getType();
        List<Book> result = readFile(BOOKS_FILE, type);
        return result != null ? result : new ArrayList<>();
    }

    public List<Member> loadMembers() {
        Type type = new TypeToken<List<Member>>() {}.getType();
        List<Member> result = readFile(MEMBERS_FILE, type);
        return result != null ? result : new ArrayList<>();
    }

    public List<Loan> loadLoans() {
        Type type = new TypeToken<List<Loan>>() {}.getType();
        List<Loan> result = readFile(LOANS_FILE, type);
        return result != null ? result : new ArrayList<>();
    }

    // file I/O helpers

    private void writeFile(String path, String json) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(json);
        } catch (IOException e) {
            System.out.println("Error saving to " + path + ": " + e.getMessage());
        }
    }

    private <T> T readFile(String path, Type type) {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) return null;

        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.out.println("Error reading " + path + ": " + e.getMessage());
            return null;
        }
    }
}
