package bookonline.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookonline.dto.response.PaymentRequestResponse;
import bookonline.entity.Book;
import bookonline.entity.BookEarning;
import bookonline.entity.PaymentRequest;
import bookonline.entity.User;
import bookonline.repository.BookEarningRepository;
import bookonline.repository.BookRepository;
import bookonline.repository.PaymentRequestRepository;
import bookonline.repository.UserRepository;

//tac gia yeu cau tra tien sach
@Service
public class PaymentRequestService {
	@Autowired private UserRepository userRepository;
	@Autowired private BookRepository bookRepository;
	@Autowired private PaymentRequestRepository paymentRequestRepository;
	@Autowired private BookEarningRepository bookEarningRepository;
	
	//tính số tiền có thể yêu cầu
	public Map<String, Long> getAvailableBalance() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String authorName = authentication.getName(); 

		User author = userRepository.findByUsername(authorName);
		List<BookEarning> listBookEarning = bookEarningRepository.findByAuthorIdAndStatus(author.getUserId(), "UNPAID");
		long totalAmount = 0;
		if(listBookEarning.isEmpty()) {
			return Map.of("totalAmount", totalAmount);
		}
		for(BookEarning bookEarning : listBookEarning) {
			Book book = bookRepository.findByBookId(bookEarning.getBookId());
			if(book.getStatus()==1) {
				totalAmount += bookEarning.getAmount();
			}
		}
		return Map.of("totalAmount", totalAmount);
	}
	
	//tạo yêu cầu
	@Transactional
	public void createPaymentRequest() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String authorName = authentication.getName(); 
		
		User author = userRepository.findByUsername(authorName);
		List<BookEarning> listBookEarning = bookEarningRepository.findByAuthorIdAndStatus(author.getUserId(), "UNPAID");
		if(listBookEarning.isEmpty()) {
			throw new RuntimeException("Bạn chưa có sách VIP để yêu cầu trả tiền!");
		}
		long amount = listBookEarning.get(0).getAmount();
		
		PaymentRequest request = PaymentRequest.builder()
				.authorId(author.getUserId())
				.amount(amount*listBookEarning.size())
				.status("PENDING")
				.content("Yêu cầu trả tiền sách")
				.createdAt(LocalDateTime.now())
				.build();
		PaymentRequest savedRequest = paymentRequestRepository.save(request);
		listBookEarning.forEach(bookEarning -> {
			bookEarning.setPaymentRequestId(savedRequest.getId());
			bookEarning.setStatus("PENDING");
		});
		
		bookEarningRepository.saveAll(listBookEarning);
	}
	
	//admin duyệt yêu cầu
	@Transactional
	public Map<String, String> processPaymentRequest(String requestId, String action) {
		PaymentRequest request = paymentRequestRepository.findById(requestId)
	            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu yêu cầu với mã: " + requestId));

	    if (!"PENDING".equals(request.getStatus())) {
	        throw new RuntimeException("Phiếu yêu cầu này đã được xử lý xong rồi!");
	    }
		
		List<BookEarning> listBookEarnings = bookEarningRepository.findByPaymentRequestId(requestId);
		if ("APPROVE".equalsIgnoreCase(action)) {
	        request.setStatus("PAID");
	        request.setPaidAt(LocalDateTime.now());
	        listBookEarnings.forEach(earning -> earning.setStatus("PAID"));
	        
	    } 
		else if ("REJECT".equalsIgnoreCase(action)) {
	        request.setStatus("REJECTED");
	        request.setPaidAt(LocalDateTime.now());
	        listBookEarnings.forEach(earning -> {
	            earning.setStatus("UNPAID");       // Trả về trạng thái chưa thanh toán
	            earning.setPaymentRequestId(null); // Giải phóng sách khỏi phiếu này
	        });
	        
	    } 
		else {
	        throw new RuntimeException("Hành động không hợp lệ! Chỉ chấp nhận APPROVE hoặc REJECT.");
	    }
		paymentRequestRepository.save(request);
	    bookEarningRepository.saveAll(listBookEarnings);
	    
	    return Map.of("message", "Duyệt thành công");
	}
	
	//tính tổng tiền đã tra trong tháng
	public Map<String, Long> getTotalPaidAmountInMonth() {
		return Map.of("totalPaidAmount", paymentRequestRepository.sumAmountByCurrentMonth());
	}
	
	//lấy danh sách yêu cầu
	public Page<PaymentRequestResponse> getAllPaymentRequests(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("paidAt").nullsFirst()));
		
		Page<PaymentRequest> listRequest = paymentRequestRepository.findAll(pageable);
		return listRequest.map(request -> {
			User author = userRepository.findByUserId(request.getAuthorId());
			PaymentRequestResponse response = PaymentRequestResponse.builder()
					.paymentRequestId(request.getId())
					.authorId(request.getAuthorId())
					.authorName(author.getUsername())
					.status(request.getStatus())
					.amount(request.getAmount())
					.content(request.getContent())
					.createdAt(request.getCreatedAt())
					.paidAt(request.getPaidAt())
					.build();
			return response;
		});
	}
	
	//đếm số yêu cầu chưa xử lý
	public Map<String, Long> countPaymentRequestIsPending() {
		return Map.of("numberOfRequests", paymentRequestRepository.countPaymentRequestIsPending());
	}
}
