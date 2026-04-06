package bookonline.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import bookonline.entity.PaymentRequest;


public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, String>{
	@Query(value = "SELECT COALESCE(SUM(amount), 0) FROM paymentrequest " +
            "WHERE status = 'PAID' " +
            "AND MONTH(paidAt) = MONTH(CURDATE()) " +
            "AND YEAR(paidAt) = YEAR(CURDATE())", 
            nativeQuery = true)
	Long sumAmountByCurrentMonth();
	
	@Query(value = "SELECT COUNT(id) FROM paymentrequest " +
            "WHERE status = 'PENDING' ", 
            nativeQuery = true)
	Long countPaymentRequestIsPending();
}
