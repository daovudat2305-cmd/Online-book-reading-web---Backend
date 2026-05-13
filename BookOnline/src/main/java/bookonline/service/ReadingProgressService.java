package bookonline.service;

import java.time.LocalDateTime;
import java.util.UUID; // Nhớ import thư viện UUID này nhé

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bookonline.entity.Book;
import bookonline.entity.ReadingProgress;
import bookonline.entity.User;
import bookonline.repository.BookRepository;
import bookonline.repository.ReadingProgressRepository;
import bookonline.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {

    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CacheWarmingService cacheWarmingService;

    // 1. HÀM LƯU LỊCH SỬ (Gọi ngay khi bấm nút "Đọc ngay")
    @Transactional
    public void saveOrUpdateProgress(String username, String bookId, Integer currentPage) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách này!"));
        
        int totalPages = (book.getTotalPages() != null && book.getTotalPages() > 0) ? book.getTotalPages() : 1;
        
        // Nếu số trang user gửi lên mà lớn hơn tổng số trang thực tế
        if (currentPage != null && currentPage > totalPages) {
            currentPage = totalPages;
        }

        String realUserId = user.getUserId();

        ReadingProgress progress = progressRepository.findByUser_UserIdAndBook_BookId(realUserId, bookId)
                .orElse(null);

        if (progress == null) {
            String newId = java.util.UUID.randomUUID().toString();
            int pageToSave = (currentPage != null) ? currentPage : 1;
            
            progressRepository.insertNewProgress(newId, realUserId, bookId, pageToSave, java.time.LocalDateTime.now());
            cacheWarmingService.refreshRecommendationAsync(username);
        } else {
            if (currentPage != null) {
                progressRepository.updateTimeAndPage(progress.getProgressId(), java.time.LocalDateTime.now(), currentPage);
            } else {
                progressRepository.updateTimeOnly(progress.getProgressId(), java.time.LocalDateTime.now());
            }
        }
    }

    // 2. HÀM TĂNG LƯỢT ĐỌC (Gọi sau khi user ở lại 10 giây)
    @Transactional
    public void incrementBookView(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách này!"));

        Integer currentView = book.getViewCount();
        if (currentView == null) currentView = 0;
        
        book.setViewCount(currentView + 1);
        bookRepository.save(book);
    }

    // 3. Hàm lấy danh sách Lịch sử đọc
    public Page<ReadingProgress> getUserReadingHistory(String username, int page, int size) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }

        String realUserId = user.getUserId();
        Pageable pageable = PageRequest.of(page, size);
        return progressRepository.findByUser_UserIdOrderByLastTimeReadDesc(realUserId, pageable);
    }

    // 4. HÀM CỘNG DỒN THỜI GIAN ĐỌC (TÍNH BẰNG GIÂY)
    @Transactional
    public void addReadingTime(String username, String bookId, Long secondsToAdd) {
        User user = userRepository.findByUsername(username);
        
        if (user == null || secondsToAdd == null || secondsToAdd <= 0) {
            return; 
        }

        progressRepository.incrementReadingTime(user.getUserId(), bookId, secondsToAdd);
    }
}