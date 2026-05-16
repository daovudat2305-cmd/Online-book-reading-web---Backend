package bookonline.service;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import bookonline.dto.response.BookRankResponse;
import bookonline.entity.Book;
import bookonline.entity.BookEarning;
import bookonline.entity.User;
import bookonline.repository.BookEarningRepository;
import bookonline.repository.BookRepository;
import bookonline.repository.CommentRepository;
import bookonline.repository.FavoriteRepository;
import bookonline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CloudinaryService cloudinaryService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private BookEarningRepository bookEarningRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RecommendationService recommendationService;
    
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
        
        //lưu vào bookearning để tính tiền
        if(book.getType().equalsIgnoreCase("VIP")) {
        	BookEarning bookEarning = BookEarning.builder()
        			.authorId(book.getAuthorId())
        			.bookId(bookId)
        			.amount(10000)
        			.status("UNPAID")
        			.build();
        	bookEarningRepository.save(bookEarning);
        }
        
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
    @Transactional 
    public Book createBook(String title, String description, String authorId, String authorName, 
                           String type, Integer totalPages, 
                           MultipartFile coverFile, MultipartFile pdfFile,
                           List<Integer> categoryIds) throws IOException { 
        
        String coverUrl = cloudinaryService.uploadImage(coverFile);
        String pdfUrl = cloudinaryService.uploadPdf(pdfFile);
        
        String newBookId = UUID.randomUUID().toString();
        
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
                
        Book savedBook = bookRepository.save(newBook);

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
            
            book.setStatus(3);
            book.setMessage(reason);
            bookRepository.save(book);
            
        } catch (Exception e) {
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

    // 9. Lọc sách theo nhiều điều kiện kết hợp phân trang
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
    
    //xếp hạng 10 quyển sách rating cao nhất
    public List<BookRankResponse> findTop10Books() {
    	List<Book> listBooks = bookRepository.findTop10BooksSortedByRating();
    	
    	List<BookRankResponse> response = new ArrayList<>(); 
    	
    	for(Book book : listBooks) {
    		long commentCount = commentRepository.countByBookId(book.getBookId());
    		long favoCount = favoriteRepository.countByBookId(book.getBookId());
    		List<String> categories = bookRepository.findCategoryNamesByBookId(book.getBookId());
    		BookRankResponse item = BookRankResponse.builder()
    				.bookId(book.getBookId())
    				.title(book.getTitle())
    				.authorName(book.getAuthorName())
    				.type(book.getType())
    				.coverImage(book.getCoverImage())
    				.viewCount(book.getViewCount())
    				.commentCount(commentCount)
    				.favoCount(favoCount)
    				.categories(categories)
    				.build();
    		response.add(item);
    				
    	}
    	
    	return response;
    }
    
    //lấy sách đăng bởi tác giả (đã được duyệt) để admin xem trong thống kê
    public List<Book> getBookByAuthorName(String authorName) {
    	return bookRepository.findByAuthorNameAndStatus(authorName, 1);
    }
    
    // đề xuất sách
    @Cacheable(value = "userRecommendations", key = "#username", unless = "#result.size() == 0")
    public List<Book> getRecommendBooks(String username) {
        return generateRecommendationsBypassCache(username);
    }
    
    public List<Book> generateRecommendationsBypassCache(String username) {
        if (username == null || username.trim().isEmpty()) {
            return bookRepository.findTop10BooksSortedByRating();
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return bookRepository.findTop10BooksSortedByRating();
        }
        
        String userId = user.getUserId();
        
        List<String> readDeeplyIds = bookRepository.findBookIdsReadDeeplyIn30Days(userId);
        List<String> readShallowlyIds = bookRepository.findBookIdsReadShallowlyIn30Days(userId);
        List<String> favIds = bookRepository.findBookIdsFavoritedIn30Days(userId);
        List<String> cmtIds = bookRepository.findBookIdsCommentedIn30Days(userId);
        
        if (readDeeplyIds.isEmpty() && readShallowlyIds.isEmpty() && favIds.isEmpty() && cmtIds.isEmpty()) {
            return bookRepository.findTop10BooksSortedByRating();
        }
        
        List<Book> candidateBooks = bookRepository.findCandidateBooks(userId);
        
        if (candidateBooks.isEmpty()) {
            return bookRepository.findTop10BooksSortedByRating();
        }

        // lấy thông tin sách
        List<Book> favBooks = bookRepository.findAllById(favIds);
        List<Book> deepReadBooks = bookRepository.findAllById(readDeeplyIds);
        List<Book> shallowReadBooks = bookRepository.findAllById(readShallowlyIds);
        List<Book> cmtBooks = bookRepository.findAllById(cmtIds);

        // gọi GROQ AI (Reranking)
        List<String> aiRecommendedIds = recommendationService.getAiRecommendedBookIds(
                favBooks, deepReadBooks, shallowReadBooks, cmtBooks, candidateBooks
        );

        // xử lý kết quả tra về
        if (aiRecommendedIds != null && !aiRecommendedIds.isEmpty()) {
            // Lấy sách từ DB dựa trên list ID mà AI đã chốt
            List<Book> finalBooks = bookRepository.findAllById(aiRecommendedIds);
            
            //lấy sách theo thứ tự trả về
            return aiRecommendedIds.stream()
                    .map(id -> finalBooks.stream()
                               .filter(b -> b.getBookId().equals(id))
                               .findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
        }

        System.out.println("⚠️ Groq AI không trả về kết quả, dùng fallback SQL.");
        return candidateBooks.stream().limit(10).toList();
    }
    
}