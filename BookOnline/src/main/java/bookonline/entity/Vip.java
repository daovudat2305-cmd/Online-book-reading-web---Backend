package bookonline.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class Vip {
	
	@Id
	String vipId;
	
	//số ngày
	int durationTime;
	double price;
	
	@OneToMany(mappedBy = "vip")
	private List<Payment> payments;
	
	@OneToMany(mappedBy = "vip")
	private List<UserVip> userVips;
}
