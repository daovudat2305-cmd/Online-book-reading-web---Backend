package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bookonline.entity.BookEarning;
import java.util.List;


public interface BookEarningRepository extends JpaRepository<BookEarning, Long>{
	@Query(value = "SELECT b.* FROM bookearning b " +
            "JOIN book b2 ON b.bookId = b2.bookId " +
            "WHERE b2.authorId = :authorId " + 
            "AND b2.status = 1 " +
            "AND b.status = :status", 
    nativeQuery = true)
	List<BookEarning> findByAuthorIdAndStatus(@Param("authorId") String authorId, @Param("status") String status);
	
	List<BookEarning> findByPaymentRequestId(String paymentRequestId);
	
}
