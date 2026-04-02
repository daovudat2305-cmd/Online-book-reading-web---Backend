package bookonline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import bookonline.entity.Book;
import bookonline.entity.User;
import bookonline.repository.UserRepository;
import bookonline.service.BookService;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/author/books")     
@RequiredArgsConstructor        
public class AuthorBookController {

    private final BookService bookService;
    
    private final UserRepository userRepository; 

    @PostMapping("/create")
    public ResponseEntity<?> createBook(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("totalPages") Integer totalPages,
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("categoryIds") List<Integer> categoryIds
    ) {
        
        // 1. Kiểm tra rỗng
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng điền đầy đủ thông tin chữ của cuốn sách!");
        }

        if (coverImage == null || coverImage.isEmpty() || pdfFile == null || pdfFile.isEmpty()) {
            throw new RuntimeException("Vui lòng đính kèm đầy đủ Ảnh bìa và File PDF!");
        }

        try {
            // 2. Lấy thông tin Tác giả từ Token bảo mật
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName(); 
            
            User user = userRepository.findByUsername(username); 
            
            if (user == null) {
                throw new RuntimeException("Không xác định được danh tính tác giả!");
            }

            // Lấy ID và Username.
            String authorId = user.getUserId(); 
            String authorName = user.getUsername(); 

            // 3. Gọi Service để lưu
            Book savedBook = bookService.createBook(
                    title, description, authorId, authorName, type, totalPages, coverImage, pdfFile, categoryIds
            );
            
            return ResponseEntity.ok(savedBook); 
            
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu sách vào hệ thống: " + e.getMessage());
        }
    }
 // LẤY LỊCH SỬ ĐĂNG SÁCH
    @org.springframework.web.bind.annotation.GetMapping("/my-history")
    public ResponseEntity<?> getMyBookHistory() {
        try {
            // 1. Lấy username từ Token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 2. Tìm User
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body("Không xác định được danh tính!");
            }

            // 3. Gọi Service để lấy danh sách sách theo authorId
            // Giả sử trong BookService đã có hàm findByAuthorId
            List<Book> myBooks = bookService.getBooksByAuthorId(user.getUserId());
            
            return ResponseEntity.ok(myBooks);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
    }
}