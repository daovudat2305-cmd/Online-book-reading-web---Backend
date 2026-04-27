package bookonline.service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import bookonline.dto.request.GroqRequest;
import bookonline.dto.response.GroqResponse;
import bookonline.entity.Book;
import bookonline.repository.BookRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class RecommendationService {
	@Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;
    
    @Autowired private BookRepository bookRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getAiRecommendedBookIds(
            List<Book> likedBooks, 
            List<Book> deepReadBooks, 
            List<Book> shallowReadBooks,
            List<Book> cmtBooks,
            List<Book> candidateBooks) {

        // lấy dữ liệu đầu vào
        String userContext = buildUserContext(likedBooks, deepReadBooks, shallowReadBooks, cmtBooks);
        String candidatesList = buildCandidatesList(candidateBooks);

        //tạo prompt
        String systemPrompt = "Bạn là một chuyên gia đề xuất sách. \n" +
                "Nhiệm vụ của bạn là chọn ra CHÍNH XÁC 10 cuốn sách phù hợp nhất từ 'Danh sách ứng viên' dựa trên 'TOP 3 Thể loại yêu thích nhất của người dùng'.\n" +
                "Cách tính thể loại yêu thích nhất:\n" +
                "- Sách cực kỳ yêu thích: 10 điểm\n" +
                "- Sách đã đọc kỹ: 7 điểm\n" +
                "- Sách có xem qua nhưng bỏ dở: 2 điểm\n" +
                "- Sách đã bình luận: 6 điểm\n" +
                "QUAN TRỌNG: Chỉ trả về MỘT MẢNG JSON chứa bookId của 10 cuốn sách đó. KHÔNG giải thích, KHÔNG thêm bất kỳ văn bản nào khác. \n" +
                "Ví dụ output: [\"id1\", \"id2\", \"id3\", ...]";

        String userPrompt = "--- SỞ THÍCH NGƯỜI DÙNG ---\n" + userContext +
                            "\n--- DANH SÁCH ỨNG VIÊN ---\n" + candidatesList;
        
        //đóng gói và gọi
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<GroqRequest.Message> messages = List.of(
                new GroqRequest.Message("system", systemPrompt),
                new GroqRequest.Message("user", userPrompt)
        );

        GroqRequest requestBody = new GroqRequest(model, messages, 0.1);
        HttpEntity<GroqRequest> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GroqResponse> response = restTemplate.postForEntity(apiUrl, request, GroqResponse.class);
            String jsonResult = response.getBody().getChoices().get(0).getMessage().getContent();
            
            logRequest(systemPrompt, userContext, candidatesList, jsonResult);
            
            // lọc bỏ markdown block nếu Groq vô tình trả về ```json ... ```
            jsonResult = jsonResult.replace("```json", "").replace("```", "").trim();

            // dùng Jackson ObjectMapper để parse chuỗi JSON thành List<String>
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonResult, new TypeReference<List<String>>(){});

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Groq API: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    //tạo chuỗi
    private String buildUserContext(List<Book> liked, List<Book> deep, List<Book> shallow, List<Book> cmt) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("- Sách cực kỳ yêu thích: \n").append(extractBookInfo(liked)).append("\n");
        sb.append("- Sách đã đọc kỹ: \n").append(extractBookInfo(deep)).append("\n");
        sb.append("- Sách có xem qua nhưng bỏ dở: \n").append(extractBookInfo(shallow)).append("\n");
        sb.append("- Sách đã bình luận: \n").append(extractBookInfo(cmt)).append("\n");
        return sb.toString();
    }

    private String buildCandidatesList(List<Book> candidates) {
        StringBuilder sb = new StringBuilder();
        for (Book b : candidates) {
            String categoryNames = getCategoryNamesAsString(b);
            sb.append(String.format("- ID: %s | Tên: %s | Thể loại: %s | Mô tả: %s\n", 
                    b.getBookId(), 
                    b.getTitle(), 
                    categoryNames, 
                    b.getDescription()));
        }
        return sb.toString();
    }

    private String extractBookInfo(List<Book> books) {
        return books.stream()
                .map(b -> String.format("+ '%s' (Thể loại: %s)", b.getTitle(), getCategoryNamesAsString(b)))
                .collect(Collectors.joining("\n"));
    }
    
    //lấy ds thể loại
    private String getCategoryNamesAsString(Book book) {
        List<String> categoryNames = bookRepository.findCategoryNamesByBookId(book.getBookId());
        if (categoryNames != null && !categoryNames.isEmpty()) {
            return String.join(", ", categoryNames);
        }
        return "Chưa rõ";
        
    }
    
    private void logRequest(String systemPrompt, String userContext, String candidatesList, String groqResponse) {
    	String csvFilePath = "groq_api_log.csv";
    	boolean isNewFile = !new File(csvFilePath).exists();
    	
    	try (FileWriter fw = new FileWriter(csvFilePath, true);
    		PrintWriter pw = new PrintWriter(fw)) {
			
    		if(isNewFile) {
    			pw.write('\ufeff');
    			pw.println("Thời gian,SystemPrompt,Thông tin User(Context),Danh sách ứng viên(Candidates), Groq Response");
    		}
    		
    		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    		
    		String cleanSys = systemPrompt.replace("\"", "\"\"");
            String cleanUser = userContext.replace("\"", "\"\"");
            String cleanCand = candidatesList.replace("\"", "\"\"");
            String cleanRes = (groqResponse != null) ? groqResponse.replace("\"", "\"\"") : "N/A";

            pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n", currentTime, cleanSys, cleanUser, cleanCand, cleanRes);
    		
		} catch (Exception e) {
			System.err.println("Lỗi khi in log" + e.getMessage());
		}
    }
}
