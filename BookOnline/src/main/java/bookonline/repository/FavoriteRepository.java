package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.Favorite;



@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer>{
	Favorite findByFavoriteId(String favoriteId);
	Favorite findByUserIdAndBookId(String userId, String bookId);
	long countByBookId(String bookId);
}
