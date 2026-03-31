package kvo.order.service;

import kvo.order.config.DivisionConfig;
import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import kvo.order.repository.ErrorIndicatorRepository;
import kvo.order.repository.TargetIndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndicatorServiceTest {

    @Mock
    private TargetIndicatorRepository targetRepo;

    @Mock
    private ErrorIndicatorRepository errorRepo;

    @Mock
    private DivisionConfig divisionConfig;

    @InjectMocks
    private IndicatorService service;

    @BeforeEach
    void setUp() {
        // Setup mock divisions
        lenient().when(divisionConfig.getDivisions()).thenReturn(Arrays.asList(
            new TargetIndicator.Division("Группа"),
            new TargetIndicator.Division("Болтен")
        ));
    }

    @Test
    void testGetAllIndicators() {
        List<TargetIndicator> indicators = Arrays.asList(new TargetIndicator(), new TargetIndicator());
        when(targetRepo.findAll()).thenReturn(indicators);

        List<TargetIndicator> result = service.getAllIndicators();
        assertThat(result).hasSize(2);
        verify(targetRepo).findAll();
    }

    @Test
    void testGetAllErrors() {
        List<ErrorIndicator> errors = Arrays.asList(new ErrorIndicator(), new ErrorIndicator());
        when(errorRepo.findAll()).thenReturn(errors);

        List<ErrorIndicator> result = service.getAllErrors();
        assertThat(result).hasSize(2);
        verify(errorRepo).findAll();
    }

    @Test
    void testDeleteAllIndicators() {
        service.deleteAllIndicators();
        verify(targetRepo).deleteAll();
    }

    @Test
    void testDeleteAllErrors() {
        service.deleteAllErrors();
        verify(errorRepo).deleteAll();
    }

    @Test
    void testTransferErrors() {
        List<Long> errorIds = Arrays.asList(1L, 2L);

        ErrorIndicator error1 = new ErrorIndicator();
        error1.setId(1L);
        error1.setNumber("ERR001");

        ErrorIndicator error2 = new ErrorIndicator();
        error2.setId(2L);
        error2.setNumber("ERR002");

        when(errorRepo.findById(1L)).thenReturn(Optional.of(error1));
        when(errorRepo.findById(2L)).thenReturn(Optional.of(error2));

        service.transferErrors(errorIds);

        verify(targetRepo, times(2)).save(any(TargetIndicator.class));
        verify(errorRepo).delete(error1);
        verify(errorRepo).delete(error2);
    }

    @Test
    void testUpdateError() {
        ErrorIndicator existingError = new ErrorIndicator();
        existingError.setId(1L);
        existingError.setNumber("ERR001");

        ErrorIndicator updateData = new ErrorIndicator();
        updateData.setNumber("ERR001_UPDATED");
        updateData.setErrorMessage("Updated message");

        when(errorRepo.findById(1L)).thenReturn(Optional.of(existingError));
        when(errorRepo.save(any(ErrorIndicator.class))).thenReturn(existingError);

        ErrorIndicator result = service.updateError(1L, updateData);

        assertThat(result).isNotNull();
        verify(errorRepo).save(existingError);
    }

    @Test
    void testGetIndicatorById() {
        TargetIndicator indicator = new TargetIndicator();
        indicator.setId(1L);
        when(targetRepo.findById(1L)).thenReturn(Optional.of(indicator));

        TargetIndicator result = service.getIndicatorById(1L);
        assertThat(result).isEqualTo(indicator);
    }

    @Test
    void testSaveIndicator() {
        TargetIndicator indicator = new TargetIndicator();
        when(targetRepo.save(indicator)).thenReturn(indicator);

        TargetIndicator result = service.saveIndicator(indicator);
        assertThat(result).isEqualTo(indicator);
        verify(targetRepo).save(indicator);
    }

    // Note: importFromXls and exportToXls would require more complex mocking of MultipartFile and Workbook
    // For brevity, we'll skip those in this example, but in a real scenario, you'd want to test them
}
