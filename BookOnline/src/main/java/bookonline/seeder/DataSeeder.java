package bookonline.seeder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.javafaker.Faker;

import bookonline.entity.Book;
import bookonline.entity.User;
import bookonline.repository.BookRepository;
import bookonline.repository.CategoryRepository;
import bookonline.repository.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner{
	
	@Autowired private UserRepository userRepository;
	@Autowired private BookRepository bookRepository;
	@Autowired private CategoryRepository categoryRepository;
	
	@Transactional
	@Override
	public void run(String... args) throws Exception {
		System.out.println("kiểm tra dữ liệu");
        seedBooks();
        System.out.println("hoàn tất sinh");
	}
	
	private void seedBooks() {
		if(bookRepository.count() > 200) {
			return;
		}
		// lấy admin làm tác giả
        List<User> users = userRepository.findAll();
        User adminUser = users.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .findFirst()
                .orElse(null);
        if(adminUser == null) {
        	System.out.println("Không có admin");
        	return;
        }
        String authorId = adminUser.getUserId(); 
        String authorName = adminUser.getUsername();
        
        List<Book> fakeBooks = new ArrayList<>();
        Random random = new Random();
        
        Faker faker = new Faker();
        
        // tạo 100 cuốn sách
        for (int i = 1; i <= 200; i++) {
            String title = faker.book().title();
            String desc = faker.lorem().paragraph(4);
            String type = random.nextDouble() > 0.7 ? "VIP" : "FREE"; 

            Book book = Book.builder()
                    .bookId(UUID.randomUUID().toString()) 
                    .title(title)
                    .description(desc)
                    .authorId(authorId) // dùng adminId
                    .authorName(authorName) // tên tác giả để là admin
                    .type(type)
                    .coverImage("https://picsum.photos/seed/book_" + i + "/400/600")
                    .fileUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                    .status(1) 
                    .totalPages(random.nextInt(300) + 100)
                    .viewCount(random.nextInt(5000))
                    .createdAt(LocalDate.now().minusDays(random.nextInt(365)))
                    .build();

            fakeBooks.add(book);
        }

        // lưu vào db
        bookRepository.saveAll(fakeBooks);
        System.out.println("đã lưu");

        // gán thể loại
        System.out.println("tạo thể loại ngẫu nhiên cho sách");
        int totalCategories = (int)categoryRepository.count();
        for (Book book : fakeBooks) {
            int numCategories = random.nextInt(3) + 1; 
            Set<Integer> assignedCategories = new HashSet<>();
            
            while (assignedCategories.size() < numCategories) {
                int randomCategoryId = random.nextInt(totalCategories) + 1;
                assignedCategories.add(randomCategoryId);
            }

            for (Integer catId : assignedCategories) {
                bookRepository.insertBookCategory(book.getBookId(), catId);
            }
        }

	}
}
