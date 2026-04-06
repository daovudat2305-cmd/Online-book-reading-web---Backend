package bookonline.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequestResponse {
	String paymentRequestId;
	String authorId;
	String authorName;//username
	long amount;
	String status;
	String content;
	LocalDateTime createdAt;
	LocalDateTime paidAt;
}
