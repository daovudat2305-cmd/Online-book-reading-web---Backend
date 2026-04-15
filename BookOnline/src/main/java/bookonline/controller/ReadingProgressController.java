package bookonline.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.entity.ReadingProgress;
import bookonline.service.ReadingProgressService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/history")
@RequiredArgsConstructor
public class ReadingProgressController {

    private final ReadingProgressService progressService;

    // 1. API: LƯU HOẶC CẬP NHẬT LỊCH SỬ ĐỌC
    // Sẽ được gọi ngầm mỗi khi user lật trang hoặc bấm "Đọc sách"
    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(
            @RequestParam("bookId") String bookId,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            Principal principal) {
        
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập!");
            }
            
            // Lấy ID người dùng từ Token đăng nhập
            String userId = principal.getName(); 
            
            progressService.saveOrUpdateProgress(userId, bookId, currentPage);
            return ResponseEntity.ok("Đã lưu lịch sử đọc thành công!");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // API MỚI: TĂNG LƯỢT ĐỌC (Sẽ được gọi từ Frontend sau khi user ở lại trang 10 giây)
    @PostMapping("/{bookId}/increment-view")
    public ResponseEntity<?> incrementView(@PathVariable("bookId") String bookId) {
        try {
            progressService.incrementBookView(bookId);
            return ResponseEntity.ok("Đã tăng lượt đọc thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    // API CỘNG DỒN THỜI GIAN ĐỌC

    @PostMapping("/{bookId}/add-time")
    public ResponseEntity<?> addReadingTime(
            @PathVariable("bookId") String bookId,
            @RequestParam("seconds") Long seconds,
            Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập!");
            }
            
            // Lấy username (userId) từ Token
            String userId = principal.getName();
            
            // Gọi Service để cộng dồn số giây
            progressService.addReadingTime(userId, bookId, seconds);
            
            return ResponseEntity.ok("Đã cộng thời gian đọc thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    // 2. API: LẤY DANH SÁCH LỊCH SỬ ĐỌC
    @GetMapping("/list")
    public ResponseEntity<?> getReadingHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Principal principal) {
        
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("Vui lòng đăng nhập!");
            }

            // Lấy ID người dùng từ Token đăng nhập
            String userId = principal.getName();
            
            Page<ReadingProgress> history = progressService.getUserReadingHistory(userId, page, size);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}