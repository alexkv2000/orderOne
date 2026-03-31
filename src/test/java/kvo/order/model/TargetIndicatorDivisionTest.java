package kvo.order.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TargetIndicatorDivisionTest {

    @Test
    void testDivisionEqualsAndHashCode() {
        TargetIndicator.Division div1 = new TargetIndicator.Division("Test");
        TargetIndicator.Division div2 = new TargetIndicator.Division("Test");
        TargetIndicator.Division div3 = new TargetIndicator.Division("Different");

        assertThat(div1).isEqualTo(div2);
        assertThat(div1).isNotEqualTo(div3);
        assertThat(div1.hashCode()).isEqualTo(div2.hashCode());
    }

    @Test
    void testDivisionToString() {
        TargetIndicator.Division div = new TargetIndicator.Division("Test Division");
        assertThat(div.toString()).isEqualTo("Test Division");
        assertThat(div.getDisplayName()).isEqualTo("Test Division");
    }

    @Test
    void testFromDisplayName() {
        List<TargetIndicator.Division> divisions = Arrays.asList(
            new TargetIndicator.Division("Group1"),
            new TargetIndicator.Division("Group2")
        );

        assertThat(TargetIndicator.Division.fromDisplayName("Group1", divisions))
            .isPresent()
            .contains(new TargetIndicator.Division("Group1"));

        assertThat(TargetIndicator.Division.fromDisplayName("NonExistent", divisions))
            .isEmpty();
    }

    @Test
    void testFromStringList() {
        List<TargetIndicator.Division> availableDivisions = Arrays.asList(
            new TargetIndicator.Division("Group1"),
            new TargetIndicator.Division("Group2"),
            new TargetIndicator.Division("Group3")
        );

        List<TargetIndicator.Division> result = TargetIndicator.Division.fromStringList("Group1, Group2", availableDivisions);
        assertThat(result).hasSize(2);
        assertThat(result).contains(new TargetIndicator.Division("Group1"), new TargetIndicator.Division("Group2"));

        // Test with non-existent division
        result = TargetIndicator.Division.fromStringList("Group1, NonExistent", availableDivisions);
        assertThat(result).hasSize(2);
        assertThat(result.get(1).getDisplayName()).isEqualTo("error");
    }

    @Test
    void testToStringMethod() {
        List<TargetIndicator.Division> divisions = Arrays.asList(
            new TargetIndicator.Division("Div1"),
            new TargetIndicator.Division("Div2")
        );

        String result = TargetIndicator.Division.toString(divisions);
        assertThat(result).isEqualTo("Div1, Div2");

        assertThat(TargetIndicator.Division.toString(List.of())).isEqualTo("");
    }
}
