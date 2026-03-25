package bookonline.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bookonline.dto.request.LoginRequest;
import bookonline.dto.request.UserCreationRequest;
import bookonline.service.AuthService;
import bookonline.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired private UserService userService;
	@Autowired private AuthService authService;
	
	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody UserCreationRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
	}
	
	@PostMapping("/signin")
	public ResponseEntity<?> signin(@Valid @RequestBody LoginRequest request) {
		Map<String,String> response = authService.signin(request);
		return ResponseEntity.ok().body(response);
	}
}
