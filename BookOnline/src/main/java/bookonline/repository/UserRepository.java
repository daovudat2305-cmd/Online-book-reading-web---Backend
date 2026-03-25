package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, String>{
	boolean existsByUsername(String username);
	boolean existsByEmail(String emmail);
	User findByUsername(String username);
}
