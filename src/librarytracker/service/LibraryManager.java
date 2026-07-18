package librarytracker.service;

import librarytracker.model.Book;
import librarytracker.model.Borrower;
import librarytracker.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LibraryManager {
    private final List<Book> books;
    private final List<Borrower> borrowers;
    private final FileStorage fileStorage;
    private long nextBookId;
    private long nextBorrowerId;

    public LibraryManager(FileStorage fileStorage) throws IOException {
        this.fileStorage = fileStorage;

        FileStorage.LibraryData libraryData = fileStorage.load();
        this.books = new ArrayList<>(libraryData.getBooks());
        this.borrowers = new ArrayList<>(libraryData.getBorrowers());
        this.nextBookId = findNextBookId();
        this.nextBorrowerId = findNextBorrowerId();
    }

    public Book addBook(String title, String author, String isbn) {
        Book book = new Book(nextBookId++, title, author, emptyToNull(isbn));
        books.add(book);
        return book;
    }

    public Borrower registerBorrower(String name, String email, String phone) {
        Borrower borrower = new Borrower(nextBorrowerId++, name, emptyToNull(email), emptyToNull(phone));
        borrowers.add(borrower);
        return borrower;
    }

    public List<Book> getAllBooks() {
        List<Book> sortedBooks = new ArrayList<>(books);
        sortedBooks.sort(Comparator.comparingLong(Book::getId));
        return sortedBooks;
    }

    public List<Borrower> getAllBorrowers() {
        List<Borrower> sortedBorrowers = new ArrayList<>(borrowers);
        sortedBorrowers.sort(Comparator.comparingLong(Borrower::getId));
        return sortedBorrowers;
    }

    public List<Book> getCheckedOutBooks() {
        List<Book> checkedOutBooks = new ArrayList<>();

        for (Book book : books) {
            if (book.isCheckedOut()) {
                checkedOutBooks.add(book);
            }
        }

        checkedOutBooks.sort(Comparator.comparingLong(Book::getId));
        return checkedOutBooks;
    }

    public String checkOutBook(long bookId, long borrowerId, LocalDate dueDate) {
        Optional<Book> bookOptional = findBookById(bookId);
        if (bookOptional.isEmpty()) {
            return "Book not found.";
        }

        Optional<Borrower> borrowerOptional = findBorrowerById(borrowerId);
        if (borrowerOptional.isEmpty()) {
            return "Borrower not found.";
        }

        Book book = bookOptional.get();
        if (book.isCheckedOut()) {
            return "Book is already checked out.";
        }

        book.checkOut(borrowerId, LocalDate.now(), dueDate);
        return "Book checked out successfully.";
    }

    public String returnBook(long bookId) {
        Optional<Book> bookOptional = findBookById(bookId);
        if (bookOptional.isEmpty()) {
            return "Book not found.";
        }

        Book book = bookOptional.get();
        if (!book.isCheckedOut()) {
            return "Book is not currently checked out.";
        }

        book.returnBook();
        return "Book returned successfully.";
    }

    public Optional<Book> findBookById(long id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return Optional.of(book);
            }
        }

        return Optional.empty();
    }

    public Optional<Borrower> findBorrowerById(long id) {
        for (Borrower borrower : borrowers) {
            if (borrower.getId() == id) {
                return Optional.of(borrower);
            }
        }

        return Optional.empty();
    }

    public Optional<Borrower> findBorrowerForBook(Book book) {
        if (book.getBorrowerId() == null) {
            return Optional.empty();
        }

        return findBorrowerById(book.getBorrowerId());
    }

    public void saveData() throws IOException {
        fileStorage.save(books, borrowers);
    }

    private long findNextBookId() {
        long highestId = -1;

        for (Book book : books) {
            if (book.getId() > highestId) {
                highestId = book.getId();
            }
        }

        return highestId + 1;
    }

    private long findNextBorrowerId() {
        long highestId = -1;

        for (Borrower borrower : borrowers) {
            if (borrower.getId() > highestId) {
                highestId = borrower.getId();
            }
        }

        return highestId + 1;
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
