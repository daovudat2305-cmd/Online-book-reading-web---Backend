package bookonline.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, String>{
	boolean existsByUsername(String username);
	boolean existsByEmail(String emmail);
	User findByUsername(String username);
	User findByUserId(String userId);
	List<User> findAllByRole(String role);
	Page<User> findAllByRole(String role, Pageable pageable);
}
