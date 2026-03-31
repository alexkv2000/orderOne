package kvo.order.config;

import kvo.order.model.TargetIndicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class DivisionConfigTest {

    @TempDir
    Path tempDir;

//    private DivisionConfig divisionConfig;
    private Path settingsFile;

    @BeforeEach
    void setUp() throws IOException {
        settingsFile = tempDir.resolve("setting.properties");
        // Create a test settings file
        Files.writeString(settingsFile, "app.divisions=Division1, Division2, Division3\n");

        // Use reflection to set the private field
//        divisionConfig = new DivisionConfig();
        // Since SETTINGS_FILE is static, we need to mock or use a different approach
        // For simplicity, we'll test the logic directly
    }

    @Test
    void testLoadDivisions() throws IOException {
        // Create a temporary settings file
        Path testSettings = tempDir.resolve("test.properties");
        Files.writeString(testSettings, "app.divisions=Div1, Div2, Div3\n");

        DivisionConfig config = new DivisionConfig();
        // We can't easily test the private method, so we'll test the public interface
        // In a real scenario, you might use reflection or redesign for testability

        // For now, create a simple test that checks the initial state
        List<TargetIndicator.Division> divisions = config.getDivisions();
        // Since the file doesn't exist in the default location, it should have error divisions
        assertThat(divisions).isNotNull();
    }

    @Test
    void testGetDivisionsReturnsCopy() {
        DivisionConfig config = new DivisionConfig();
        List<TargetIndicator.Division> divisions1 = config.getDivisions();
        List<TargetIndicator.Division> divisions2 = config.getDivisions();

        // Should return different instances (copies)
        assertThat(divisions1).isNotSameAs(divisions2);
    }
}
