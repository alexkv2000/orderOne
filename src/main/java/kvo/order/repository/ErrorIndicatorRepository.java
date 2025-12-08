package kvo.order.repository;


import  kvo.order.model.ErrorIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorIndicatorRepository extends JpaRepository<ErrorIndicator, Long> {
}
