package bookonline.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import bookonline.dto.request.AuthorRegisterRequest;
import bookonline.dto.response.AuthorInfoResponse;
import bookonline.dto.response.AuthorRegisterReponse;
import bookonline.entity.AuthorRequest;
import bookonline.entity.User;
import bookonline.entity.UserInfo;
import bookonline.repository.AuthorRequestRepository;
import bookonline.repository.UserInfoRepository;
import bookonline.repository.UserRepository;

@Service
public class AuthorService {
	@Autowired private AuthorRequestRepository authorRequestRepository;
	@Autowired private UserInfoRepository userInfoRepository;
	@Autowired private UserRepository userRepository;
	
	public Map<String, String> authorRegister(AuthorRegisterRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		
		User user = userRepository.findByUsername(username);
		if(user == null) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}
		
		if(authorRequestRepository.existsByAuthorId(user.getUserId())) {
			Map<String,String> response = new HashMap<>();
			response.put("message", "Bạn đã nộp đơn đăng ký trước đó!");
			return response;
		}
		
		UserInfo userInfo = userInfoRepository.findByUserId(user.getUserId());
		userInfo.setFullName(request.getFullName());
		userInfo.setDob(request.getDob());
		userInfo.setGender(request.getGender());
		userInfo.setBankAccount(request.getBankAccount());
		
		AuthorRequest authorRequest = AuthorRequest.builder()
				.authorId(user.getUserId())
				.createdAt(LocalDate.now())
				.description(request.getDescription())
				.status("PENDING")
				.isDelete(0)
				.build();
		authorRequestRepository.save(authorRequest);
		
		Map<String,String> response = new HashMap<>();
		response.put("message", "Đã nộp đơn đăng ký!");
		return response;
	}
	
	public AuthorRegisterReponse getRegisterReponse() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName(); 
		
		User user = userRepository.findByUsername(username);
		if(user == null) {
			throw new RuntimeException("Không tìm thấy người dùng");
		}
		
		UserInfo userInfo = userInfoRepository.findByUserId(user.getUserId());
		AuthorRegisterReponse response = AuthorRegisterReponse.builder()
				.fullName(userInfo.getFullName())
				.email(user.getEmail())
				.gender(userInfo.getGender())
				.dob(userInfo.getDob())
				.bankAccount(userInfo.getBankAccount())
				.build();
		if(authorRequestRepository.existsByAuthorId(user.getUserId())) {
			AuthorRequest authorRequest = authorRequestRepository.findByAuthorId(user.getUserId());
			response.setRequestId(authorRequest.getRequestId());
			response.setCreatedAt(authorRequest.getCreatedAt());
			response.setReviewAt(authorRequest.getReviewAt());
			response.setStatus(authorRequest.getStatus());
			response.setDescription(authorRequest.getDescription());
		}
		return response;
	}
	
	public Page<AuthorRegisterReponse> getAllRequests(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<AuthorRequest> requestPage = authorRequestRepository.findAllByIsDelete(0,pageable);
		
		//Page<AuthorRequest> sang Page<AuthorRegisterReponse>
		return requestPage.map(authorRequest -> {
			User user = userRepository.findByUserId(authorRequest.getAuthorId());
			UserInfo userInfo = userInfoRepository.findByUserId(authorRequest.getAuthorId());
			AuthorRegisterReponse responseRegister = AuthorRegisterReponse.builder()
					.fullName(userInfo.getFullName())
					.email(user.getEmail())
					.username(user.getUsername())
					.avatar(userInfo.getAvatar())
					.gender(userInfo.getGender())
					.dob(userInfo.getDob())
					.bankAccount(userInfo.getBankAccount())
					.requestId(authorRequest.getRequestId())
					.createdAt(authorRequest.getCreatedAt())
					.reviewAt(authorRequest.getReviewAt())
					.description(authorRequest.getDescription())
					.build();
			return responseRegister;
		});
		
	}
	
	public Page<AuthorRegisterReponse> searchAuthorRequestByName(int page, int size, String keyword) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<AuthorRequest> requestPage = authorRequestRepository.findAuthorRequestByName(keyword, pageable);
		
		//Page<AuthorRequest> sang Page<AuthorRegisterReponse>
		return requestPage.map(authorRequest -> {
			User user = userRepository.findByUserId(authorRequest.getAuthorId());
			UserInfo userInfo = userInfoRepository.findByUserId(authorRequest.getAuthorId());
			AuthorRegisterReponse responseRegister = AuthorRegisterReponse.builder()
					.fullName(userInfo.getFullName())
					.email(user.getEmail())
					.username(user.getUsername())
					.avatar(userInfo.getAvatar())
					.gender(userInfo.getGender())
					.dob(userInfo.getDob())
					.bankAccount(userInfo.getBankAccount())
					.requestId(authorRequest.getRequestId())
					.createdAt(authorRequest.getCreatedAt())
					.reviewAt(authorRequest.getReviewAt())
					.description(authorRequest.getDescription())
					.build();
			return responseRegister;
		});
		
	}
	
	public void reviewAuthorRegister(String requestId, String status) {
		AuthorRequest authorRequest = authorRequestRepository.findByRequestId(requestId);
		if(authorRequest == null) {
			throw new RuntimeException("Không tìm thấy đơn");
		}
		
		if(status.equals("ACCEPT")) {
			User user = userRepository.findByUserId(authorRequest.getAuthorId());
			user.setRole("AUTHOR");
			userRepository.save(user);
		}
		
		authorRequest.setStatus(status);
		authorRequest.setReviewAt(LocalDate.now());
		authorRequest.setIsDelete(1);
		authorRequestRepository.save(authorRequest);
		
	}
	
	
	public Page<AuthorInfoResponse> getAllAuthors(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<User> listAuthor = userRepository.findAllByRole("AUTHOR", pageable);
		
		return listAuthor.map(author -> {
			UserInfo userInfo = userInfoRepository.findByUserId(author.getUserId());
			AuthorInfoResponse responseRegister = AuthorInfoResponse.builder()
					.authorId(author.getUserId())
					.username(author.getUsername())
					.email(author.getEmail())
					.fullName(userInfo.getFullName())
					.avatar(userInfo.getAvatar())
					.gender(userInfo.getGender())
					.dob(userInfo.getDob())
					.bankAccount(userInfo.getBankAccount())
					.build();
			return responseRegister;
		});
		
	} 
	
	public Page<AuthorInfoResponse> searchAuthors(int page, int size, String keyword) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());

		Page<UserInfo> listAuthor = userInfoRepository.findAuthorsByKeyword(keyword.trim(), pageable);
		
		return listAuthor.map(author -> {
			User authorAccount = userRepository.findByUserId(author.getUserId());
			AuthorInfoResponse responseRegister = AuthorInfoResponse.builder()
					.authorId(author.getUserId())
					.username(authorAccount.getUsername())
					.email(authorAccount.getEmail())
					.fullName(author.getFullName())
					.avatar(author.getAvatar())
					.gender(author.getGender())
					.dob(author.getDob())
					.bankAccount(author.getBankAccount())
					.build();
			return responseRegister;
		});
		
	} 
}
