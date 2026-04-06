package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import bookonline.entity.BookEarning;
import java.util.List;


public interface BookEarningRepository extends JpaRepository<BookEarning, Long>{
	List<BookEarning> findByAuthorIdAndStatus(String authorId, String status);
	List<BookEarning> findByPaymentRequestId(String paymentRequestId);
	
}
