package bookonline.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import bookonline.entity.Payment;
import bookonline.entity.UserVip;
import bookonline.repository.PaymentRepository;
import bookonline.repository.UserVipRepository;

@Service
public class ScheduleService {
	@Autowired private UserVipRepository userVipRepository;
	@Autowired private PaymentRepository paymentRepository;
	
	//xóa các đơn thanh toán pending sau 10p nếu không được thanh toán
	@Scheduled(fixedRate = 300000)
	public void cancelExpiredPayments() {
		LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(10);
		
		List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedTimeBefore("PENDING", expirationTime);
		for(Payment payment : expiredPayments) {
			payment.setStatus("CANCELLED");
		}
		paymentRepository.saveAll(expiredPayments);
	}
	
	//cập nhật trạng thái vip người dùng hàng ngày
	@Scheduled(cron = "0 0 0 * * ?")
	public void cleanupExpiredVips() {
		List<UserVip> expiredVips = userVipRepository.findByEndDateBefore(LocalDateTime.now());
		
		userVipRepository.deleteAll(expiredVips);
		
		//xóa các đơn đã bị hủy khi qua ngày mới
		List<Payment> cancelledPayments = paymentRepository.findByStatus("CANCELLED");
		paymentRepository.deleteAll(cancelledPayments);
	}
}
