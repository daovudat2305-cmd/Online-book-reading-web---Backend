package bookonline.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bookonline.dto.request.UserUpdateRequest;
import bookonline.service.CloudinaryService;
import bookonline.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
	@Autowired private UserService userService;
	@Autowired private CloudinaryService cloudinaryService;
	
	@GetMapping("/myInfo")
	@PreAuthorize("hasAnyRole('AUTHOR', 'USER')")
	public ResponseEntity<?> getMyInfo() {
		return ResponseEntity.ok().body(userService.getMyInfo());
	}
	
	//cập nhật thông tin
	@PatchMapping("/myInfo")
	@PreAuthorize("hasAnyRole('AUTHOR', 'USER')")
	public ResponseEntity<?> updateMyInfo(@RequestBody UserUpdateRequest request) {
		userService.updateMyInfo(request);
		return ResponseEntity.ok().body("{\"message\": \"Cập nhật thông tin thành công\"}");
	}
	
	//cập nhật avatar
	@PatchMapping("/uploadAvatar")
	@PreAuthorize("hasAnyRole('AUTHOR', 'USER')")
	public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile request) throws IOException {
		Map<String, String> response = new HashMap<>();
		
		String avatarUrl = cloudinaryService.uploadImage(request); 
		userService.updateAvatar(avatarUrl);
		
		response.put("message", "Cập nhật ảnh đại diện thành công");
		response.put("avatarUrl", avatarUrl);
		
		return ResponseEntity.ok().body(response);
	}
}
