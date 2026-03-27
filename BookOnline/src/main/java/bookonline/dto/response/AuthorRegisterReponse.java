package bookonline.dto.response;

import java.time.LocalDate;

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
public class AuthorRegisterReponse {
	String requestId;
	String email;
	String fullName;
	String avatar;
	String username;
	String gender;
	LocalDate dob;
	String bankAccount;
	String description;
	LocalDate createdAt;
	String status;
	LocalDate reviewAt;
}
