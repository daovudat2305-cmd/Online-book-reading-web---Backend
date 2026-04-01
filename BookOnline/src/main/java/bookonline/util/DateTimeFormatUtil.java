package bookonline.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeFormatUtil {
	public static String format(LocalDateTime localDateTime) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

		String result = localDateTime.format(formatter);
		
		return result;
	}
}
