package bookonline.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SePayWebhookRequest {
	String gateway;
	String transactionDate;
	String accountNumber;
	String subAccount;
	String content;
	String transferType;
	Double transferAmount;
	Double accumulated;
	String referenceCode;
	String description;
}
