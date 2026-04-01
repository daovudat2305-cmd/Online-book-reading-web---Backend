package bookonline.controller;

import bookonline.entity.Book;
import bookonline.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books") // Đường dẫn dành cho người đọc (đã đăng nhập)
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    // 1. Lấy danh sách sách đã duyệt để hiện ở Trang chủ
    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllApprovedBooks() {
        // Gọi lại hàm lấy sách status = 1 trong Service
        return ResponseEntity.ok(bookService.getApprovedBooks());
    }

    // 2. Lấy chi tiết 1 cuốn sách để xem thông tin & đọc PDF
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookDetail(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}