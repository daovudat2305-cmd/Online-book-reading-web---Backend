package bookonline.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import bookonline.util.JwtAuthFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Autowired private JwtAuthFilter jwtAuthFilter;
	
	private static final String[] PUBLIC_ENDPOINTS = {
			"/auth/signup",
			"/auth/signin",
	};

    /**
     * Cấu hình CORS toàn cục cho Spring Security
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // cho phép Frontend ở cổng 5500 gọi sang (Thêm cả localhost và 127.0.0.1)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500", 
            "http://127.0.0.1:5500"
        ));
        
        // cho phép các phương thức (GET, POST,...)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // cho phép tất cả các Header (quan trọng để Frontend gửi được header Authorization chứa Token)
        configuration.setAllowedHeaders(List.of("*"));
        
        // (Tùy chọn) Cho phép gửi thông tin xác thực như Cookie (nếu cần)
        configuration.setAllowCredentials(true);

        // Áp dụng cấu hình này cho TẤT CẢ CÁC ĐƯỜNG DẪN ("/**")
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)  throws Exception {
		
		httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		
		//vô hiệu hóa bảo vệ csrf
		httpSecurity.csrf(AbstractHttpConfigurer::disable);
		
		//cấu hình session thành stateless
		httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		//phân quyền
		httpSecurity.authorizeHttpRequests(request -> 
				request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
						.requestMatchers(HttpMethod.GET, "/books/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/comments/*").permitAll()
						.requestMatchers(HttpMethod.GET, "/favorites/*/count").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/payment/sepay-webhook").permitAll()
				.anyRequest().authenticated()
		);
		
		//chèn bộ lọc JWT 
        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		
		return httpSecurity.build();
	}
	
	// dang ki password encoder de ma hoa mat khau
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(10);
	}

    // Bean để quản lý xác thực
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}