package kvo.order.repository;

import kvo.order.model.TargetIndicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TargetIndicatorRepositoryTest {

    @Autowired
    private TargetIndicatorRepository repository;

    @Test
    void testFindByNumber() {
        TargetIndicator indicator = new TargetIndicator();
        indicator.setNumber("TEST001");
        indicator.setStructure(TargetIndicator.Structure.EVENT);
        repository.save(indicator);

        Optional<TargetIndicator> found = repository.findByNumber("TEST001");
        assertThat(found).isPresent();
        assertThat(found.get().getNumber()).isEqualTo("TEST001");
    }

    @Test
    void testFindByStructure() {
        TargetIndicator indicator1 = new TargetIndicator();
        indicator1.setNumber("TEST001");
        indicator1.setStructure(TargetIndicator.Structure.EVENT);
        repository.save(indicator1);

        TargetIndicator indicator2 = new TargetIndicator();
        indicator2.setNumber("TEST002");
        indicator2.setStructure(TargetIndicator.Structure.SECTION);
        repository.save(indicator2);

        List<TargetIndicator> results = repository.findByStructure(TargetIndicator.Structure.EVENT);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getNumber()).isEqualTo("TEST001");
    }

    @Test
    void testFindByOwner() {
        TargetIndicator indicator = new TargetIndicator();
        indicator.setNumber("TEST001");
        indicator.setOwner("TestOwner");
        repository.save(indicator);

        List<TargetIndicator> results = repository.findByOwner("TestOwner");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOwner()).isEqualTo("TestOwner");
    }

    @Test
    void testExistsByNumber() {
        TargetIndicator indicator = new TargetIndicator();
        indicator.setNumber("TEST001");
        repository.save(indicator);

        assertThat(repository.existsByNumber("TEST001")).isTrue();
        assertThat(repository.existsByNumber("NONEXISTENT")).isFalse();
    }

    @Test
    void testDeleteByNumber() {
        TargetIndicator indicator = new TargetIndicator();
        indicator.setNumber("TEST001");
        repository.save(indicator);

        repository.deleteByNumber("TEST001");
        assertThat(repository.findByNumber("TEST001")).isEmpty();
    }

    @Test
    void testCountByStructure() {
        TargetIndicator indicator1 = new TargetIndicator();
        indicator1.setNumber("TEST001");
        indicator1.setStructure(TargetIndicator.Structure.EVENT);
        repository.save(indicator1);

        TargetIndicator indicator2 = new TargetIndicator();
        indicator2.setNumber("TEST002");
        indicator2.setStructure(TargetIndicator.Structure.EVENT);
        repository.save(indicator2);

        long count = repository.countByStructure(TargetIndicator.Structure.EVENT);
        assertThat(count).isEqualTo(2);
    }
}
