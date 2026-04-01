package bookonline.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "user_vip")
public class UserVip {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	String uvId;
	
	String userId;
	String vipId;
	LocalDateTime startDate;
	LocalDateTime endDate;
	
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "vipId", insertable = false, updatable = false)
	private Vip vip;
}
