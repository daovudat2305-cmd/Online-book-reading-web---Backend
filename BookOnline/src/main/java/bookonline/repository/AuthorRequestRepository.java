package bookonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
