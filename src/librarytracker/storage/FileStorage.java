package librarytracker.storage;

import librarytracker.model.Book;
import librarytracker.model.Borrower;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileStorage {
    private static final String RECORD_SEPARATOR = "---";

    private final Path dataDirectory;
    private final Path booksFile;
    private final Path borrowersFile;

    public FileStorage(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.booksFile = dataDirectory.resolve("books.txt");
        this.borrowersFile = dataDirectory.resolve("borrowers.txt");
    }

    public LibraryData load() throws IOException {
        Files.createDirectories(dataDirectory);
        List<Book> books = loadBooks();
        List<Borrower> borrowers = loadBorrowers();
        return new LibraryData(books, borrowers);
    }

    public void save(List<Book> books, List<Borrower> borrowers) throws IOException {
        Files.createDirectories(dataDirectory);
        saveBooks(books);
        saveBorrowers(borrowers);
    }

    private List<Book> loadBooks() throws IOException {
        List<Book> books = new ArrayList<>();

        if (!Files.exists(booksFile)) {
            return books;
        }

        for (Map<String, String> record : readRecords(booksFile)) {
            Book book = new Book(
                    Long.parseLong(record.getOrDefault("id", "0")),
                    record.getOrDefault("title", ""),
                    record.getOrDefault("author", ""),
                    blankToNull(record.getOrDefault("isbn", "")),
                    Boolean.parseBoolean(record.getOrDefault("checkedOut", "false")),
                    parseLongOrNull(record.getOrDefault("borrowerId", "")),
                    parseDateOrNull(record.getOrDefault("checkedOutAt", "")),
                    parseDateOrNull(record.getOrDefault("dueDate", "")));
            books.add(book);
        }

        return books;
    }

    private List<Borrower> loadBorrowers() throws IOException {
        List<Borrower> borrowers = new ArrayList<>();

        if (!Files.exists(borrowersFile)) {
            return borrowers;
        }

        for (Map<String, String> record : readRecords(borrowersFile)) {
            Borrower borrower = new Borrower(
                    Long.parseLong(record.getOrDefault("id", "0")),
                    record.getOrDefault("name", ""),
                    blankToNull(record.getOrDefault("email", "")),
                    blankToNull(record.getOrDefault("phone", "")));
            borrowers.add(borrower);
        }

        return borrowers;
    }

    private void saveBooks(List<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();

        for (Book book : books) {
            lines.add("id=" + book.getId());
            lines.add("title=" + clean(book.getTitle()));
            lines.add("author=" + clean(book.getAuthor()));
            lines.add("isbn=" + clean(book.getIsbn()));
            lines.add("checkedOut=" + book.isCheckedOut());
            lines.add("borrowerId=" + valueOrBlank(book.getBorrowerId()));
            lines.add("checkedOutAt=" + valueOrBlank(book.getCheckedOutAt()));
            lines.add("dueDate=" + valueOrBlank(book.getDueDate()));
            lines.add(RECORD_SEPARATOR);
        }

        Files.write(booksFile, lines);
    }

    private void saveBorrowers(List<Borrower> borrowers) throws IOException {
        List<String> lines = new ArrayList<>();

        for (Borrower borrower : borrowers) {
            lines.add("id=" + borrower.getId());
            lines.add("name=" + clean(borrower.getName()));
            lines.add("email=" + clean(borrower.getEmail()));
            lines.add("phone=" + clean(borrower.getPhone()));
            lines.add(RECORD_SEPARATOR);
        }

        Files.write(borrowersFile, lines);
    }

    private List<Map<String, String>> readRecords(Path filePath) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);
        Map<String, String> currentRecord = new LinkedHashMap<>();

        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            if (RECORD_SEPARATOR.equals(line)) {
                if (!currentRecord.isEmpty()) {
                    records.add(new LinkedHashMap<>(currentRecord));
                    currentRecord.clear();
                }
                continue;
            }

            String[] parts = line.split("=", 2);
            String key = parts[0];
            String value = parts.length > 1 ? parts[1] : "";
            currentRecord.put(key, value);
        }

        if (!currentRecord.isEmpty()) {
            records.add(currentRecord);
        }

        return records;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Long parseLongOrNull(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    private LocalDate parseDateOrNull(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private String valueOrBlank(Object value) {
        return value == null ? "" : value.toString();
    }

    private String clean(String value) {
        return value == null ? "" : value.replace("\n", " ");
    }

    public static class LibraryData {
        private final List<Book> books;
        private final List<Borrower> borrowers;

        public LibraryData(List<Book> books, List<Borrower> borrowers) {
            this.books = books;
            this.borrowers = borrowers;
        }

        public List<Book> getBooks() {
            return books;
        }

        public List<Borrower> getBorrowers() {
            return borrowers;
        }
    }
}
