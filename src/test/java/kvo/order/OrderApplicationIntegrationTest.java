package kvo.order;

import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import kvo.order.repository.ErrorIndicatorRepository;
import kvo.order.repository.TargetIndicatorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Use test profile if needed
class OrderApplicationIntegrationTest {

    @Autowired
    private TargetIndicatorRepository targetRepo;

    @Autowired
    private ErrorIndicatorRepository errorRepo;

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
        assertThat(targetRepo).isNotNull();
        assertThat(errorRepo).isNotNull();
    }

    @Test
    void testSaveAndRetrieveIndicators() {
        // Create and save a target indicator
        TargetIndicator indicator = new TargetIndicator();
        indicator.setNumber("INT001");
        indicator.setStructure(TargetIndicator.Structure.EVENT);
        indicator.setGoal("Integration test goal");

        TargetIndicator saved = targetRepo.save(indicator);
        assertThat(saved.getId()).isNotNull();

        // Retrieve and verify
        List<TargetIndicator> indicators = targetRepo.findAll();
        assertThat(indicators).isNotEmpty();
        assertThat(indicators.stream().anyMatch(i -> "INT001".equals(i.getNumber()))).isTrue();

        // Clean up
        targetRepo.delete(saved);
    }

    @Test
    void testSaveAndRetrieveErrors() {
        // Create and save an error indicator
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR_INT001");
        error.setStructure(TargetIndicator.Structure.ERROR);
        error.setErrorMessage("Integration test error");

        ErrorIndicator saved = errorRepo.save(error);
        assertThat(saved.getId()).isNotNull();

        // Retrieve and verify
        List<ErrorIndicator> errors = errorRepo.findAll();
        assertThat(errors).isNotEmpty();
        assertThat(errors.stream().anyMatch(e -> "ERR_INT001".equals(e.getNumber()))).isTrue();

        // Clean up
        errorRepo.delete(saved);
    }

    @Test
    void testRepositoryQueries() {
        // Save test data
        TargetIndicator indicator = new TargetIndicator();
        indicator.setNumber("QUERY001");
        indicator.setStructure(TargetIndicator.Structure.SECTION);
        indicator.setOwner("TestOwner");
        targetRepo.save(indicator);

        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR_QUERY001");
        error.setStructure(TargetIndicator.Structure.ERROR);
        error.setErrorMessage("Query test error");
        errorRepo.save(error);

        // Test queries
        assertThat(targetRepo.findByStructure(TargetIndicator.Structure.SECTION)).hasSizeGreaterThan(0);
        assertThat(targetRepo.findByOwner("TestOwner")).hasSizeGreaterThan(0);
        assertThat(errorRepo.findByErrorMessage("Query test error")).hasSizeGreaterThan(0);

        // Clean up
        targetRepo.delete(indicator);
        errorRepo.delete(error);
    }
}
