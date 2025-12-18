package kvo.order.repository;


import  kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErrorIndicatorRepository extends JpaRepository<ErrorIndicator, Long> {
    // Найти по номеру
    Optional<ErrorIndicator> findByNumber(String number);

    // Найти по структуре
    List<ErrorIndicator> findByStructure(TargetIndicator.Structure structure);

    List<ErrorIndicator> findByErrorMessage(String errorMessage);
    // Найти по причине ошибки
    List<ErrorIndicator> findByErrorMessageContaining(String reason);

    // Найти по дивизиону
    List<ErrorIndicator> findByDivisionContaining(String division);

    // Проверить существование по номеру
    boolean existsByNumber(String number);

    // Удалить по номеру
    void deleteByNumber(String number);
}
