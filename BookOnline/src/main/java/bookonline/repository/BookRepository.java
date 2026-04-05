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
    
    
    //xếp hạng sách
    @Query(value = """
            WITH sub1 AS (
                SELECT b.bookId, COUNT(c.commentId) AS countComment
                FROM book b 
                LEFT JOIN comment c ON b.bookId = c.bookId
                WHERE b.status = 1
                GROUP BY b.bookId 
            ), 
            sub2 AS (
                SELECT sub1.bookId, sub1.countComment, COUNT(f.favoriteId) AS countFavo
                FROM sub1
                LEFT JOIN favorite f ON sub1.bookId = f.bookId 
                GROUP BY sub1.bookId, sub1.countComment 
            )
            SELECT b.* FROM book b 
            INNER JOIN sub2 ON b.bookId = sub2.bookId
            ORDER BY (COALESCE(b.viewCount, 0) + sub2.countComment + sub2.countFavo) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Book> findTop15BooksSortedByRating();
    
    //danh sách thể loại theo bookId
    @Query(value = """
            SELECT c.categoryName 
            FROM category c 
            JOIN book_category bc ON c.categoryId = bc.categoryId 
            WHERE bc.bookId = :bookId
            """, nativeQuery = true)
    List<String> findCategoryNamesByBookId(@Param("bookId") String bookId);
    
    //tìm theo tác giả và trạng thái đã duyệt
    List<Book> findByAuthorNameAndStatus(String authorName, Integer status);
}