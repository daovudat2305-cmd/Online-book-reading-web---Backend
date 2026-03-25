package bookonline.util;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import bookonline.BookOnlineApplication;
import bookonline.entity.User;

@Component
public class JwtUtil {

    private final BookOnlineApplication bookOnlineApplication;
	
	private static final String SINGER_KEY = "e851f08c564ed7b23969bcaaa8c262ed891f95a31662a05ecb6ed3be58644a69";

    JwtUtil(BookOnlineApplication bookOnlineApplication) {
        this.bookOnlineApplication = bookOnlineApplication;
    }
	
	// ham tao Token	
	public String generateToken(User user) {
		// thuat toan HS512
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
		
		//de build payload can claims, trong do co 1 claim la username
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
				.subject(user.getUsername())
				.issuer("bookonline")
				.issueTime(new Date())
				.expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
				.jwtID(UUID.randomUUID().toString())
				.claim("scope", "ROLE_" + user.getRole())
				.build();
		
		// tao payload tu claims
		Payload payload = new Payload(jwtClaimsSet.toJSONObject());
		
		//build JWSObject tu header va payload
		JWSObject jwsObject = new JWSObject(header,payload);
		
		try {
			jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));
			return jwsObject.serialize();
		} catch (JOSEException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Không thể tạo token: " + e.getMessage());
		}
	}
	
	//ham kiem tra tinh hop le cua token
	public boolean validateToken(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);
		
			// tao verifier de kiem tra chu ky voi khoa bi mat
			JWSVerifier verifier = new MACVerifier(SINGER_KEY.getBytes());
			
			//kiem tra chu ky
			boolean isSignatureValid = signedJWT.verify(verifier);
			
			//kiem tra thoi gian het han
			Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
			boolean isTokenExpired = expirationTime.before(new Date());
			
			return isSignatureValid && !isTokenExpired;
		} catch (ParseException | JOSEException e) {
			return false; //token sai dinh dang hoac loi
		}
		
	}
	
	// lấy username từ token
	public String extractUsername(String token) throws ParseException {
		SignedJWT signedJWT = SignedJWT.parse(token);
		return signedJWT.getJWTClaimsSet().getSubject();
	}
	
	// lấy scope(quyền) từ token
	public String extractScope(String token) throws ParseException {
		SignedJWT signedJWT = SignedJWT.parse(token);
		return signedJWT.getJWTClaimsSet().getStringClaim("scope");
	}
}
