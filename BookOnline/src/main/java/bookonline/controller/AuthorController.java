package bookonline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bookonline.dto.request.AuthorRegisterRequest;
import bookonline.service.AuthorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/authors")
public class AuthorController {
	@Autowired private AuthorService authorService;
	
	@PostMapping("/register")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> authorRegister(@Valid @RequestBody AuthorRegisterRequest request) {
		return ResponseEntity.ok().body(authorService.authorRegister(request));
	}
	
	@GetMapping("/register")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> getRegisterResponse() {
		return ResponseEntity.ok().body(authorService.getRegisterReponse());
	}
}
