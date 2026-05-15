package bookonline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.dto.request.SePayWebhookRequest;
import bookonline.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {
	@Autowired private PaymentService paymentService;
	
	@Value("${sepay.webhook.api-key}")
    private String sepayApiKey;
	
	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('AUTHOR', 'USER')")
	public ResponseEntity<?> createPayment(@RequestParam String vipId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(vipId));
	}
	
	@PostMapping("/sepay-webhook")
	public ResponseEntity<?> handleSePayWebhook(@RequestHeader(value = "Authorization", required = false) String authHeader,@RequestBody SePayWebhookRequest request) {
		if (authHeader == null || !authHeader.equals(sepayApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Cảnh báo: Request giả mạo, không có quyền truy cập!");
        }
		try {
			return ResponseEntity.ok().body(paymentService.processSePayWebhook(request));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
	}
	
	// kiểm tra trạng thái giao dịch
	@GetMapping("/status")
	public ResponseEntity<?> checkPaymentStatus(@RequestParam String paymentId) {
		return ResponseEntity.ok().body(paymentService.getPaymentStatus(paymentId));
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
