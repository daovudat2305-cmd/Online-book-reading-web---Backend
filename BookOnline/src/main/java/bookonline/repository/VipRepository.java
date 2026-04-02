package bookonline.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bookonline.entity.Vip;
import java.util.List;


@Repository
public interface VipRepository extends JpaRepository<Vip, String> {
	Vip findByVipId(String vipId);
}
