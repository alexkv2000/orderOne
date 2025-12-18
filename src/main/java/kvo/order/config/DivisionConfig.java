package kvo.order.config;

import kvo.order.model.TargetIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DivisionConfig {
    private static final Logger logger = LoggerFactory.getLogger(DivisionConfig.class);

//    private static String SETTINGS_FILE = Paths.get("config", "setting.txt").toString();
    private volatile List<TargetIndicator.Division> divisions = Collections.emptyList();
    private static final String SETTINGS_FILE = new StringBuilder()
            .append("config")
            .append(java.io.File.separator)
            .append("setting.txt")
            .toString();

    // Инициализация при старте
    public DivisionConfig() {
        loadDivisions();
    }

    // Метод для получения текущего списка дивизионов
    public List<TargetIndicator.Division> getDivisions() {
        return new ArrayList<>(divisions); // Возвращаем копию для потокобезопасности
    }

    // Загрузка из файла
    private synchronized void loadDivisions() {
        List<TargetIndicator.Division> newDivisions = new ArrayList<>();
        System.out.println(SETTINGS_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("app.divisions=")) {
                    String value = line.substring("app.divisions=".length()).trim();
                    // Разбираем строку: разделяем по , и игнорируем пробелы
                    String[] parts = value.split("[,\\s]+");
                    for (String part : parts) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty()) {
                            newDivisions.add(new TargetIndicator.Division(trimmed));
                        }
                    }
                    break; // Предполагаем, что параметр один
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка чтения файла settings.txt: {}", e.getMessage());
            // В случае ошибки оставляем старый список или добавляем дефолтные
            newDivisions.add(new TargetIndicator.Division("EMPTY"));
            newDivisions.add(new TargetIndicator.Division("error"));
        }
        divisions = newDivisions;
        logger.info("Загружены дивизионы: {}", divisions);
    }

    // Scheduled task: проверка каждые 15 минут
    @Scheduled(fixedRate = 300000) // 300000  5 минут = 900 секунд * 1000
    public void refreshDivisions() {
        logger.info("Проверка обновлений в settings.txt...");
        loadDivisions();
    }
}
