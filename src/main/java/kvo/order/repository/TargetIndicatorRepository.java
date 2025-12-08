package kvo.order.repository;

import kvo.order.model.TargetIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetIndicatorRepository extends JpaRepository<TargetIndicator, Long> {

}
