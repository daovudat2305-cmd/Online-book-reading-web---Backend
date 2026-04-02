package bookonline.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class AuthorRegisterRequest {
	
	@NotEmpty(message = "Yêu cầu điền đủ Họ và tên")
	String fullName;
	
	String gender;
	
	@NotNull(message = "Điền đủ Ngày tháng năm sinh")
	LocalDate dob;
	
	@NotEmpty(message = "Yêu cầu điền đủ lý do")
	String description;
	
	@NotEmpty(message = "Yêu cầu điền đủ Số tài khoản")
	String bankAccount;
}
