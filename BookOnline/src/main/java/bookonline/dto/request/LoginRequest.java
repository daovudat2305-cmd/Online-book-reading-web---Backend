package bookonline.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	@NotEmpty(message = "Tên đăng nhập trống")
	private String username;
	
	@NotEmpty(message = "Mật khẩu trống")
	private String password;
}
