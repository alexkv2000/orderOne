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
                indicator.setNumber(getCellValue(row.getCell(0)));

                if (getCellValue(row.getCell(1)).equals("Цель") ||
                        getCellValue(row.getCell(1)).equals("Подцель") ||
                        getCellValue(row.getCell(1)).equals("Задача")) {
                    try {
                        indicator.setStructure(TargetIndicator.Structure.valueOf(getCellValue(row.getCell(1))));
                    } catch (Exception e) {
                        System.out.println("!!!! invalid_structure - " + e.toString());
                        err = true;
                        indicator.setStructure(TargetIndicator.Structure.error);
                        err_message.append("invalid_structure");
                    }
                } else {
                    err = true;
                    indicator.setStructure(TargetIndicator.Structure.error);
                    err_message.append("|invalid_structure");
                }

                try {
                    indicator.setGoal(getCellValue(row.getCell(2)));
                } catch (IllegalArgumentException e) {
                    err = true;
                    err_message.append("|invalid_goal");
                    indicator.setGoal("Нет цели");
                }
                try {
                    indicator.setDeadline(getCellValue(row.getCell(3)));
                } catch (IllegalArgumentException e) {
                    err = true;
                    err_message.append("|invalid_deadline");
                    indicator.setDeadline("Нет даты");
                }
                try {
                    indicator.setDivision(TargetIndicator.Division.valueOf(getCellValue(row.getCell(4))));
                } catch (IllegalArgumentException e) {
                    err = true;
                    indicator.setDivision(TargetIndicator.Division.error);
                    err_message.append("|invalid_division");
                }

                String coord = getCellValue(row.getCell(5));
                if (!validateEmails(coord)) {
                    err = true;
                    err_message.append("|invalid_email_coordinate");
                }
                indicator.setCoordinator(coord);


                String resp = getCellValue(row.getCell(6));
                if (!validateEmails(resp)) {
                    err = true;
                    err_message.append("|invalid_email_responsible");
                }
                indicator.setResponsibles(resp);

                String addResp = getCellValue(row.getCell(7));
                if (!validateEmails(addResp)) {
                    err_message.append("|invalid_email_additionalResponsible");
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
        if (cell == null) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf((int) cell.getNumericCellValue());
    }

    private boolean validateEmails(String emails) {
        if (emails == null || emails.isEmpty()) return true;
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
