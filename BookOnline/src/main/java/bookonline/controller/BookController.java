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

    // 1. Lấy chi tiết 1 cuốn sách để xem thông tin & đọc PDF
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookDetail(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
    // 2. lọc thể loại và phân trang(trong trang lọc thể loại) 
    @GetMapping("/filter")
    public ResponseEntity<Page<Book>> getFilteredBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categories, // Nhận chuỗi "1,2" từ JS
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        
        // Gọi Service xử lý
        Page<Book> result = bookService.filterBooks(keyword, categories, type, 1, page, size, sort);
        	
        // Trả về cho Frontend
        return ResponseEntity.ok(result);
    }
    
    //xếp hạng sách
    @GetMapping("/rank")
    public ResponseEntity<?> bookRanking() {
    	return ResponseEntity.ok().body(bookService.findTop10Books());
    }
    
    //lay sach de xuat
    @GetMapping("/recommend")
    public ResponseEntity<?> getRecommendBooks(@RequestParam(defaultValue = "") String username) {
    	return ResponseEntity.ok().body(bookService.getRecommendBooks(username));
    }

}