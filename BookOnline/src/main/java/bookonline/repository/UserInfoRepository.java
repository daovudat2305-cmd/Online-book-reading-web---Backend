package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.UserInfo;


@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String>{
	UserInfo findByUserId(String userId);
}
