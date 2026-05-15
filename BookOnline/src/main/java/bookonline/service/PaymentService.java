package bookonline.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import bookonline.dto.request.SePayWebhookRequest;
import bookonline.dto.response.PaymentResponse;
import bookonline.entity.Payment;
import bookonline.entity.User;
import bookonline.entity.UserVip;
import bookonline.entity.Vip;
import bookonline.repository.PaymentRepository;
import bookonline.repository.UserRepository;
import bookonline.repository.UserVipRepository;
import bookonline.repository.VipRepository;
import bookonline.util.DateTimeFormatUtil;

@Service
public class PaymentService {

	@Autowired private UserRepository userRepository;
	@Autowired private PaymentRepository paymentRepository;
	@Autowired private VipRepository vipRepository;
	@Autowired private UserVipRepository userVipRepository;
	
	
	public PaymentResponse createPayment(String vipId) {
		// lấy username người đăng ký
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		User user = userRepository.findByUsername(username);
		
		Vip vip = vipRepository.findByVipId(vipId);
		
		if(vip == null) {
			throw new RuntimeException("Gói VIP không tồn tại!");
		}
		
		String paymentId = UUID.randomUUID().toString();
		Payment newPayment = Payment.builder()
				.paymentId(paymentId)
				.userId(user.getUserId())
				.vipId(vipId)
				.amount(vip.getPrice())
				.status("PENDING")
				.createdTime(LocalDateTime.now())
				.paidTime(null)
				.content(username + "NAPVIP" + DateTimeFormatUtil.format(LocalDateTime.now()))
				.build();
		paymentRepository.save(newPayment);
		
		return PaymentResponse.builder()
				.paymentId(paymentId)
				.username(username)
				.vipId(newPayment.getVipId())
				.amount(newPayment.getAmount())
				.status(newPayment.getStatus())
				.createdTime(newPayment.getCreatedTime())
				.paidTime(null)
				.content(newPayment.getContent())
				.build();
	}
	
	
	public Map<String,Boolean> processSePayWebhook(SePayWebhookRequest request) {
		if(!"IN".equalsIgnoreCase(request.getTransferType())) {
			return Map.of("success", false);
		}
		String transferContent = request.getContent().toUpperCase();
		Double amountPaid = request.getTransferAmount();
		
		List<Payment> pendingPayments = paymentRepository.findByStatus("PENDING");
		
		for(Payment payment : pendingPayments) {
			if(transferContent.toLowerCase().contains(payment.getContent().toLowerCase())) {
				if(amountPaid >= payment.getAmount()) {
					payment.setStatus("PAID");
					payment.setPaidTime(LocalDateTime.now());
					
					paymentRepository.save(payment);
					
					
					User user = userRepository.findByUserId(payment.getUserId());
					Vip vip = vipRepository.findByVipId(payment.getVipId());
					UserVip userVip = userVipRepository.findByUserId(payment.getUserId());
					//đăng ký vip nếu chưa có
					if(userVip==null) {
						userVip = UserVip.builder()
								.userId(payment.getUserId())
								.vipId(payment.getVipId())
								.startDate(payment.getPaidTime())
								.endDate(payment.getPaidTime().plusDays(vip.getDurationTime()))
								.user(user)
								.vip(vip)
								.build();
					}
					else {//gia hạn vip (đã có vip)
						if(userVip.getEndDate().isBefore(LocalDateTime.now())) {
						    userVip.setEndDate(LocalDateTime.now().plusDays(vip.getDurationTime()));
						} else {
						    userVip.setEndDate(userVip.getEndDate().plusDays(vip.getDurationTime()));
						}
						userVip.setVipId(payment.getVipId());
						userVip.setVip(vip);
					}
					userVipRepository.save(userVip);
					
					return Map.of("success", true);
				}
				else {
					payment.setStatus("FAIL");
					payment.setPaidTime(LocalDateTime.now());
					
					paymentRepository.save(payment);
					return Map.of("sucsess", false);
				}
			}
		}
		return Map.of("success", false);
	}
	
	public Map<String,String> getPaymentStatus(String paymentId) {
		Payment payment = paymentRepository.findByPaymentId(paymentId);
		return Map.of("status", payment.getStatus());
	}
	
	
	public Page<PaymentResponse> getPaymentsByUsername(int page, int size, String username) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username1 = authentication.getName();
		
		if(!username.equals(username1)) {
			throw new RuntimeException("Không thể xem lịch sử thanh toán của người khác");
		}
		
		User user = userRepository.findByUsername(username);
		if(user==null) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}
		
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("paidTime").nullsFirst()));
		
		Page<Payment> listPayments = paymentRepository.findByUserId(user.getUserId(), pageable);
		
		return listPayments.map(payment -> {
			PaymentResponse response = PaymentResponse.builder()
					.paymentId(payment.getPaymentId())
					.username(payment.getUser().getUsername())
					.vipId(payment.getVipId())
					.amount(payment.getAmount())
					.status(payment.getStatus())
					.createdTime(payment.getCreatedTime())
					.paidTime(payment.getPaidTime())
					.content(payment.getContent())
					.build();
			return response;
		});
	}
	
	//admin lấy danh sách giao dịch
	public Page<PaymentResponse> getAllPayments(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("paidTime").nullsFirst()));
		
		Page<Payment> listPayments = paymentRepository.findAll(pageable);
		
		return listPayments.map(payment -> {
			PaymentResponse response = PaymentResponse.builder()
					.paymentId(payment.getPaymentId())
					.username(payment.getUser().getUsername())
					.userRole(payment.getUser().getRole())
					.vipId(payment.getVipId())
					.amount(payment.getAmount())
					.status(payment.getStatus())
					.createdTime(payment.getCreatedTime())
					.paidTime(payment.getPaidTime())
					.content(payment.getContent())
					.build();
			return response;
		});
	}
	
	//tìm kiếm giao dịch
	public Page<PaymentResponse> searchPayment(String keyword,int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("paidTime")));
		
		Page<Payment> listPayments = paymentRepository.searchPaymentByKeyword(keyword, pageable);
		
		return listPayments.map(payment -> {
			PaymentResponse response = PaymentResponse.builder()
					.paymentId(payment.getPaymentId())
					.username(payment.getUser().getUsername())
					.userRole(payment.getUser().getRole())
					.vipId(payment.getVipId())
					.amount(payment.getAmount())
					.status(payment.getStatus())
					.createdTime(payment.getCreatedTime())
					.paidTime(payment.getPaidTime())
					.content(payment.getContent())
					.build();
			return response;
		});
	}
	
	//tổng doanh thu tháng
	public Map<String, Long> getTotalRevenueCurrentMonth() {
		return Map.of("totalRevenue", paymentRepository.sumAmountByCurrentMonth());
	}
	
	//tổng người đăng ký vip trong tháng
	public Map<String, Long> getNumberOfVipCurrentMonth() {
		return Map.of("numberOfVip", paymentRepository.countVipCurrentMonth());
	}
}
