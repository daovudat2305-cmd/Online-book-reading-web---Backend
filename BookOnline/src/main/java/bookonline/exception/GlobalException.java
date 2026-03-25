package bookonline.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolation;
import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalException {
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<?> handlingEntityRuntimeException(RuntimeException ex) {
		Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage()); 
        
        return ResponseEntity.status(400).body(errorResponse);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handlingValidation(MethodArgumentNotValidException ex) {
		var constraintViolation = ex.getBindingResult()
									.getAllErrors().getFirst().unwrap(ConstraintViolation.class);
		
		var attributes = constraintViolation.getConstraintDescriptor().getAttributes();
		
		Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getFieldError().getDefaultMessage());
		return ResponseEntity.badRequest().body(errorResponse);
		
	}
}
