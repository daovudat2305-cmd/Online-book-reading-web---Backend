package bookonline.service;

import java.time.LocalDateTime;

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

    // 1. HÀM LƯU LỊCH SỬ (Gọi ngay khi bấm nút "Đọc ngay")
    // Hàm này bây giờ CHỈ LO lưu tiến trình, KHÔNG cộng view nữa.
    @Transactional
    public void saveOrUpdateProgress(String username, String bookId, Integer currentPage) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách này!"));

        String realUserId = user.getUserId();

        ReadingProgress progress = progressRepository.findByUser_UserIdAndBook_BookId(realUserId, bookId)
                .orElse(null);

        if (progress == null) {
            // NẾU CHƯA CÓ LỊCH SỬ: Tạo mới và mặc định là trang 1 (hoặc trang được gửi lên)
            progress = ReadingProgress.builder()
                    .user(user)
                    .book(book)
                    .currentPage(currentPage != null ? currentPage : 1)
                    .lastTimeRead(LocalDateTime.now())
                    .totalReadingTimeSeconds(0L) // 🟢 THÊM MỚI: Khởi tạo thời gian đọc bằng 0 giây
                    .build();
        } else {
            // NẾU ĐÃ CÓ LỊCH SỬ:
            // 1. Luôn cập nhật thời gian mới nhất
            progress.setLastTimeRead(LocalDateTime.now());
            
            // 2. CHỈ CẬP NHẬT SỐ TRANG NẾU CÓ TRUYỀN XUỐNG (KHÔNG PHẢI NULL)
            if (currentPage != null) {
                progress.setCurrentPage(currentPage);
            }
            // Nếu currentPage là null, nó sẽ giữ nguyên giá trị cũ trong DB (ví dụ trang 50)
        }

        progressRepository.save(progress);
    }

    // 2. HÀM TĂNG LƯỢT ĐỌC RIÊNG BIỆT (Gọi sau khi user ở lại 10 giây)
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
        if (user == null) return;

        ReadingProgress progress = progressRepository
                .findByUser_UserIdAndBook_BookId(user.getUserId(), bookId)
                .orElse(null);

        // Chỉ cộng thời gian nếu lịch sử đã tồn tại và số giây truyền lên > 0
        if (progress != null && secondsToAdd != null && secondsToAdd > 0) {
            Long currentSeconds = progress.getTotalReadingTimeSeconds();
            if (currentSeconds == null) currentSeconds = 0L;
            
            // Cộng dồn thời gian cũ và mới
            progress.setTotalReadingTimeSeconds(currentSeconds + secondsToAdd);
            progressRepository.save(progress);
        }
    }
}