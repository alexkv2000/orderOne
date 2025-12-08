package kvo.order.controller;

import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@RequestMapping("/api/order")
public class IndicatorController {
    private static final Logger logger = LoggerFactory.getLogger(IndicatorController.class);
    @Autowired
    private IndicatorService service;

    @GetMapping
    public String showOrderPage(Model model) {
        logger.info("Indicators size: {}", service.getAllIndicators().size());
        logger.info("Errors size: {}", service.getAllErrors().size());
        List<ErrorIndicator> indicators_err = service.getAllErrors();  // Или ваш метод загрузки
        indicators_err = indicators_err.stream()
                .filter(Objects::nonNull)  // Фильтруем null
                .collect(Collectors.toList());
        List<TargetIndicator> indicators_ind = service.getAllIndicators();
        model.addAttribute("indicators", indicators_ind);
        model.addAttribute("errors", indicators_err);
        model.addAttribute("structures", TargetIndicator.Structure.values());
        model.addAttribute("divisions", TargetIndicator.Division.values());
        return "order";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, Model model) {
        try {
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                redirectAttributes.addFlashAttribute("message", "Only .xlsx files are allowed!");
                return "redirect:/api/order";
            }
            service.importFromXls(file);
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        } catch (IOException e) {
            logger.error("Error during file upload: ", e);
            redirectAttributes.addFlashAttribute("message", "Error uploading file: " + e.getMessage());
        }
        model.addAttribute("indicators", service.getAllIndicators());
        model.addAttribute("errors", service.getAllErrors());
        model.addAttribute("structures", TargetIndicator.Structure.values());
        model.addAttribute("divisions", TargetIndicator.Division.values());
        model.addAttribute("message", "Файл загружен успешно!");
        return "redirect:/api/order";
    }

//    @PostMapping("/transfer")
//    public String transferErrors(@RequestParam("errorIds") List<Long> errorIds, RedirectAttributes redirectAttributes) {
////        service.transferErrors(errorIds);
//        redirectAttributes.addFlashAttribute("message", "Errors transferred successfully!");
//        return "redirect:/api/order";
//    }
    @PostMapping("/transfer")
    public String transferErrors(@RequestParam(value = "errorIds", required = false) List<Long> errorIds, Model model) {
        if (errorIds == null || errorIds.isEmpty()) {
            // Добавляем сообщение об ошибке в модель
            model.addAttribute("transferMessage", "Ошибка: Выберите хотя бы одну строку из (Ошибки XLS файла) для переноса в основной экран.");
            // Перезагружаем данные страницы (чтобы отобразить сообщение)
            model.addAttribute("indicators", service.getAllIndicators()); // Или ваш метод для получения indicators
            model.addAttribute("errors", service.getAllErrors()); // Или ваш метод для получения errors
            model.addAttribute("structures", TargetIndicator.Structure.values()); // Если нужно
            model.addAttribute("divisions", TargetIndicator.Division.values()); // Если нужно
            return "order"; // Возврат на ту же страницу с сообщением
        }
        // Раскомментируем и выполняем логику переноса
        service.transferErrors(errorIds);
        // После успешного переноса перенаправляем обратно с сообщением
        model.addAttribute("message", "Ошибки успешно перенесены в основной экран.");
        model.addAttribute("indicators", service.getAllIndicators());
        model.addAttribute("errors", service.getAllErrors());
        model.addAttribute("structures", TargetIndicator.Structure.values());
        model.addAttribute("divisions", TargetIndicator.Division.values());
        return "order";
    }

    @PostMapping("/update/{id}")
    public String updateIndicator(@PathVariable Long id, @ModelAttribute TargetIndicator indicator, RedirectAttributes redirectAttributes) {
        indicator.setId(id);
        service.updateIndicator(indicator);
        redirectAttributes.addFlashAttribute("message", "Indicator updated!");
        return "redirect:/api/order";
    }

    @PostMapping("/update-error/{id}")
    public String updateError(@PathVariable Long id, @ModelAttribute ErrorIndicator error, RedirectAttributes redirectAttributes) {
        error.setId(id);
        service.updateError(error);
        redirectAttributes.addFlashAttribute("message", "Error updated!");
        return "redirect:/api/order";
    }

    @GetMapping("/export/{type}")
    public ResponseEntity<byte[]> export(@PathVariable String type) throws IOException {
        byte[] data = service.exportToXls(type);
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
    @GetMapping("/clear")
    public String clearIndicators(Model model) {
        // Очищаем indicators (делаем пустым списком)
        service.deleteAllIndicators();
        model.addAttribute("message", "Экран очищен. Indicators удалены.");

        return "redirect:/api/order";  // Возвращаем тот же шаблон
    }
}
