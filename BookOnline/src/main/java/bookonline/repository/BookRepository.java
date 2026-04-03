package bookonline.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import bookonline.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
	Book findByBookId(String bookId);

    // 1. Tìm tất cả sách theo trạng thái (0: Chờ duyệt, 1: Đã duyệt, 2: Từ chối, 3: Đã xóa)
    List<Book> findByStatus(Integer status);

    // 2. Tìm danh sách sách của một tác giả cụ thể (phục vụ phần Lịch sử đăng sách)
    List<Book> findByAuthorId(String authorId);

    // 3. Lưu nhiều thể loại cho 1 cuốn sách (Native Query để chèn vào bảng trung gian)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO book_category (bookId, categoryId) VALUES (:bookId, :categoryId)", nativeQuery = true)
    void insertBookCategory(@Param("bookId") String bookId, @Param("categoryId") Integer categoryId);
    // phân trang
    Page<Book> findByStatus(int status, Pageable pageable);
}