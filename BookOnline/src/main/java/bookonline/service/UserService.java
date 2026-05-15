package bookonline.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import bookonline.dto.request.UserCreationRequest;
import bookonline.dto.request.UserUpdateRequest;
import bookonline.dto.response.UserProfileResponse;
import bookonline.dto.response.UserVipResponse;
import bookonline.entity.User;
import bookonline.entity.UserInfo;
import bookonline.entity.UserVip;
import bookonline.repository.UserInfoRepository;
import bookonline.repository.UserRepository;
import bookonline.repository.UserVipRepository;

@Service
public class UserService {
	
	@Autowired private PasswordEncoder passwordEncoder;
	
	@Autowired private UserRepository userRepository;
	
	@Autowired private UserInfoRepository userInfoRepository;
	
	@Autowired private UserVipRepository userVipRepository;
	
	UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
	
	public Map<String,String> createUser(UserCreationRequest request) {
		if(userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email đã tồn tại");
		}
		
		if(userRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Tên đăng nhập đã tồn tại");
		}
		
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
		
		User user = User.builder()
				.username(request.getUsername())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.role("USER")
				.createdAt(LocalDate.now())
				.build();
		UserInfo userInfo = new UserInfo();
		userInfo.setUser(user);
		userInfo.setAvatar("http://res.cloudinary.com/dnkm4kqa9/image/upload/v1774541408/vqha0vf7wy9sckb8rkxr.jpg");
		user.setUserInfo(userInfo);
		
		userRepository.save(user);
		Map<String,String> response = new HashMap<>();
		response.put("message", "Đăng ký tài khoản thành công!");
		
		return response;
	}
	
	@PostAuthorize("returnObject.username == authentication.name")
	public UserProfileResponse getMyInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		
		User user = userRepository.findByUsername(username);
		if(user == null) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}
		
		UserInfo userInfo = userInfoRepository.findByUserId(user.getUserId());
		UserVip userVip = userVipRepository.findByUserId(user.getUserId());

		UserVipResponse userVipDetail = null;
		boolean isVip = false;
		if(userVip!=null) {
			if (userVip.getEndDate().isBefore(LocalDateTime.now())) {
		        // Nếu ngày kết thúc nằm TRƯỚC hiện tại -> Đã hết hạn
		        userVipRepository.delete(userVip);
		    }
			else {
				long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), userVip.getEndDate().toLocalDate());
				isVip = true;
				userVipDetail = UserVipResponse.builder()
						.vipName(userVip.getVipId())
						.startDate(userVip.getStartDate())
						.endDate(userVip.getEndDate())
						.daysRemaining(daysRemaining)
						.build();
			}
		}
		
		
		UserProfileResponse response = UserProfileResponse.builder()
				.username(username)
				.fullName(userInfo.getFullName())
				.email(user.getEmail())
				.dob(userInfo.getDob())
				.role(user.getRole())
				.gender(userInfo.getGender())
				.avatar(userInfo.getAvatar())
				.bankAccount(userInfo.getBankAccount())
				.isVip(isVip)
				.vipDetail(userVipDetail)
				.build();
		
		return response;
	}
	
	public void updateMyInfo(UserUpdateRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		
		User user = userRepository.findByUsername(username);
		if(user == null) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}
		
		UserInfo userInfo = userInfoRepository.findByUserId(user.getUserId());
		// Cập nhật thông tin
		if(request.getBankAccount() != null && !request.getBankAccount().trim().isEmpty()) {
			userInfo.setBankAccount(request.getBankAccount());
		}
		
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            userInfo.setFullName(request.getFullName());
        }
        
        if (request.getDob() != null) {
            userInfo.setDob(request.getDob());
        }
        
        if (request.getGender() != null) {
            userInfo.setGender(request.getGender());
        }
        
        userInfoRepository.save(userInfo);
	}
	
	public void updateAvatar(String avatarUrl) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		
		User user = userRepository.findByUsername(username);
		if(user == null) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}
		
		UserInfo userInfo = userInfoRepository.findByUserId(user.getUserId());
		userInfo.setAvatar(avatarUrl);
		
		userInfoRepository.save(userInfo);
	}
}
