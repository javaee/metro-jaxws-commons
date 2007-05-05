package books;

public class Book {
    public Book() {
    }

    public Book(int id, String author, String title) {
        this.id = id;
        this.author = author;
        this.title = title;
    }

    public String author;
    public int id;
    public String title;
}
