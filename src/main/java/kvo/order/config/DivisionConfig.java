package kvo.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class DivisionConfig {
    private List<String> divisions = new ArrayList<>();

    public List<String> getDivisions() {
        return divisions;
    }

    public void setDivisions(List<String> divisions) {
        this.divisions = divisions;
    }
}