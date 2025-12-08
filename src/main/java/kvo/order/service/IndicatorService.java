package kvo.order.service;

import jakarta.transaction.Transactional;
import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import kvo.order.repository.ErrorIndicatorRepository;
import kvo.order.repository.TargetIndicatorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IndicatorService {

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
                // Проверяем, заканчивается ли на точку
                if (numberValue == null || numberValue.trim().isEmpty()) {
                    err_message.append("Пустое значение номера в строке ").append(row.getRowNum() + 1).append("; ");
                    err = true;
                } else if (numberValue != null && !numberValue.trim().isEmpty() && !numberValue.endsWith(".")) {
                    numberValue = numberValue + "."; // Добавляем точку
                }

                if (numberValue != null && !numberValue.trim().isEmpty()) {
// Разделяем по точкам
                    String[] numberParts = numberValue.split("\\.");

                    for (String part : numberParts) {
                        String trimmedPart = part.trim();
                        // Проверяем, что каждая часть является числом (содержит только цифры)
                        if (!trimmedPart.isEmpty() && !trimmedPart.matches("\\d+")) {
                            err = true;
                            err_message.append("Ошибка_ожидается число");
                            break; // Прерываем цикл при первой ошибке
                        }
                    }
                }
                indicator.setNumber(numberValue);

                // ПРОВЕРКА: если есть число до первой точки, то ячейка 1 должна быть "Цель"
                if (numberValue != null && !numberValue.trim().isEmpty()) {
                    // Извлекаем число до первой точки
                    String[] parts = numberValue.split("\\.");
                    String numberBeforeDot = parts[0].trim();

                    // Проверяем, что это число (содержит только цифры)
                    if (numberBeforeDot.matches("\\d+") && (parts.length==1)) {
                        String cell1Value = getCellValue(row.getCell(1));

                        // Проверяем, что ячейка 1 содержит "Цель"
                        if (cell1Value == null || !"Цель".equals(cell1Value.trim())) {
                            err = true;
                            err_message.append("|Ошибка_ожидается_Цель_вместо_").append(cell1Value);
                        }
                    }
                }
                String stringStructure = getCellValue(row.getCell(1));
                if (stringStructure == null || stringStructure.trim().isEmpty()) {
                    err_message.append("Пустое значение номера в строке ").append(row.getRowNum() + 1).append("; ");
                    indicator.setStructure(TargetIndicator.Structure.error);
                    err = true;
                } else if (stringStructure.equals("Цель") ||
                        stringStructure.equals("Подцель") ||
                        stringStructure.equals("Задача")) {
                    try {
                        indicator.setStructure(TargetIndicator.Structure.valueOf(stringStructure));
                    } catch (Exception e) {
                        System.out.println("!!!! Ошибка_структары - " + e.toString());
                        err = true;
                        indicator.setStructure(TargetIndicator.Structure.error);
                        err_message.append("Ошибка_структары");
                    }
                } else {
                    err = true;
                    indicator.setStructure(TargetIndicator.Structure.error);
                    err_message.append("|Ошибка_структары");
                }

                try {
//                    indicator.setGoal(getCellValue(row.getCell(2)));
                    String dateValue = getFormattedDateCellValue(row.getCell(2));
                    indicator.setGoal(dateValue);
                } catch (IllegalArgumentException e) {
                    err = true;
                    err_message.append("|Нет цели");
                    indicator.setGoal("Нет цели");
                }
                try {
                    indicator.setDeadline(getCellValue(row.getCell(3)));
                } catch (IllegalArgumentException e) {
                    err = true;
                    err_message.append("|Ошибка_даты");
                    indicator.setDeadline("Нет даты");
                }
                try {
                    indicator.setDivision(TargetIndicator.Division.valueOf(getCellValue(row.getCell(4))));
                } catch (IllegalArgumentException e) {
                    err = true;
                    indicator.setDivision(TargetIndicator.Division.error);
                    err_message.append("|Ошибка_диизиона");
                }

                String coord = getCellValue(row.getCell(5));
                // Для координатора пустое значение допустимо, проверяем только если не пусто
                if (coord == null || coord.trim().isEmpty()) {
                    err = true;
                    err_message.append("|Нет координатора");
                } else if (!validateEmails(coord)) {
                    err = true;
                    err_message.append("|Ошибка_email_координатор");
                }
                indicator.setCoordinator(coord);

                String resp = getCellValue(row.getCell(6));
                // Ответственные - обязательное поле
                if (resp == null || resp.trim().isEmpty()) {
                    err = true;
                    err_message.append("|Нет ответственного");
                } else if (!validateMultipleEmails(resp)) {
                    err = true;
                    err_message.append("|Ошибка_email_ответственный");
                }
                indicator.setResponsibles(resp);

                String addResp = getCellValue(row.getCell(7));
                // Дополнительные ответственные - обязательное поле
                if (!validateMultipleEmails(addResp)) {
                    err_message.append("|Нет_email_доп_ответственный");
                }
                indicator.setAdditionalResponsibles(addResp);

                if (err) {
                    saveError(indicator, err_message.toString());
                } else {
                    targetRepo.save(indicator);
                }
            }
            workbook.close();
        }
    }

    // Метод для проверки нескольких email в одной строке
    private boolean validateMultipleEmails(String emails) {
        if (emails == null || emails.isEmpty()) return false;

        // Разделяем строку по различным разделителям
        String[] emailArray = emails.split("[,\\s;]+");

        for (String email : emailArray) {
            String trimmedEmail = email.trim();
            if (!trimmedEmail.isEmpty() && !isValidEmail(trimmedEmail)) {
                return false;
            }
        }
        return true;
    }

    // Метод для проверки одного email
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void saveError(TargetIndicator indicator, String reason) {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber(indicator.getNumber());
        error.setStructure(indicator.getStructure());
        error.setGoal(indicator.getGoal());
        error.setDeadline(indicator.getDeadline());
        error.setDivision(indicator.getDivision());
        error.setCoordinator(indicator.getCoordinator());
        error.setResponsibles(indicator.getResponsibles());
        error.setAdditionalResponsibles(indicator.getAdditionalResponsibles());
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
                    // Правильное форматирование даты в dd-MM-yyyy
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    // Для числовых значений убираем десятичную часть если она .0
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
                // Для формул тоже нужно обработать даты
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
        return getCellValue(cell); // Для не-дат используем обычный метод
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
        return targetRepo.findAll().stream().sorted(TargetIndicator.VERSION_COMPARATOR).collect(Collectors.toList());
    }

    public List<ErrorIndicator> getAllErrors() {
        return errorRepo.findAll();
    }

    public void deleteAllIndicators() {
        targetRepo.deleteAll();
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
            indicator.setGoal(error.getGoal());
            indicator.setDeadline(error.getDeadline());
            indicator.setDivision(error.getDivision());
            indicator.setCoordinator(error.getCoordinator());
            indicator.setResponsibles(error.getResponsibles());
            indicator.setAdditionalResponsibles(error.getAdditionalResponsibles());

            targetRepo.save(indicator);
            errorRepo.delete(error);
        }
    }

    public TargetIndicator updateIndicator(Long id, TargetIndicator indicatorData) {
        Optional<TargetIndicator> optionalIndicator = targetRepo.findById(id);

        if (optionalIndicator.isPresent()) {
            TargetIndicator indicator = optionalIndicator.get();

            // Обновляем только необходимые поля
            if (indicatorData.getNumber() != null) {
                indicator.setNumber(indicatorData.getNumber());
            }
            if (indicatorData.getStructure() != null) {
                indicator.setStructure(indicatorData.getStructure());
            }
            if (indicatorData.getGoal() != null) {
                indicator.setGoal(indicatorData.getGoal());
            }
            if (indicatorData.getDeadline() != null) {
                indicator.setDeadline(indicatorData.getDeadline());
            }
            if (indicatorData.getDivision() != null) {
                indicator.setDivision(indicatorData.getDivision());
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

            return targetRepo.save(indicator);
        } else {
            throw new RuntimeException("Indicator with id " + id + " not found");
        }
    }

    public ErrorIndicator updateError(Long id, ErrorIndicator errorData) {
        Optional<ErrorIndicator> optionalError = errorRepo.findById(id);

        if (optionalError.isPresent()) {
            ErrorIndicator error = optionalError.get();

            // Обновляем только необходимые поля
            if (errorData.getNumber() != null) {
                error.setNumber(errorData.getNumber());
            }
            if (errorData.getStructure() != null) {
                error.setStructure(errorData.getStructure());
            }
            if (errorData.getGoal() != null) {
                error.setGoal(errorData.getGoal());
            }
            if (errorData.getDeadline() != null) {
                error.setDeadline(errorData.getDeadline());
            }
            if (errorData.getDivision() != null) {
                error.setDivision(errorData.getDivision());
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
        header.createCell(2).setCellValue("Goal");
        header.createCell(3).setCellValue("Deadline");
        header.createCell(4).setCellValue("Division");
        header.createCell(5).setCellValue("Coordinator");
        header.createCell(6).setCellValue("Responsibles");
        header.createCell(7).setCellValue("Additional Responsibles");
        if (type.equals("errors")) header.createCell(8).setCellValue("Error Reason");

        int rowNum = 1;
        for (Object obj : indicators) {
            Row row = sheet.createRow(rowNum++);
            if (obj instanceof TargetIndicator) {
                TargetIndicator ind = (TargetIndicator) obj;
                row.createCell(0).setCellValue(ind.getNumber());
                row.createCell(1).setCellValue(ind.getStructure().toString());
                row.createCell(2).setCellValue(ind.getGoal());
                row.createCell(3).setCellValue(ind.getDeadline());
                row.createCell(4).setCellValue(ind.getDivision().toString());
                row.createCell(5).setCellValue(ind.getCoordinator());
                row.createCell(6).setCellValue(ind.getResponsibles());
                row.createCell(7).setCellValue(ind.getAdditionalResponsibles());
            } else {
                ErrorIndicator err = (ErrorIndicator) obj;
                row.createCell(0).setCellValue(err.getNumber());
                row.createCell(1).setCellValue(err.getStructure().toString());
                row.createCell(2).setCellValue(err.getGoal());
                row.createCell(3).setCellValue(err.getDeadline());
                row.createCell(4).setCellValue(err.getDivision().toString());
                row.createCell(5).setCellValue(err.getCoordinator());
                row.createCell(6).setCellValue(err.getResponsibles());
                row.createCell(7).setCellValue(err.getAdditionalResponsibles());
                row.createCell(8).setCellValue(err.getErrorReason());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
