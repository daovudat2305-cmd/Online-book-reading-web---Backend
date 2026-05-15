package bookonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bookonline.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
	List<Payment> findByStatus(String status);
	List<Payment> findByContent(String content);
	Payment findByPaymentId(String paymentId);
	
	List<Payment> findByStatusAndCreatedTimeBefore(String status, LocalDateTime dateTime);
	
	Page<Payment> findByUserId(String userId, Pageable pageable);
	
	@Query(
	        value = "SELECT p.paymentId, p.userId, p.vipId, p.createdTime as createdTime, p.paidTime as paidTime, p.content, p.amount, p.status " +
	                "FROM Payment p " +
	                "JOIN user u ON u.userId = p.userId " +
	                "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.vipId) LIKE LOWER(CONCAT('%', :keyword, '%'))",
	        countQuery = "SELECT COUNT(p.paymentId) " +
	                     "FROM Payment p " +
	                     "JOIN user u ON u.userId = p.userId " +
	                     "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.vipId) LIKE LOWER(CONCAT('%', :keyword, '%'))",    
	        nativeQuery = true
	    )
	Page<Payment> searchPaymentByKeyword(@Param("keyword") String keyword, Pageable pageable);
	
	
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM payment " +
            "WHERE status = 'PAID' " +
            "AND MONTH(paidTime) = MONTH(CURDATE()) " +
            "AND YEAR(paidTime) = YEAR(CURDATE())", 
            nativeQuery = true)
	Long sumAmountByCurrentMonth();
	
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM payment " +
            "WHERE status = 'PAID' " +
            "AND MONTH(paidTime) = :month " +
            "AND YEAR(paidTime) = :year", 
	    nativeQuery = true)
	Long sumAmountByMonthAndYear(@Param("month") int month, @Param("year") int year);
	
	@Query(value = "SELECT COUNT(paymentId) FROM payment " +
            "WHERE status = 'PAID' " +
            "AND MONTH(paidTime) = MONTH(CURDATE()) " +
            "AND YEAR(paidTime) = YEAR(CURDATE())", 
            nativeQuery = true)
	Long countVipCurrentMonth();
}
