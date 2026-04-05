package bookonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import bookonline.entity.Comment;



public interface CommentRepository extends JpaRepository<Comment, String>{
	Comment findByCommentId(String commentId);
	Page<Comment> findByBookId(String bookId, Pageable pageable);
	long countByBookId(String bookId);
}
