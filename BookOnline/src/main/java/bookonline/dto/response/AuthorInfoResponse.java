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
public class AuthorInfoResponse {
	String authorId;
	String username;
	String email;
	String fullName;
	String avatar;
	String gender;
	LocalDate dob;
	String bankAccount;
	
	//List<Book>
}
