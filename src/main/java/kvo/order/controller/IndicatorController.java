package kvo.order.controller;

import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import kvo.order.config.DivisionConfig;
import kvo.order.repository.ErrorIndicatorRepository;
import kvo.order.service.IndicatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/api/order") // Базовый путь для API
public class IndicatorController {
    private static final Logger logger = LoggerFactory.getLogger(IndicatorController.class);

    @Autowired
    private IndicatorService service;
    @Autowired
    private DivisionConfig divisionConfig;
    // JSON API endpoint для получения всех данных
    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("indicators", service.getAllIndicators());
        data.put("errors", service.getAllErrors());
        data.put("structures", TargetIndicator.Structure.values());
        // Изменено: Вместо enum Division используем список строк из DivisionConfig (предполагаем, что DivisionConfig.getDivisions() возвращает List<String>)
        // Если DivisionConfig не имеет такого метода, замените на Arrays.asList("Дивизион1", "Дивизион2", ...) или добавьте логику
        data.put("divisions", divisionConfig.getDivisions());
        return data;
    }

    // Обрабатываем два пути: корневой /order и /api/order
    @GetMapping({"", "/", "/order"})
    public String showOrderPage(Model model) {
        logger.info("Indicators size: {}", service.getAllIndicators().size());
        logger.info("Errors size: {}", service.getAllErrors().size());

        List<ErrorIndicator> indicators_err = service.getAllErrors();
        indicators_err = indicators_err.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<TargetIndicator> indicators_ind = service.getAllIndicators();
        model.addAttribute("indicators", indicators_ind);
        model.addAttribute("errors", indicators_err);
        model.addAttribute("structures", TargetIndicator.Structure.values());
        // Изменено: Вместо enum Division используем список строк из DivisionConfig
        model.addAttribute("divisions", divisionConfig.getDivisions());
        return "order";
    }

    // JSON API для загрузки файла
    @PostMapping("/upload")
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                response.put("success", false);
                response.put("message", "Only .xlsx files are allowed!");
                return response;
            }

            service.importFromXls(file);
            response.put("success", true);
            response.put("message", "File uploaded successfully!");
        } catch (Exception e) {
            logger.error("Error during file upload: ", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка при обработке файла";
            response.put("success", false);
            response.put("message", "Error uploading file: " + errorMessage);
        }
        return response;
    }

    // JSON API для переноса ошибок
    @PostMapping("/transfer")
    @ResponseBody
    public Map<String, Object> transferErrors(@RequestBody List<Long> errorIds) {
        Map<String, Object> response = new HashMap<>();

        if (errorIds == null || errorIds.isEmpty()) {
            response.put("success", false);
            response.put("message", "Ошибка: Выберите хотя бы одну строку для переноса.");
            return response;
        }

        try {
            service.transferErrors(errorIds);
            response.put("success", true);
            response.put("message", "Ошибки успешно перенесены в основной экран.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Произошла ошибка при переносе: " + e.getMessage());
        }
        return response;
    }

    // JSON API для обновления индикатора
    @PostMapping("/update/{id}")
    @ResponseBody
    public Map<String, Object> updateIndicator(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<TargetIndicator> optionalIndicator = Optional.ofNullable(service.getIndicatorById(id));

            if (optionalIndicator.isPresent()) {
                TargetIndicator indicator = optionalIndicator.get();

                // Обновляем поля из data
                if (data.containsKey("number")) {
                    indicator.setNumber(data.get("number"));
                }
                if (data.containsKey("structure")) {
                    indicator.setStructure(TargetIndicator.Structure.valueOf(data.get("structure")));
                }
                if (data.containsKey("level")) {
                    indicator.setLevel(data.get("level"));
                }
                if (data.containsKey("goal")) {
                    indicator.setGoal(data.get("goal"));
                }
                if (data.containsKey("deadline")) {
                    indicator.setDeadline(data.get("deadline"));
                }
                if (data.containsKey("deadlineend")) {
                    indicator.setDeadlineEnd(data.get("deadlineend"));
                }
                if (data.containsKey("coordinator")) {
                    indicator.setCoordinator(data.get("coordinator"));
                }
                if (data.containsKey("divisions")) {
                    indicator.setDivisions(data.get("divisions"));  // Уже String — корректно
                }
                if (data.containsKey("owner")) {
                    indicator.setOwner(data.get("owner"));
                }
                if (data.containsKey("responsibles")) {
                    indicator.setResponsibles(data.get("responsibles"));
                }
                if (data.containsKey("additionalResponsibles")) {
                    indicator.setAdditionalResponsibles(data.get("additionalResponsibles"));
                }
                if (data.containsKey("business")) {
                    indicator.setBusiness(data.get("business"));
                }

                service.saveIndicator(indicator);
                response.put("success", true);
                response.put("message", "Indicator updated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Indicator not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    // JSON API для обновления ошибки
    @PostMapping("/update-error/{id}")
    @ResponseBody
    public Map<String, Object> updateError(@PathVariable Long id, @RequestBody ErrorIndicator error) {
        Map<String, Object> response = new HashMap<>();
        try {
            error.setId(id);
            service.updateError(id, error);  // Метод в сервисе обновлён для divisions как String и errorMessage
            response.put("success", true);
            response.put("message", "Error updated successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/export/{type}")
    public ResponseEntity<byte[]> export(@PathVariable String type) throws IOException {
        byte[] data = service.exportToXls(type);  // Экспорт в сервисе обновлён: заголовок "Error Message", divisions как String
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "indicators.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @GetMapping("/errors")
    public String getErrorsPage(Model model) {
        model.addAttribute("errors", service.getAllErrors());
        return "errors";
    }

    // JSON API для очистки списка
    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearIndicators() {
        Map<String, Object> response = new HashMap<>();
        try {
            service.deleteAllIndicators();
            response.put("success", true);
            response.put("message", "Экран очищен. Indicators удалены.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при очистке: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/clearErr")
    @ResponseBody
    public Map<String, Object> clearIndicatorsErr() {
        Map<String, Object> response = new HashMap<>();
        try {
            service.deleteAllErrors();
            response.put("success", true);
            response.put("message", "Экран очищен. Indicators удалены.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при очистке: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/help")
    public String help() {
        System.out.println("'/' - мониторинг сервисов серверов; \n'/export' - экспорт данных в TXT формате; \n'/status - просмотр данных в JSON формате.\n");
        return "help";
    }
}
