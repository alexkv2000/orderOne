package kvo.order.model;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class ErrorIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    @Enumerated(EnumType.STRING)
    private TargetIndicator.Structure structure;
    private String level;
    private String goal;
    private String deadline;

    // Изменяем на String для хранения нескольких значений
    private String divisions;

    private String owner;
    private String coordinator;
    private String responsibles;
    private String additionalResponsibles;
    private String business;
    private String errorMessage;  // Переименовано с errorReason на errorMessage

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public TargetIndicator.Structure getStructure() { return structure; }
    public void setStructure(TargetIndicator.Structure structure) { this.structure = structure; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    // Геттеры и сеттеры для divisions
    public String getDivisions() {
        return divisions;
    }

    public void setDivisions(String divisions) {
        this.divisions = divisions;
    }

    // Вспомогательный метод для получения списка дивизионов (как List<String>)
    public List<String> getDivisionList() {
        if (this.divisions == null || this.divisions.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(this.divisions.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // Вспомогательный метод для установки списка дивизионов (как List<String>)
    public void setDivisionList(List<String> divisions) {
        this.divisions = String.join("; ", divisions);
    }

    // Старый геттер для обратной совместимости (теперь возвращает String)
    @Transient
    public String getDivision() {
        List<String> list = getDivisionList();
        return list.isEmpty() ? "" : list.get(0);
    }

    // Старый сеттер для обратной совместимости (принимает String)
    @Transient
    public void setDivision(String division) {
        if (division == null || division.trim().isEmpty()) {
            this.divisions = "";
        } else {
            this.divisions = division;
        }
    }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getCoordinator() { return coordinator; }
    public void setCoordinator(String coordinator) { this.coordinator = coordinator; }
    public String getResponsibles() { return responsibles; }
    public void setResponsibles(String responsibles) { this.responsibles = responsibles; }
    public String getAdditionalResponsibles() { return additionalResponsibles; }
    public void setAdditionalResponsibles(String additionalResponsibles) { this.additionalResponsibles = additionalResponsibles; }
    public String getBusiness() { return business; }
    public void setBusiness(String business) { this.business = business; }
    public String getErrorMessage() { return errorMessage; }  // Переименован геттер
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }  // Переименован сеттер
}
