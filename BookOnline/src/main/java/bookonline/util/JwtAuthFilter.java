package bookonline.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{
	
	@Autowired private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// lấy token từ header của request
		String authheader = request.getHeader(HttpHeaders.AUTHORIZATION);
		
		//kiểm tra header có chứa token hợp lệ không (bắt đầu bằng "Bearer ")
		if(authheader!=null && authheader.startsWith("Bearer ")) {
			//lấy token
			String token = authheader.substring(7);
			
			//kiểm tra token
			if(jwtUtil.validateToken(token)) {
				try {
					//lấy thông tin
					String username = jwtUtil.extractUsername(token);
					String scope = jwtUtil.extractScope(token);
					
					// chuyển đổi 1 quyền duy nhất này thành danh sách quyền cho Spring Security
                    // Collections.singletonList giúp tạo nhanh 1 List chỉ chứa 1 phần tử
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(scope));

                    // xác thực thành công, lưu thông tin vào Security Context của Spring
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
		// cho phép request đi tiếp
		filterChain.doFilter(request, response);
	}
	
	
}
