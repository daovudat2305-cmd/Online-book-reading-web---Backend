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


    // Lọc sách kết hợp phân trang cho trang chủ / trang thể loại
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.categories c " +
            "WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryIds IS NULL OR c.categoryId IN :categoryIds) " +
            "AND (:type IS NULL OR b.type = :type) " +
            "AND (:status IS NULL OR b.status = :status)")
     Page<Book> filterBooks(@Param("keyword") String keyword, 
                            @Param("categoryIds") List<Integer> categoryIds, 
                            @Param("type") String type, 
                            @Param("status") Integer status, 
                            Pageable pageable);
}