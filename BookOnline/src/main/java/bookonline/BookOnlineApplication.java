package bookonline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookOnlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookOnlineApplication.class, args);
	}

}
