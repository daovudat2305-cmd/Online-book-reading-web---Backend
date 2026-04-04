package bookonline.service;


import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import bookonline.entity.Book;
import bookonline.repository.BookRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CloudinaryService cloudinaryService;

    // 1. Lấy danh sách sách đang CHỜ DUYỆT (status = 0)
    public List<Book> getPendingBooks() {
        return bookRepository.findByStatus(0);
    }

    // 2. Lấy danh sách sách ĐÃ DUYỆT (status = 1)
    public List<Book> getApprovedBooks() {
        return bookRepository.findByStatus(1);
    }

    // 3. Hành động DUYỆT SÁCH (Đổi status thành 1)
    public Book approveBook(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách có ID: " + bookId));
        
        book.setStatus(1);
        book.setMessage(null); // Xóa lời nhắn cũ nếu có khi duyệt lại
        return bookRepository.save(book);
    }

    // 4. Hành động TỪ CHỐI SÁCH (Đổi status thành 2 và lưu lý do)
    public Book rejectBook(String bookId, String reason) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách có ID: " + bookId));
        
        book.setStatus(2);
        book.setMessage(reason); // Lưu lý do từ chối
        return bookRepository.save(book);
    }

    // 5.ĐĂNG SÁCH MỚI
    @Transactional // MỚI THÊM: Bọc giao dịch để đảm bảo lưu sách và thể loại không bị lỗi giữa chừng
    public Book createBook(String title, String description, String authorId, String authorName, 
                           String type, Integer totalPages, 
                           MultipartFile coverFile, MultipartFile pdfFile,
                           List<Integer> categoryIds) throws IOException { // MỚI THÊM: Hứng danh sách thể loại
        
        // Bước 1: Mang file đi up lên Cloudinary và lấy 2 cái link URL về
        String coverUrl = cloudinaryService.uploadImage(coverFile);
        String pdfUrl = cloudinaryService.uploadPdf(pdfFile);
        
        // Bước 2: Tạo một cái ID ngẫu nhiên bằng chữ (chuẩn UUID) cho sách
        String newBookId = UUID.randomUUID().toString();
        
        // Bước 3: Đóng gói toàn bộ nguyên liệu thành 1 cuốn sách
        Book newBook = Book.builder()
                .bookId(newBookId)
                .title(title)
                .description(description)
                .authorId(authorId)
                .authorName(authorName)
                .type(type) // "FREE" hoặc "VIP"
                .fileUrl(pdfUrl) 
                .coverImage(coverUrl) 
                .status(0) // Mặc định vừa đăng là 0 (Chờ duyệt)
                .totalPages(totalPages)
                .viewCount(0) 
                .createdAt(LocalDate.now()) 
                .build();
                
        // Bước 4: Đưa cho thủ kho cất xuống Database bảng Book
        Book savedBook = bookRepository.save(newBook);

        // Bước 5 (MỚI THÊM): Lặp qua từng ID thể loại và cất vào bảng book_category
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Integer catId : categoryIds) {
                bookRepository.insertBookCategory(newBookId, catId);
            }
        }
        
        return savedBook;
    }
    
    // 6. Hành động XÓA SÁCH (Soft delete: Chuyển status sang 3 và lưu lý do xóa)
    public void deleteBookWithReason(String bookId, String reason) {
        try {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sách có ID: " + bookId));
            
            // Thay vì xóa hẳn, ta đổi trạng thái thành 3 và lưu lý do
            book.setStatus(3);
            book.setMessage(reason);
            bookRepository.save(book);
            
        } catch (Exception e) {
            // In toàn bộ lỗi đỏ rực ra màn hình Console của Java
            System.err.println("========== LỖI KHI XÓA SÁCH ==========");
            e.printStackTrace(); 
            throw new RuntimeException("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // 7. Hàm lấy sách theo tác giả (phục vụ phần lịch sử)
    public List<Book> getBooksByAuthorId(String authorId) {
        return bookRepository.findByAuthorId(authorId);
    }
    // 8.Lấy chi tiết 1 cuốn sách (Phục vụ trang chủ và người dùng đọc sách)
    public Book getBookById(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuốn sách có ID: " + bookId));
    }

    // 9. TÍNH NĂNG MỚI: Lọc sách theo nhiều điều kiện kết hợp phân trang

    public Page<Book> filterBooks(String keyword, String categories, String type, Integer status, int page, int size, String sort) {
        // 1. Xử lý sắp xếp (JS gửi lên 'newest' hoặc 'oldest')
    	org.springframework.data.domain.Sort sortOrder = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
    	        .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "bookId")); 

    	if ("oldest".equalsIgnoreCase(sort)) {
    	    sortOrder = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "createdAt")
    	            .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "bookId"));
    	}

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 2. Xử lý danh sách thể loại từ String "1,2" thành List<Integer> [1, 2]
        List<Integer> categoryIdList = null;
        if (categories != null && !categories.trim().isEmpty()) {
            categoryIdList = new ArrayList<>();
            String[] catArray = categories.split(",");
            for (String cat : catArray) {
                try {
                    categoryIdList.add(Integer.parseInt(cat.trim()));
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu ID không phải là số hợp lệ
                }
            }
            if (categoryIdList.isEmpty()) categoryIdList = null; 
        }

        // 3. Xử lý từ khóa và loại truyện (ALL thì gán bằng null để lấy hết)
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        if (type != null && (type.trim().isEmpty() || type.equalsIgnoreCase("ALL"))) type = null;

        // 4. Gọi xuống Repository vừa viết
        return bookRepository.filterBooks(keyword, categoryIdList, type, status, pageable);
    }
}