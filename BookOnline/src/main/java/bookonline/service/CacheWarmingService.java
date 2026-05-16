package bookonline.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import bookonline.entity.Book;

@Service
public class CacheWarmingService {

	@Autowired private BookService bookService;
    @Autowired private CacheManager cacheManager; 

    @Async
    public void refreshRecommendationAsync(String username) {
        try {
            System.out.println("Chạy ngầm AI để cập nhật sách đề xuất cho: " + username);
            
            List<Book> newRecommendations = bookService.generateRecommendationsBypassCache(username);
            
            if (newRecommendations != null && !newRecommendations.isEmpty()) {
                Cache cache = cacheManager.getCache("userRecommendations");
                if (cache != null) {
                    cache.put(username, newRecommendations); 
                    System.out.println("Đã ghi đè Cache đề xuất thành công!");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi chạy ngầm làm nóng Cache: " + e.getMessage());
        }
    }
}
