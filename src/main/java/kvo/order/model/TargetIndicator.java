package kvo.order.model;

import jakarta.persistence.*;

import java.util.Comparator;

@Entity
public class TargetIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    @Enumerated(EnumType.STRING)
    private Structure structure;
    private String goal;
    private String deadline;
    @Enumerated(EnumType.STRING)
    private Division division;
    private String coordinator;
    private String responsibles;
    private String additionalResponsibles;
    private String status = "valid";

    public enum Structure {
        Цель, Подцель, Задача, error;
    }

    public enum Division {
        Группа, ДКА, ДАК, Энергобизнес, РМЭ, НЭСК, ЭСК, ДСА, ДОТ, ЯрМК, РМЭИ, error;
    }

    public TargetIndicator() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public Structure getStructure() { return structure; }
    public void setStructure(Structure structure) { this.structure = structure; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public Division getDivision() { return division; }
    public void setDivision(Division division) { this.division = division; }
    public String getCoordinator() { return coordinator; }
    public void setCoordinator(String coordinator) { this.coordinator = coordinator; }
    public String getResponsibles() { return responsibles; }
    public void setResponsibles(String responsibles) { this.responsibles = responsibles; }
    public String getAdditionalResponsibles() { return additionalResponsibles; }
    public void setAdditionalResponsibles(String additionalResponsibles) { this.additionalResponsibles = additionalResponsibles; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Comparator for version sorting
    public static Comparator<TargetIndicator> VERSION_COMPARATOR = Comparator.comparing(TargetIndicator::getNumber, (a, b) -> {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int len = Math.max(aParts.length, bParts.length);
        for (int i = 0; i < len; i++) {
//            int aPart = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
//            int bPart = i < bParts.length ? Integer.parseInt(bParts[i]) : 0;
//            if (aPart != bPart) return Integer.compare(aPart, bPart);
//        }
//        return 0;
            int aPart = 0;
            int bPart = 0;
            try {
                aPart = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
            } catch (NumberFormatException e) {
                // Если не число, считаем как 0 (или можно сравнивать как строки ниже)
                aPart = 0;
            }
            try {
                bPart = i < bParts.length ? Integer.parseInt(bParts[i]) : 0;
            } catch (NumberFormatException e) {
                bPart = 0;
            }
            if (aPart != bPart) return Integer.compare(aPart, bPart);
        }
        return 0;
    });
}
