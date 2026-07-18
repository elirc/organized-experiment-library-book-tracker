package librarytracker.model;

import java.time.LocalDate;

public class Book {
    private final long id;
    private String title;
    private String author;
    private String isbn;
    private boolean checkedOut;
    private Long borrowerId;
    private LocalDate checkedOutAt;
    private LocalDate dueDate;

    public Book(long id, String title, String author, String isbn) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.checkedOut = false;
    }

    public Book(
            long id,
            String title,
            String author,
            String isbn,
            boolean checkedOut,
            Long borrowerId,
            LocalDate checkedOutAt,
            LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.checkedOut = checkedOut;
        this.borrowerId = borrowerId;
        this.checkedOutAt = checkedOutAt;
        this.dueDate = dueDate;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public LocalDate getCheckedOutAt() {
        return checkedOutAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isOverdue(LocalDate today) {
        return checkedOut && dueDate != null && dueDate.isBefore(today);
    }

    public void checkOut(long borrowerId, LocalDate checkedOutAt, LocalDate dueDate) {
        this.checkedOut = true;
        this.borrowerId = borrowerId;
        this.checkedOutAt = checkedOutAt;
        this.dueDate = dueDate;
    }

    public void returnBook() {
        this.checkedOut = false;
        this.borrowerId = null;
        this.checkedOutAt = null;
        this.dueDate = null;
    }
}
