package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>{

}
