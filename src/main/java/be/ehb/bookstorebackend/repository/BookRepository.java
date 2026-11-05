
package be.ehb.bookstorebackend.repository;

import be.ehb.bookstorebackend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {

}
