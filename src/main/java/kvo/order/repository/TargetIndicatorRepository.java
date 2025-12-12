package kvo.order.repository;

import kvo.order.model.TargetIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetIndicatorRepository extends JpaRepository<TargetIndicator, Long> {
    Optional<TargetIndicator> findByNumber(String number);

    // Найти по структуре
    List<TargetIndicator> findByStructure(TargetIndicator.Structure structure);

    // Найти по дивизиону (используя строковое поле divisions)
    List<TargetIndicator> findByDivisionContaining(String division);

    // Найти по владельцу
    List<TargetIndicator> findByOwner(String owner);

    // Найти по координатору
    List<TargetIndicator> findByCoordinator(String coordinator);

    // Найти по статусу
    List<TargetIndicator> findByStatus(String status);

    // Найти по нескольким критериям
    List<TargetIndicator> findByStructureAndDivisionContaining(TargetIndicator.Structure structure, String division);

    // Проверить существование по номеру
    boolean existsByNumber(String number);

    // Удалить по номеру
    void deleteByNumber(String number);

    // Подсчитать по структуре
    long countByStructure(TargetIndicator.Structure structure);
}
