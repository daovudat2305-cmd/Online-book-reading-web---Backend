package bookonline.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Entity
public class PaymentRequest {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	String id;
	
	String authorId;
	long amount;
	String status;
	String content;
	LocalDateTime createdAt;
	LocalDateTime paidAt;
}
