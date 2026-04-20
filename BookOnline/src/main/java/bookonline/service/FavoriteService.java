package bookonline.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bookonline.entity.Book;
import bookonline.entity.Favorite;
import bookonline.entity.User;
import bookonline.repository.BookRepository;
import bookonline.repository.FavoriteRepository;
import bookonline.repository.UserRepository;

@Service
public class FavoriteService {
	@Autowired private UserRepository userRepository;
	@Autowired private FavoriteRepository favoriteRepository;
	@Autowired private BookRepository bookRepository;
	@Autowired private CacheWarmingService cacheWarmingService;
	
	//tính lượt yêu thích
	public Map<String,Long> countFavoritesByBookId(String bookId) {
		long favorites = favoriteRepository.countByBookId(bookId);
		return Map.of("favorites", favorites);
	}
	
	//xem tình trạng yêu thích hiện tại
	public Map<String,Boolean> currentFavoriteByBookAndUsername(String bookId, String username) {
		User user = userRepository.findByUsername(username);
		Favorite favorite = favoriteRepository.findByUserIdAndBookId(user.getUserId(), bookId);
		Boolean status = false;
		if(favorite!=null) {
			status = true;
		}
		return Map.of("status", status);
	}
	
	public Map<String, String> toggleFavorite(String bookId, String username) {
		User user = userRepository.findByUsername(username);
		Book book = bookRepository.findByBookId(bookId);
		Favorite favorite = favoriteRepository.findByUserIdAndBookId(user.getUserId(), bookId);
		String status = "";
		if(favorite==null) {
			favorite = Favorite.builder()
					.userId(user.getUserId())
					.bookId(bookId)
					.createdTime(LocalDateTime.now())
					.user(user)
					.book(book)
					.build();
			favoriteRepository.save(favorite);
			status = "add";
		}
		else {
			favoriteRepository.delete(favorite);
			status = "delete";
		}
		cacheWarmingService.refreshRecommendationAsync(username);
		return Map.of("status", status);
	}

}
