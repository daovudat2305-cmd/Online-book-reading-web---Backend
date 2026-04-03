package bookonline.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bookonline.entity.Book;
import bookonline.service.BookService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/books") 
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

 // 1. Lấy danh sách sách đã duyệt CÓ PHÂN TRANG (20 quyển/trang)
    @GetMapping("/all")
    public ResponseEntity<Page<Book>> getAllApprovedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Gọi hàm phân trang mới trong Service
        return ResponseEntity.ok(bookService.getApprovedBooksPaged(page, size));
    }

    // 2. Lấy chi tiết 1 cuốn sách để xem thông tin & đọc PDF
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookDetail(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}