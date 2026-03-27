package bookonline.entity;

import java.time.LocalDate;

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
public class AuthorRequest {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	String requestId;
	
	String authorId;
	String status;
	String description;
	LocalDate createdAt;
	LocalDate reviewAt;
	int isDelete;
}
