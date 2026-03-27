package bookonline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.service.AuthorService;

@RestController
@RequestMapping("/admin")
public class AdminController {
	@Autowired private AuthorService authorService;
	
	@GetMapping("/author-request")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getAllRequests(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "12") int size) {
		return ResponseEntity.ok().body(authorService.getAllRequests(page,size));
	}
	
	//duyệt đơn đăng ký
	@PatchMapping("/author-request/{requestId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> reviewAuthorRegister(@PathVariable String requestId, @RequestBody String status) {
		authorService.reviewAuthorRegister(requestId, status);
		return ResponseEntity.ok(null);
	}
	
	//lấy danh sách tác giả
	@GetMapping("/authors")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getListAuthors(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "12") int size) {
		return ResponseEntity.ok().body(authorService.getAllAuthors(page, size));
	}
}
