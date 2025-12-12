package kvo.order.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class TargetIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    @Enumerated(EnumType.STRING)
    private Structure structure;
    private String level;
    private String goal;
    private String deadline;

    // Изменяем на String для хранения нескольких значений
    private String divisions; // Храним как "Группа,ДКА,Энергобизнес"

    private String owner;
    private String coordinator;
    private String responsibles;
    private String additionalResponsibles;
    private String business;
    private String status = "valid";

    public enum Structure {
        МЕРОПРИЯТИЕ, РАЗДЕЛ, ПОДРАЗДЕЛ, ЦЕЛЬ, ПОДЦЕЛЬ, ЗАДАЧА, ПОДЗАДАЧА, EMPTY(""), SPACE(" "), error;

        private final String displayName;

        Structure() {
            this.displayName = this.name().toUpperCase();
        }

        Structure(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Structure fromDisplayName(String displayName) {
            if (displayName == null || displayName.trim().isEmpty()) {
                return EMPTY;
            }
            String trimmed = displayName.trim();

            // Проверяем специальные случаи
            if (trimmed.isEmpty()) return EMPTY;
            if (displayName.equals(" ")) return SPACE;

            for (Structure struct : values()) {
                if (struct.displayName != null &&
                        struct.displayName.equals(displayName)) {
                    return struct;
                }
            }
            return error;
        }
    }

    public enum Division {
        Группа, ДКА, ДАК, Энергобизнес, РМЭ, НЭСК, ЭСК, ДСА, ДОТ, ЯрМК, РМЭИ, НГА, ТРМ, ПДД, Сотекс, ПГА, Оптитэк,
        @JsonProperty("ООО \"РМКПГ\"")
        ООО_РМКПГ, EMPTY(""), error;

        private final String displayName;

        Division() {
            this.displayName = this.name();
        }

        Division(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Optional<Division> fromDisplayName(String displayName) {
            if (displayName == null || displayName.trim().isEmpty()) {
                return Optional.empty();
            }

            for (Division division : values()) {
                if (division.displayName != null &&
                        division.displayName.equals(displayName.trim())) {
                    return Optional.of(division);
                }
            }
            return Optional.of(error);
        }

        // Новый метод для обработки нескольких дивизионов
        public static List<Division> fromStringList(String divisionsString) {
            if (divisionsString == null || divisionsString.trim().isEmpty()) {
                return Collections.emptyList();
            }

            List<Division> result = new ArrayList<>();
            String[] parts = divisionsString.split("[,\\s;]+");

            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    fromDisplayName(trimmed).ifPresent(result::add);
                }
            }

            return result;
        }

        // Метод для преобразования списка в строку
        public static String toString(List<Division> divisions) {
            if (divisions == null || divisions.isEmpty()) {
                return "";
            }

            return divisions.stream()
                    .map(Division::getDisplayName)
                    .collect(Collectors.joining(", "));
        }
    }

    public TargetIndicator() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public Structure getStructure() { return structure; }
    public void setStructure(Structure structure) { this.structure = structure; }
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

    // Вспомогательный метод для получения списка дивизионов
    public List<Division> getDivisionList() {
        return Division.fromStringList(this.divisions);
    }

    // Вспомогательный метод для установки списка дивизионов
    public void setDivisionList(List<Division> divisions) {
        this.divisions = Division.toString(divisions);
    }

    // Старый геттер для обратной совместимости
    @Transient
    public Division getDivision() {
        List<Division> list = getDivisionList();
        return list.isEmpty() ? Division.EMPTY : list.get(0);
    }

    // Старый сеттер для обратной совместимости
    @Transient
    public void setDivision(Division division) {
        if (division == null || division == Division.EMPTY) {
            this.divisions = "";
        } else {
            this.divisions = division.getDisplayName();
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Comparator for version sorting
    public static Comparator<TargetIndicator> VERSION_COMPARATOR = Comparator.comparing(TargetIndicator::getNumber, (a, b) -> {
        String[] aParts = a.split("\\.");
        String[] bParts = b.split("\\.");
        int len = Math.max(aParts.length, bParts.length);
        for (int i = 0; i < len; i++) {
            int aPart = 0;
            int bPart = 0;
            try {
                aPart = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
            } catch (NumberFormatException e) {
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