package kvo.order.model;

import jakarta.persistence.*;

@Entity
public class ErrorIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    @Enumerated(EnumType.STRING)
    private TargetIndicator.Structure structure;
    private String goal;
    private String deadline;
    @Enumerated(EnumType.STRING)
    private TargetIndicator.Division division;
    private String coordinator;
    private String responsibles;
    private String additionalResponsibles;
    private String status = "error";
    private String errorReason;
    private Long originalId;

    // Getters and Setters (аналогично TargetIndicator)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public TargetIndicator.Structure getStructure() { return structure; }
    public void setStructure(TargetIndicator.Structure structure) { this.structure = structure; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public TargetIndicator.Division getDivision() { return division; }
    public void setDivision(TargetIndicator.Division division) { this.division = division; }
    public String getCoordinator() { return coordinator; }
    public void setCoordinator(String coordinator) { this.coordinator = coordinator; }
    public String getResponsibles() { return responsibles; }
    public void setResponsibles(String responsibles) { this.responsibles = responsibles; }
    public String getAdditionalResponsibles() { return additionalResponsibles; }
    public void setAdditionalResponsibles(String additionalResponsibles) { this.additionalResponsibles = additionalResponsibles; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }
    public Long getOriginalId() { return originalId; }
    public void setOriginalId(Long originalId) { this.originalId = originalId; }
}
