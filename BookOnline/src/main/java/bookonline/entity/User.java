package bookonline.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	String userId;
	
	String email;
	String username;
	String password;
	String role;
	LocalDate createdAt;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private UserInfo userInfo;
}
