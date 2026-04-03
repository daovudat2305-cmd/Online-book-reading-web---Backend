package bookonline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.service.FavoriteService;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
	@Autowired private FavoriteService favoriteService;
	
	@GetMapping("/{bookId}/count")
	public ResponseEntity<?> countFavoritesByBook(@PathVariable String bookId) {
		return ResponseEntity.ok().body(favoriteService.countFavoritesByBookId(bookId));
	}
	
	@GetMapping("/{bookId}")
	public ResponseEntity<?> statusFavorite(@PathVariable String bookId, @RequestParam String username) {
		return ResponseEntity.ok().body(favoriteService.currentFavoriteByBookAndUsername(bookId, username));
	}
	
	@PostMapping("/{bookId}")
	public ResponseEntity<?> toggleFavorite(@PathVariable String bookId, @RequestParam String username) {
		return ResponseEntity.ok().body(favoriteService.toggleFavorite(bookId,username));
	}
}
