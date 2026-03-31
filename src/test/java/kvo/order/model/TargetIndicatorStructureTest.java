package kvo.order.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TargetIndicatorStructureTest {

    @Test
    void testFromDisplayName() {
        assertThat(TargetIndicator.Structure.fromDisplayName("МЕРОПРИЯТИЕ")).isEqualTo(TargetIndicator.Structure.EVENT);
        assertThat(TargetIndicator.Structure.fromDisplayName("РАЗДЕЛ")).isEqualTo(TargetIndicator.Structure.SECTION);
        assertThat(TargetIndicator.Structure.fromDisplayName("")).isEqualTo(TargetIndicator.Structure.EMPTY);
        assertThat(TargetIndicator.Structure.fromDisplayName(" ")).isEqualTo(TargetIndicator.Structure.SPACE);
        assertThat(TargetIndicator.Structure.fromDisplayName("UNKNOWN")).isEqualTo(TargetIndicator.Structure.ERROR);
    }

    @Test
    void testGetDisplayName() {
        assertThat(TargetIndicator.Structure.EVENT.getDisplayName()).isEqualTo("МЕРОПРИЯТИЕ");
        assertThat(TargetIndicator.Structure.EMPTY.getDisplayName()).isEmpty();
        assertThat(TargetIndicator.Structure.SPACE.getDisplayName()).isEqualTo(" ");
    }
}
