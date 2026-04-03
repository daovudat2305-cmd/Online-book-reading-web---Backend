package bookonline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.service.CommentService;

@RestController
@RequestMapping("/comments")
public class CommentController {
	
	@Autowired private CommentService commentService;
	
	@GetMapping("/{bookId}")
	public ResponseEntity<?> getCommentsByBookId(
			@RequestParam(defaultValue = "0") int page, 
			@RequestParam(defaultValue = "15") int size,
			@PathVariable String bookId) {
		return ResponseEntity.ok().body(commentService.getCommentsByBookId(bookId, page, size));
	}
	
	@PostMapping("/{bookId}")
	public ResponseEntity<?> createCommentBook(@PathVariable String bookId, @RequestBody String content) {
		return ResponseEntity.ok().body(commentService.createCommentBook(bookId, content));
	}
	
	@DeleteMapping("/{commentId}")
	public ResponseEntity<?> deleteComment(@PathVariable String commentId, @RequestParam String username) {
		return ResponseEntity.ok().body(commentService.deleteCommentByCommentId(commentId, username));
	}
}
