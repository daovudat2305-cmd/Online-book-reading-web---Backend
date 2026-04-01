package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.UserVip;
import java.util.List;
import java.time.LocalDateTime;



@Repository
public interface UserVipRepository extends JpaRepository<UserVip, String> {
	UserVip findByUserId(String userId);
	
	List<UserVip> findByEndDateBefore(LocalDateTime dateTime);
}
