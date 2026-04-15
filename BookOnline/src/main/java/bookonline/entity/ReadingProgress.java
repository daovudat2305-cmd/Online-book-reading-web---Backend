package bookonline.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "reading_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    private String progressId;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "bookId")
    private Book book;

    private Integer currentPage;
    
    private Integer isCounted; 
    
    private LocalDateTime lastTimeRead; 

    @Column(name = "total_reading_time_seconds")
    private Long totalReadingTimeSeconds = 0L; // Mặc định là 0 giây
}