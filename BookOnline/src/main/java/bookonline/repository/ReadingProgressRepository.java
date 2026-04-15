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

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE ReadingProgress r SET r.totalReadingTimeSeconds = coalesce(r.totalReadingTimeSeconds, 0) + :seconds WHERE r.user.userId = :userId AND r.book.bookId = :bookId")
    void incrementReadingTime(@org.springframework.data.repository.query.Param("userId") String userId, 
                              @org.springframework.data.repository.query.Param("bookId") String bookId, 
                              @org.springframework.data.repository.query.Param("seconds") Long seconds);
    
    // CHỈ CẬP NHẬT THỜI GIAN (Dùng khi mở sách)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE ReadingProgress r SET r.lastTimeRead = :now WHERE r.progressId = :id")
    void updateTimeOnly(@org.springframework.data.repository.query.Param("id") String id, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);

    // CẬP NHẬT CẢ THỜI GIAN LẪN TRANG (Dùng khi bấm Lưu trang)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE ReadingProgress r SET r.lastTimeRead = :now, r.currentPage = :page WHERE r.progressId = :id")
    void updateTimeAndPage(@org.springframework.data.repository.query.Param("id") String id, @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now, @org.springframework.data.repository.query.Param("page") Integer page);
    

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "INSERT INTO reading_progress (progressId, userId, bookId, currentPage, isCounted, lastTimeRead, total_reading_time_seconds) VALUES (:id, :userId, :bookId, :page, 0, :now, 0)", nativeQuery = true)
    void insertNewProgress(
            @org.springframework.data.repository.query.Param("id") String id, 
            @org.springframework.data.repository.query.Param("userId") String userId, 
            @org.springframework.data.repository.query.Param("bookId") String bookId, 
            @org.springframework.data.repository.query.Param("page") Integer page, 
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now
    );
}