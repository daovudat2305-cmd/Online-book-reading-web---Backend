package bookonline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.dto.request.SePayWebhookRequest;
import bookonline.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {
	@Autowired private PaymentService paymentService;
	
	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('AUTHOR', 'USER')")
	public ResponseEntity<?> createPayment(@RequestParam String vipId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(vipId));
	}
	
	@PostMapping("/sepay-webhook")
	public ResponseEntity<?> handleSePayWebhook(@RequestBody SePayWebhookRequest request) {
		return ResponseEntity.ok().body(paymentService.processSePayWebhook(request));
	}
	
	// kiểm tra trạng thái giao dịch
	@GetMapping("/status")
	public ResponseEntity<?> checkPaymentStatus(@RequestParam String paymentId) {
		return ResponseEntity.ok().body(paymentService.getPaymentStatus(paymentId));
	}
	
	@GetMapping("/history")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllPayments(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok().body(paymentService.getAllPayments(page, size));
	}
	
	@GetMapping("/history/{username}")
	@PreAuthorize("hasAnyRole('AUTHOR', 'USER')")
	public ResponseEntity<?> getPaymentsByUsername(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "10") int size,
			@PathVariable String username) {
		return ResponseEntity.ok().body(paymentService.getPaymentsByUsername(page, size, username));
	}
}
