package bookonline.dto.response;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookRankResponse {
	String bookId;
	String title;
	String authorName;
	String coverImage;
	String type;
	List<String> categories;
	long viewCount;
	long commentCount;
	long favoCount;
}
