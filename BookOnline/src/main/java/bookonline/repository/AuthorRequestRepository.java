package bookonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bookonline.entity.AuthorRequest;
import java.util.List;



@Repository
public interface AuthorRequestRepository extends JpaRepository<AuthorRequest, String>{
	boolean existsByAuthorId(String authorId);
	AuthorRequest findByAuthorId(String authorId);
	AuthorRequest findByRequestId(String requestId);
	List<AuthorRequest> findAllByIsDelete(int delete);
	Page<AuthorRequest> findAllByIsDelete(int deleteStatus, Pageable pageable);
	
	@Query(
        value = "SELECT a.requestId, a.authorId, a.description, a.createdAt as createdAt, a.reviewAt, a.status, a.isDelete " +
                "FROM authorrequest a " +
                "JOIN userinfo u ON u.userId = a.authorId " +
                "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.isDelete=0 ",
        countQuery = "SELECT COUNT(authorrequest.userId) " +
                     "FROM authorrequest " +
                     "JOIN userinfo u ON u.userId = a.authorId " +
                     "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.isDelete=0",    
        nativeQuery = true
    )
	Page<AuthorRequest> findAuthorRequestByName(@Param("keyword") String keyword, Pageable pageable);
}
