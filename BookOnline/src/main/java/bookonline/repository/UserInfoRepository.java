package bookonline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bookonline.entity.UserInfo;


@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String>{
	UserInfo findByUserId(String userId);
	
	@Query(
        value = "SELECT userinfo.userId, userinfo.fullName as fullName, userinfo.dob, userinfo.gender, userinfo.bankAccount, userinfo.avatar  " +
                "FROM userinfo " +
                "JOIN user ON user.userId = userinfo.userId AND user.role = 'AUTHOR' " +
                "WHERE userinfo.fullName LIKE CONCAT('%', :keyword, '%')",
        countQuery = "SELECT COUNT(userinfo.userId) " +
                     "FROM userinfo " +
                     "JOIN user ON user.userId = userinfo.userId AND user.role = 'AUTHOR' " +
                     "WHERE userinfo.fullName LIKE CONCAT('%', :keyword, '%')",    
        nativeQuery = true
    )
    Page<UserInfo> findAuthorsByKeyword(
        @Param("keyword") String keyword, 
        Pageable pageable
    );
}
