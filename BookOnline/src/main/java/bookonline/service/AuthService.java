package bookonline.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import bookonline.dto.request.LoginRequest;
import bookonline.entity.User;
import bookonline.repository.UserRepository;
import bookonline.util.JwtUtil;

@Service
public class AuthService {
	
	@Autowired private JwtUtil jwtUtil;
	@Autowired private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	public Map<String, String> signin(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername());
		if(user == null) {
			throw new RuntimeException("Sai tên đăng nhập");
		}
		
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
		boolean checkSignIn = passwordEncoder.matches(request.getPassword(), user.getPassword());
		
		if(!checkSignIn) {
			throw new RuntimeException("Sai mật khẩu");
		}
		
		String token = jwtUtil.generateToken(user);
		Map<String,String> response = new HashMap<>();
		response.put("token", token);
		response.put("role", user.getRole());
		response.put("username", user.getUsername());
		return response;
	}
}
