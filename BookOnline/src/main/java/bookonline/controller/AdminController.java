package bookonline.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.service.AuthorService;
import bookonline.service.BookService;
import bookonline.service.PaymentRequestService;
import bookonline.service.PaymentService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired private AuthorService authorService;
	@Autowired private BookService bookService;
	@Autowired private PaymentService paymentService;
	@Autowired private PaymentRequestService paymentRequestService;
	
	//danh sách đơn đăng ký
	@GetMapping("/author-request")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllRequests(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "12") int size) {
		return ResponseEntity.ok().body(authorService.getAllRequests(page,size));
	}
	
	//tìm kiếm đơn đăng ký
	@GetMapping("/author-request/{keyword}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> searchRequestsByAuthorName(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "12") int size,
			@PathVariable String keyword) {
		return ResponseEntity.ok().body(authorService.searchAuthorRequestByName(page, size, keyword));
	}
	
	//duyệt đơn đăng ký
	@PatchMapping("/author-request/{requestId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> reviewAuthorRegister(@PathVariable String requestId, @RequestBody String status) {
		authorService.reviewAuthorRegister(requestId, status);
		return ResponseEntity.ok(null);
	}
	
	//lấy danh sách tác giả
	@GetMapping("/authors")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getListAuthors(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "12") int size) {
		return ResponseEntity.ok().body(authorService.getAllAuthors(page, size));
	}
	
	//tìm kiếm tác giả
	@GetMapping("/authors/{keyword}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> searchAuthors(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "12") int size,
			@PathVariable String keyword) {
		return ResponseEntity.ok().body(authorService.searchAuthors(page, size, keyword));
	}
	
	//xem sách theo tác giả
	@GetMapping("/authors/{authorName}/books")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getBookByAuthor(@PathVariable String authorName) {
		return ResponseEntity.ok().body(bookService.getBookByAuthorName(authorName));
	}
	
	//lấy danh sách giao dịch
	@GetMapping("/payments/history")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllPayments(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok().body(paymentService.getAllPayments(page, size));
	}
	
	//lịch sử giao dịch vip
	@GetMapping("/payments/history/{keyword}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> searchPayment(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "20") int size,
			@PathVariable String keyword) {
		return ResponseEntity.ok().body(paymentService.searchPayment(keyword,page, size));
	}
	
	//tính doanh thu
	@GetMapping("/payments/revenue")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getRevenueCurrentMonth() {
		Map<String, Long> revenue = paymentService.getTotalRevenueCurrentMonth();
		Map<String, Long> totalPaid = paymentRequestService.getTotalPaidAmountInMonth();
		
		Long totalRevenue = revenue.get("totalRevenue") - totalPaid.get("totalPaidAmount");
		
		return ResponseEntity.ok().body(Map.of("totalRevenue", totalRevenue));
	}
	
	//số đăng ký vip
	@GetMapping("/payments/registerVip")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getNumberOfVipCurrentMonth() {
		return ResponseEntity.ok().body(paymentService.getNumberOfVipCurrentMonth());
	}
	
	//lấy danh sách giao dịch yêu cầu từ tác giả
	@GetMapping("/paymentRequests")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllPaymentRequests(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok().body(paymentRequestService.getAllPaymentRequests(page, size));
	}
	
	//đếm số yêu cầu
	@GetMapping("/paymentRequests/count")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> countPaymentRequestIsPending() {
		return ResponseEntity.ok().body(paymentRequestService.countPaymentRequestIsPending());
	}
	
	//duyệt tiền yêu cầu
	@PatchMapping("/paymentRequests/process")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> processPaymentRequest(
			@RequestParam String requestId, 
			@RequestParam String action) {
		return ResponseEntity.ok().body(paymentRequestService.processPaymentRequest(requestId, action));
	}
}
