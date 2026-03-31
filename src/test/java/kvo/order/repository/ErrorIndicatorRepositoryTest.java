package kvo.order.repository;

import kvo.order.model.ErrorIndicator;
import kvo.order.model.TargetIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ErrorIndicatorRepositoryTest {

    @Autowired
    private ErrorIndicatorRepository repository;

    @Test
    void testFindByNumber() {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR001");
        error.setStructure(TargetIndicator.Structure.ERROR);
        repository.save(error);

        Optional<ErrorIndicator> found = repository.findByNumber("ERR001");
        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo("ERR001");
    }

    @Test
    void testFindByStructure() {
        ErrorIndicator error1 = new ErrorIndicator();
        error1.setNumber("ERR001");
        error1.setStructure(TargetIndicator.Structure.ERROR);
        repository.save(error1);

        ErrorIndicator error2 = new ErrorIndicator();
        error2.setNumber("ERR002");
        error2.setStructure(TargetIndicator.Structure.EVENT);
        repository.save(error2);

        List<ErrorIndicator> results = repository.findByStructure(TargetIndicator.Structure.ERROR);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNumber()).isEqualTo("ERR001");
    }

    @Test
    void testFindByErrorMessage() {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR001");
        error.setErrorMessage("Test error message");
        repository.save(error);

        List<ErrorIndicator> results = repository.findByErrorMessage("Test error message");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getErrorMessage()).isEqualTo("Test error message");
    }

    @Test
    void testFindByErrorMessageContaining() {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR001");
        error.setErrorMessage("Test error message");
        repository.save(error);

        List<ErrorIndicator> results = repository.findByErrorMessageContaining("error");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getErrorMessage()).isEqualTo("Test error message");
    }

    @Test
    void testFindByDivisionContaining() {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR001");
        error.setDivisions("Division1; Division2");
        repository.save(error);

        List<ErrorIndicator> results = repository.findByDivisionsContaining("Division1");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNumber()).isEqualTo("ERR001");
    }

    @Test
    void testExistsByNumber() {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR001");
        repository.save(error);

        assertThat(repository.existsByNumber("ERR001")).isTrue();
        assertThat(repository.existsByNumber("NONEXISTENT")).isFalse();
    }

    @Test
    void testDeleteByNumber() {
        ErrorIndicator error = new ErrorIndicator();
        error.setNumber("ERR001");
        repository.save(error);

        repository.deleteByNumber("ERR001");
        assertThat(repository.findByNumber("ERR001")).isEmpty();
    }
}
