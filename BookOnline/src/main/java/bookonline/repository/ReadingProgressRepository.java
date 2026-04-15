package bookonline.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.ReadingProgress;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, String> {

    // 1. Phục vụ trang "LỊCH SỬ": 
    // Tìm tất cả sách user đã đọc, sắp xếp theo thời gian đọc gần nhất (mới nhất lên đầu)
    Page<ReadingProgress> findByUser_UserIdOrderByLastTimeReadDesc(String userId, Pageable pageable);

    // 2. Phục vụ nút "ĐỌC SÁCH": 
    // Kiểm tra xem User này đã từng đọc Cuốn sách này chưa (để biết đường tạo mới hoặc cập nhật trang)
    Optional<ReadingProgress> findByUser_UserIdAndBook_BookId(String userId, String bookId);
}