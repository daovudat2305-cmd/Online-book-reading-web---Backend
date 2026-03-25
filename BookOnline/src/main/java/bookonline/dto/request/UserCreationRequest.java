package bookonline.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
	@NotEmpty(message = "Tên đăng nhập không được để trống")
	private String username;
	
	@Email(message = "Sai định dạng email")
	@NotEmpty(message = "Email không được để trống")
	private String email;
	
	@Size(min = 8, message = "Mật khẩu phải có tối đa 8 kí tự")
	private String password;
}
