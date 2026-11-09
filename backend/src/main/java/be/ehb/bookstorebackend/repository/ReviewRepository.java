
package be.ehb.bookstorebackend.repository;

import be.ehb.bookstorebackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndBookIsbn(Long userId, String isbn);

    List<Review> findByBookIsbn(String isbn);

    List<Review> findByUserId(Long userId);


}
