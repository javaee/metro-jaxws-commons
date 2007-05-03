package books;

import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;

@WebService
public class BookService {
    public List<Book> getRecommendedBooks() {
        List<Book> books = new ArrayList<Book>();
        books.add(new Book(5100,"Kohsuke Kawaguchi","JAXB inside out"));
        books.add(new Book(5101,"Vivek Pandey","JAX-WS RI explained"));
        books.add(new Book(5102,"Jitendra Kotamraju","apache log processing 2nd edition"));
        return books;
    }
}
