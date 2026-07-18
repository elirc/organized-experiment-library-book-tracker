package librarytracker;

import librarytracker.model.Book;
import librarytracker.model.Borrower;
import librarytracker.service.LibraryManager;
import librarytracker.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {
    private final LibraryManager libraryManager;
    private final Scanner scanner;

    public App(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");

        try {
            FileStorage fileStorage = new FileStorage(dataDirectory);
            LibraryManager libraryManager = new LibraryManager(fileStorage);
            App app = new App(libraryManager);
            app.run();
        } catch (IOException e) {
            System.out.println("Could not load library data: " + e.getMessage());
        }
    }

    public void run() {
        System.out.println("Library Book Tracker");
        System.out.println("Data is loaded from the local data folder.");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> addBook();
                case 2 -> listBooks();
                case 3 -> registerBorrower();
                case 4 -> listBorrowers();
                case 5 -> checkOutBook();
                case 6 -> returnBook();
                case 7 -> viewCheckedOutBooks();
                case 8 -> saveData();
                case 9 -> {
                    exitApplication();
                    running = false;
                }
                default -> System.out.println("Invalid option. Please choose a number from the menu.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Add book");
        System.out.println("2. List books");
        System.out.println("3. Register borrower");
        System.out.println("4. List borrowers");
        System.out.println("5. Check out book");
        System.out.println("6. Return book");
        System.out.println("7. View checked-out books");
        System.out.println("8. Save data");
        System.out.println("9. Exit");
    }

    private void addBook() {
        System.out.println();
        String title = readRequiredText("Title: ");
        String author = readRequiredText("Author: ");
        String isbn = readOptionalText("ISBN (optional): ");

        Book book = libraryManager.addBook(title, author, isbn);
        System.out.println("Book added with ID " + book.getId() + ".");
    }

    private void listBooks() {
        System.out.println();
        List<Book> books = libraryManager.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        for (Book book : books) {
            System.out.println(formatBook(book));
        }
    }

    private void registerBorrower() {
        System.out.println();
        String name = readRequiredText("Name: ");
        String email = readOptionalText("Email (optional): ");
        String phone = readOptionalText("Phone (optional): ");

        Borrower borrower = libraryManager.registerBorrower(name, email, phone);
        System.out.println("Borrower registered with ID " + borrower.getId() + ".");
    }

    private void listBorrowers() {
        System.out.println();
        List<Borrower> borrowers = libraryManager.getAllBorrowers();

        if (borrowers.isEmpty()) {
            System.out.println("No borrowers found.");
            return;
        }

        for (Borrower borrower : borrowers) {
            System.out.println(formatBorrower(borrower));
        }
    }

    private void checkOutBook() {
        System.out.println();
        long bookId = readLong("Book ID: ");
        long borrowerId = readLong("Borrower ID: ");
        LocalDate dueDate = readOptionalDate("Due date (YYYY-MM-DD, optional): ");

        String result = libraryManager.checkOutBook(bookId, borrowerId, dueDate);
        System.out.println(result);
    }

    private void returnBook() {
        System.out.println();
        long bookId = readLong("Book ID: ");
        String result = libraryManager.returnBook(bookId);
        System.out.println(result);
    }

    private void viewCheckedOutBooks() {
        System.out.println();
        List<Book> books = libraryManager.getCheckedOutBooks();

        if (books.isEmpty()) {
            System.out.println("No books are currently checked out.");
            return;
        }

        for (Book book : books) {
            System.out.println(formatBook(book));
        }
    }

    private void saveData() {
        try {
            libraryManager.saveData();
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    private void exitApplication() {
        saveData();
        System.out.println("Goodbye.");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid ID number.");
            }
        }
    }

    private String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private String readOptionalText(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Please use the YYYY-MM-DD format.");
            }
        }
    }

    private String formatBook(Book book) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID: ").append(book.getId());
        builder.append(" | Title: ").append(book.getTitle());
        builder.append(" | Author: ").append(book.getAuthor());

        if (book.getIsbn() != null) {
            builder.append(" | ISBN: ").append(book.getIsbn());
        }

        builder.append(" | Status: ").append(buildBookStatus(book));
        return builder.toString();
    }

    private String buildBookStatus(Book book) {
        if (!book.isCheckedOut()) {
            return "Available";
        }

        StringBuilder builder = new StringBuilder("Checked out");

        Optional<Borrower> borrower = libraryManager.findBorrowerForBook(book);
        borrower.ifPresent(value -> builder.append(" to ").append(value.getName()));

        if (book.getCheckedOutAt() != null) {
            builder.append(" on ").append(book.getCheckedOutAt());
        }

        if (book.getDueDate() != null) {
            builder.append(", due ").append(book.getDueDate());

            if (book.isOverdue(LocalDate.now())) {
                builder.append(" (OVERDUE)");
            }
        }

        return builder.toString();
    }

    private String formatBorrower(Borrower borrower) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID: ").append(borrower.getId());
        builder.append(" | Name: ").append(borrower.getName());

        if (borrower.getEmail() != null) {
            builder.append(" | Email: ").append(borrower.getEmail());
        }

        if (borrower.getPhone() != null) {
            builder.append(" | Phone: ").append(borrower.getPhone());
        }

        return builder.toString();
    }
}
