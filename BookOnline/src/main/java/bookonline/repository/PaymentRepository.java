package bookonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
	List<Payment> findByStatus(String status);
	Payment findByPaymentId(String paymentId);
	
	List<Payment> findByStatusAndCreatedTimeBefore(String status, LocalDateTime dateTime);
	
	Page<Payment> findByUserId(String userId, Pageable pageable);
}
