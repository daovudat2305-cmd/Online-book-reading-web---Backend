package bookonline.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "category")
@Data
public class Category {
    @Id
    private Integer categoryId;
    private String categoryName;
}