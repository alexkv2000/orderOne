package kvo.order.service;

import jakarta.transaction.Transactional;
import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import kvo.order.repository.ErrorIndicatorRepository;
import kvo.order.repository.TargetIndicatorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IndicatorService {

    private static final Logger log = LoggerFactory.getLogger(IndicatorService.class);
    @Autowired
    private TargetIndicatorRepository targetRepo;

    @Autowired
    private ErrorIndicatorRepository errorRepo;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public void importFromXls(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            StringBuilder err_message = new StringBuilder();
            for (Row row : sheet) {
                int leng_sb = err_message.length();
                err_message.delete(0, leng_sb);
                boolean err = false;

                if (row.getRowNum() == 0) continue; // Skip header
                TargetIndicator indicator = new TargetIndicator();

                String numberValue = getCellValue(row.getCell(0));
                if (numberValue == null || numberValue.trim().isEmpty()) {
                    err_message.append("Пустой номер в строке ").append(row.getRowNum() + 1).append("; ");
                    err = true;
                } else if (!numberValue.trim().isEmpty() && !numberValue.endsWith(".")) {
                    numberValue = numberValue + ".";
                }

                if (numberValue != null && !numberValue.trim().isEmpty()) {
                    String[] numberParts = numberValue.split("\\.");
                    for (String part : numberParts) {
                        String trimmedPart = part.trim();
                        if (!trimmedPart.isEmpty() && !trimmedPart.matches("\\d+")) {
                            err = true;
                            err_message.append("!ожидается_число");
                            break;
                        }
                    }
                }
                indicator.setNumber(numberValue);

                if (numberValue != null && !numberValue.trim().isEmpty()) {
                    String[] parts = numberValue.split("\\.");
                    String numberBeforeDot = parts[0].trim();
                    if (numberBeforeDot.matches("\\d+") && (parts.length == 1)) {
                        String cell1Value = getCellValue(row.getCell(1));
                        if (cell1Value == null || !"Раздел".equals(cell1Value.trim())) {
                            err = true;
                            err_message.append("|!ожидается_Раздел_а_не_").append(cell1Value);
                        }
                    }
                }

                String stringStructure = getCellValue(row.getCell(1));
                if (stringStructure == null || stringStructure.trim().isEmpty()) {
                    err_message.append("Структура пустая (строка - ").append(row.getRowNum() + 1).append("); ");
                    indicator.setStructure(TargetIndicator.Structure.error);
                    err = true;
                } else if (stringStructure.equals("Мероприятие") ||
                        stringStructure.equals("Раздел") ||
                        stringStructure.equals("Подраздел") ||
                        stringStructure.equals("Цель") ||
                        stringStructure.equals("Подцель") ||
                        stringStructure.equals("Задача") ||
                        stringStructure.equals("Подзадача")) {
                    try {
                        indicator.setStructure(TargetIndicator.Structure.valueOf(stringStructure));
                    } catch (Exception e) {
                        log.error("Ошибка_структуры: {}", e.toString());
                        err = true;
                        indicator.setStructure(TargetIndicator.Structure.error);
                        err_message.append("Ошибка_структары");
                    }
                } else {
                    err = true;
                    indicator.setStructure(TargetIndicator.Structure.error);
                    err_message.append("|!структ");
                }

                // Уровень
                try {
                    String dateValue = getFormattedDateCellValue(row.getCell(2));
                    indicator.setLevel(dateValue);
                } catch (IllegalArgumentException e) {
                    err = true;
                    err_message.append("|!уровень");
                    indicator.setLevel("Нет уровня");
                }

                // Цель
                try {
                    String dateValue = getFormattedDateCellValue(row.getCell(3));
                    if (dateValue != null && dateValue.length() > 255) {
                        err = true;
                        dateValue = dateValue.substring(0, 254);
                        err_message.append("|!длинна Цели");
                    }
                    indicator.setGoal(dateValue);
                } catch (IllegalArgumentException e) {
                    err = true;
                    err_message.append("|!цель");
                    indicator.setGoal("Нет цели");
                }

                // Сроки
                String dLine = getCellValue(row.getCell(4));
                if ("Раздел".equals(stringStructure)) {
                    indicator.setOwner(dLine);
                } else {
                    if (dLine == null || dLine.trim().isEmpty()) {
                        err = true;
                        err_message.append("|!дата");
                        indicator.setDeadline("Нет даты");
                    } else {
                        indicator.setDeadline(dLine);
                    }
                }

                // Дивизионы (теперь несколько)
                try {
                    String div = getCellValue(row.getCell(5));
                    if (div == null && !Objects.equals(stringStructure, "Раздел")) {
                        err_message.append("Пустое значение дивизиона ").append(row.getRowNum() + 1).append("; ");
                        indicator.setDivisions("");
                        err = true;
                    } else if (div == null && Objects.equals(stringStructure, "Раздел")) {
                        indicator.setDivisions("");
                    } else {
                        // Проверяем, есть ли в строке несколько дивизионов
                        List<TargetIndicator.Division> divisions = TargetIndicator.Division.fromStringList(div);
                        if (divisions.isEmpty()) {
                            err = true;
                            err_message.append("|!див");
                            indicator.setDivisions("");
                        } else {
                            indicator.setDivisions(TargetIndicator.Division.toString(divisions));
                        }
                    }
                } catch (Exception e) {
                    err = true;
                    indicator.setDivisions("");
                    err_message.append("|!див");
                }

                // Владелец
                String owner = getCellValue(row.getCell(6));
                if ("Раздел".equals(stringStructure)) {
                    indicator.setOwner(owner);
                } else {
                    if (owner == null || owner.trim().isEmpty()) {
                        err = true;
                        err_message.append("|!влад");
                        indicator.setOwner("Нет владельца");
                    } else {
                        indicator.setOwner(owner);
                    }
                }

                // Координатор
                String coord = getCellValue(row.getCell(7));
                if ("Раздел".equals(stringStructure)) {
                    indicator.setCoordinator(coord);
                } else {
                    if (coord == null || coord.trim().isEmpty()) {
                        err = true;
                        err_message.append("|!коорд");
                        indicator.setCoordinator("Нет координатора");
                    } else if (!validateEmails(coord)) {
                        err = true;
                        err_message.append("|!коорд");
                        indicator.setCoordinator("Нет координатора");
                    } else {
                        indicator.setCoordinator(coord);
                    }
                }

                // Ответственные
                String resp = getCellValue(row.getCell(8));
                if ("Раздел".equals(stringStructure)) {
                    indicator.setResponsibles(resp);
                } else {
                    if (resp == null || resp.trim().isEmpty()) {
                        err = true;
                        err_message.append("|!отв");
                    } else if (!validateMultipleEmails(resp)) {
                        err = true;
                        err_message.append("|!отв");
                    } else {
                        indicator.setResponsibles(resp);
                    }
                }

                // Дополнительные ответственные
                String addResp = getCellValue(row.getCell(9));
                if ("Раздел".equals(stringStructure)) {
                    indicator.setBusiness(addResp);
                } else {
                    if (!validateMultipleEmails(addResp)) {
                        err = true;
                        err_message.append("|!доп_отв");
                    } else {
                        indicator.setAdditionalResponsibles(addResp);
                    }
                }

                // Бизнес
                String business = getCellValue(row.getCell(10));
                if (business != null) {
                    indicator.setBusiness(business);
                }

                if (err) {
                    saveError(indicator, err_message.toString());
                } else {
                    targetRepo.save(indicator);
                }
            }
        }
    }

    private boolean validateMultipleEmails(String emails) {
        if (emails == null || emails.isEmpty()) return false;
        String[] emailArray = emails.split("[,\\s;]+");
        for (String email : emailArray) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.isEmpty() && !isValidEmail(trimmedEmail)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void saveError(TargetIndicator indicator, String reason) {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber(indicator.getNumber());
        error.setStructure(indicator.getStructure());
        error.setLevel(indicator.getLevel());
        error.setGoal(indicator.getGoal());
        error.setDeadline(indicator.getDeadline());
        error.setDivisions(indicator.getDivisions());
        error.setCoordinator(indicator.getCoordinator());
        error.setOwner(indicator.getOwner());
        error.setResponsibles(indicator.getResponsibles());
        error.setAdditionalResponsibles(indicator.getAdditionalResponsibles());
        error.setBusiness(indicator.getBusiness());
        error.setErrorReason(reason);
        errorRepo.save(error);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((int) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        return dateFormat.format(cell.getDateCellValue());
                    } else {
                        return cell.getStringCellValue();
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return null;
        }
    }

    private String getFormattedDateCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            return dateFormat.format(cell.getDateCellValue());
        }
        return getCellValue(cell);
    }

    private boolean validateEmails(String emails) {
        if (emails == null || emails.isEmpty()) return false;
        String[] emailArray = emails.split(",");
        for (String email : emailArray) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) return false;
        }
        return true;
    }

    public List<TargetIndicator> getAllIndicators() {
        return targetRepo.findAll().stream()
                .sorted(TargetIndicator.VERSION_COMPARATOR)
                .collect(Collectors.toList());
    }

    public List<ErrorIndicator> getAllErrors() {
        return errorRepo.findAll();
    }

    public void deleteAllIndicators() {
        targetRepo.deleteAll();
    }
    public void deleteAllErrors() {
        errorRepo.deleteAll();
    }

    @Transactional
    public void transferErrors(List<Long> errorIds) {
        if (errorIds == null || errorIds.isEmpty()) {
            throw new IllegalArgumentException("Список ID ошибок пустой или null");
        }

        for (Long id : errorIds) {
            ErrorIndicator error = errorRepo.findById(id).orElseThrow(() ->
                    new RuntimeException("Ошибка с ID " + id + " не найдена"));

            TargetIndicator indicator = new TargetIndicator();
            indicator.setNumber(error.getNumber());
            indicator.setStructure(error.getStructure());
            indicator.setGoal(error.getLevel());
            indicator.setGoal(error.getGoal());
            indicator.setDeadline(error.getDeadline());
            indicator.setDivisions(error.getDivisions());
            indicator.setCoordinator(error.getOwner());
            indicator.setCoordinator(error.getCoordinator());
            indicator.setResponsibles(error.getResponsibles());
            indicator.setAdditionalResponsibles(error.getAdditionalResponsibles());
            indicator.setBusiness(error.getBusiness());

            targetRepo.save(indicator);
            errorRepo.delete(error);
        }
    }

    public TargetIndicator updateIndicator(Long id, TargetIndicator indicatorData) {
        Optional<TargetIndicator> optionalIndicator = targetRepo.findById(id);

        if (optionalIndicator.isPresent()) {
            TargetIndicator indicator = optionalIndicator.get();

            if (indicatorData.getNumber() != null) {
                indicator.setNumber(indicatorData.getNumber());
            }
            if (indicatorData.getStructure() != null) {
                indicator.setStructure(indicatorData.getStructure());
            }
            if (indicatorData.getLevel() != null) {
                indicator.setLevel(indicatorData.getLevel());
            }
            if (indicatorData.getGoal() != null) {
                indicator.setGoal(indicatorData.getGoal());
            }
            if (indicatorData.getDeadline() != null) {
                indicator.setDeadline(indicatorData.getDeadline());
            }
            if (indicatorData.getDivisions() != null) {
                indicator.setDivisions(indicatorData.getDivisions());
            }
            if (indicatorData.getOwner() != null) {
                indicator.setOwner(indicatorData.getOwner());
            }
            if (indicatorData.getCoordinator() != null) {
                indicator.setCoordinator(indicatorData.getCoordinator());
            }
            if (indicatorData.getResponsibles() != null) {
                indicator.setResponsibles(indicatorData.getResponsibles());
            }
            if (indicatorData.getAdditionalResponsibles() != null) {
                indicator.setAdditionalResponsibles(indicatorData.getAdditionalResponsibles());
            }
            if (indicatorData.getBusiness() != null) {
                indicator.setBusiness(indicatorData.getBusiness());
            }

            return targetRepo.save(indicator);
        } else {
            throw new RuntimeException("Indicator with id " + id + " not found");
        }
    }

    public ErrorIndicator updateError(Long id, ErrorIndicator errorData) {
        Optional<ErrorIndicator> optionalError = errorRepo.findById(id);

        if (optionalError.isPresent()) {
            ErrorIndicator error = optionalError.get();

            if (errorData.getNumber() != null) {
                error.setNumber(errorData.getNumber());
            }
            if (errorData.getStructure() != null) {
                error.setStructure(errorData.getStructure());
            }
            if (errorData.getLevel() != null) {
                error.setLevel(errorData.getLevel());
            }
            if (errorData.getGoal() != null) {
                error.setGoal(errorData.getGoal());
            }
            if (errorData.getDeadline() != null) {
                error.setDeadline(errorData.getDeadline());
            }
            if (errorData.getDivisions() != null) {
                error.setDivisions(errorData.getDivisions());
            }
            if (errorData.getOwner() != null) {
                error.setOwner(errorData.getOwner());
            }
            if (errorData.getCoordinator() != null) {
                error.setCoordinator(errorData.getCoordinator());
            }
            if (errorData.getResponsibles() != null) {
                error.setResponsibles(errorData.getResponsibles());
            }
            if (errorData.getAdditionalResponsibles() != null) {
                error.setAdditionalResponsibles(errorData.getAdditionalResponsibles());
            }
            if (errorData.getBusiness() != null) {
                error.setBusiness(errorData.getBusiness());
            }
            if (errorData.getErrorReason() != null) {
                error.setErrorReason(errorData.getErrorReason());
            }

            return errorRepo.save(error);
        } else {
            throw new RuntimeException("Error with id " + id + " not found");
        }
    }

    public byte[] exportToXls(String type) throws IOException {
        List<?> indicators = type.equals("main") ? getAllIndicators() : getAllErrors();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Indicators");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Number");
        header.createCell(1).setCellValue("Structure");
        header.createCell(2).setCellValue("Level");
        header.createCell(3).setCellValue("Goal");
        header.createCell(4).setCellValue("Deadline");
        header.createCell(5).setCellValue("Divisions");
        header.createCell(6).setCellValue("Owner");
        header.createCell(7).setCellValue("Coordinator");
        header.createCell(8).setCellValue("Responsibles");
        header.createCell(9).setCellValue("Additional Responsibles");
        header.createCell(10).setCellValue("Business");
        if (type.equals("errors")) header.createCell(11).setCellValue("Error Reason");

        int rowNum = 1;
        for (Object obj : indicators) {
            Row row = sheet.createRow(rowNum++);
            if (obj instanceof TargetIndicator) {
                TargetIndicator ind = (TargetIndicator) obj;
                row.createCell(0).setCellValue(ind.getNumber());
                row.createCell(1).setCellValue(ind.getStructure().toString());
                row.createCell(2).setCellValue(ind.getLevel());
                row.createCell(3).setCellValue(ind.getGoal());
                row.createCell(4).setCellValue(ind.getDeadline());
                row.createCell(5).setCellValue(ind.getDivisions()); // Теперь выводим строку с дивизионами
                row.createCell(6).setCellValue(ind.getOwner());
                row.createCell(7).setCellValue(ind.getCoordinator());
                row.createCell(8).setCellValue(ind.getResponsibles());
                row.createCell(9).setCellValue(ind.getAdditionalResponsibles());
                row.createCell(10).setCellValue(ind.getBusiness());
            } else {
                ErrorIndicator err = (ErrorIndicator) obj;
                row.createCell(0).setCellValue(err.getNumber());
                row.createCell(1).setCellValue(err.getStructure().toString());
                row.createCell(2).setCellValue(err.getLevel());
                row.createCell(3).setCellValue(err.getGoal());
                row.createCell(4).setCellValue(err.getDeadline());
                row.createCell(5).setCellValue(err.getDivisions()); // Теперь выводим строку с дивизионами
                row.createCell(6).setCellValue(err.getOwner());
                row.createCell(7).setCellValue(err.getCoordinator());
                row.createCell(8).setCellValue(err.getResponsibles());
                row.createCell(9).setCellValue(err.getAdditionalResponsibles());
                row.createCell(10).setCellValue(err.getBusiness());
                row.createCell(11).setCellValue(err.getErrorReason());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    public TargetIndicator getIndicatorById(Long id) {
        return targetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Indicator not found with id: " + id));
    }

    // Пример использования save()
    public TargetIndicator saveIndicator(TargetIndicator indicator) {
        return targetRepo.save(indicator);
    }
}