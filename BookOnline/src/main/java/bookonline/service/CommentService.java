package bookonline.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import bookonline.dto.response.CommentResponse;
import bookonline.entity.Comment;
import bookonline.entity.User;
import bookonline.entity.UserInfo;
import bookonline.repository.BookRepository;
import bookonline.repository.CommentRepository;
import bookonline.repository.UserInfoRepository;
import bookonline.repository.UserRepository;

@Service
public class CommentService {
	@Autowired private CommentRepository commentRepository;
	@Autowired private UserRepository userRepository;
	@Autowired private UserInfoRepository userInfoRepository;
	@Autowired private BookRepository bookRepository;
	@Autowired private CacheWarmingService cacheWarmingService;
	
	public Page<CommentResponse> getCommentsByBookId(String bookId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		
		Page<Comment> listComments = commentRepository.findByBookId(bookId, pageable);
		
		return listComments.map(comment -> {
			User user = userRepository.findByUserId(comment.getUserId());
			UserInfo userInfo = userInfoRepository.findByUserId(comment.getUserId());
			
			CommentResponse response = CommentResponse.builder()
					.commentId(comment.getCommentId())
					.username(user.getUsername())
					.avatar(userInfo.getAvatar())
					.content(comment.getContent())
					.createdAt(comment.getCreatedAt())
					.build();
			return response;
		});
	}
	
	public Map<String, String> createCommentBook(String bookId, String content) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		User user = userRepository.findByUsername(username);
		if(user==null) {
			throw new RuntimeException("Người dùng không tồn tại");
		}
		
		Comment comment = Comment.builder()
				.userId(user.getUserId())
				.bookId(bookId)
				.content(content)
				.createdAt(LocalDateTime.now())
				.user(user)
				.book(bookRepository.findByBookId(bookId))
				.build();
		
		commentRepository.save(comment);
		cacheWarmingService.refreshRecommendationAsync(username);
		return Map.of("success", "Bình luận thành công");
	}
	
	public Map<String,String> deleteCommentByCommentId(String commentId, String username) {
		User user = userRepository.findByUsername(username);
		Comment comment = commentRepository.findByCommentId(commentId);
		
		if(!user.getUserId().equals(comment.getUserId())) {
			throw new RuntimeException("Bạn không thể xóa bình luận của người khác");
		}
		
		commentRepository.delete(comment);
		cacheWarmingService.refreshRecommendationAsync(username);
		return Map.of("success", "Đã xóa bình luận!");
	}
}
