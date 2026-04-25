package bookonline.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    @Column(name = "bookId", length = 50)
    private String bookId; 

    private String title;

    @Column(columnDefinition = "TEXT") 
    private String description;
    
    @Column(name = "authorId", length = 50)
    private String authorId;
    
    private String type;
    
    @Column(name = "fileUrl")
    private String fileUrl;
    
    @Column(name = "coverImage")
    private String coverImage;
    
    /**
     * TRẠNG THÁI SÁCH:
     * 0: Đang chờ duyệt (Pending)
     * 1: Đã duyệt - Đang hiển thị (Approved)
     * 2: Bị từ chối duyệt (Rejected)
     * 3: Đã bị Admin xóa (Deleted)
     */
    private Integer status; 
    
    // Lưu lý do từ chối hoặc lý do xóa từ Admin
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "totalPages")
    private Integer totalPages;
    
    @Column(name = "viewCount")
    private Integer viewCount;
    
    @Column(name = "createdAt")
    private LocalDate createdAt;
    
    @Column(name = "authorName")
    private String authorName;

    // KẾT NỐI VỚI BẢNG THỂ LOẠI
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "book_category", 
        joinColumns = @JoinColumn(name = "bookId"), 
        inverseJoinColumns = @JoinColumn(name = "categoryId") 
    )
    private List<Category> categories;
}