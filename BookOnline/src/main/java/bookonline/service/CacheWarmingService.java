package bookonline.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CacheWarmingService {
	@Autowired private BookService bookService;

    // xóa cache cũ, rẽ nhánh chạy ngầm gọi AI lấy cache mới
    @CacheEvict(value = "userRecommendations", key = "#username")
    @Async
    public void refreshRecommendationAsync(String username) {
        bookService.getRecommendBooks(username);
    }
}
