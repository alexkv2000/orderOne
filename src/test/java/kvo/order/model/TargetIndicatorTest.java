package kvo.order.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TargetIndicatorTest {

    @Test
    void testDivisionList() {
        TargetIndicator indicator = new TargetIndicator();
        List<TargetIndicator.Division> availableDivisions = Arrays.asList(
            new TargetIndicator.Division("Div1"),
            new TargetIndicator.Division("Div2")
        );

        indicator.setDivisions("Div1, Div2");
        List<TargetIndicator.Division> divisionList = indicator.getDivisionList(availableDivisions);
        assertThat(divisionList).hasSize(2);
        assertThat(divisionList.get(0).getDisplayName()).isEqualTo("Div1");

        indicator.setDivisionList(Arrays.asList(new TargetIndicator.Division("NewDiv")));
        assertThat(indicator.getDivisions()).isEqualTo("NewDiv");
    }

    @Test
    void testLegacyDivisionMethods() {
        TargetIndicator indicator = new TargetIndicator();
        List<TargetIndicator.Division> availableDivisions = Arrays.asList(
            new TargetIndicator.Division("Div1")
        );

        indicator.setDivisions("Div1");
        TargetIndicator.Division division = indicator.getDivision(availableDivisions);
        assertThat(division.getDisplayName()).isEqualTo("Div1");

        indicator.setDivision(new TargetIndicator.Division("SingleDiv"));
        assertThat(indicator.getDivisions()).isEqualTo("SingleDiv");
    }

    @Test
    void testGettersAndSetters() {
        TargetIndicator indicator = new TargetIndicator();
        indicator.setId(1L);
        indicator.setNumber("IND001");
        indicator.setStructure(TargetIndicator.Structure.EVENT);
        indicator.setLevel("Level1");
        indicator.setGoal("Achieve goal");
        indicator.setCoordinator("Coordinator");
        indicator.setOwner("Owner");
        indicator.setStatus("valid");

        assertThat(indicator.getId()).isEqualTo(1L);
        assertThat(indicator.getNumber()).isEqualTo("IND001");
        assertThat(indicator.getStructure()).isEqualTo(TargetIndicator.Structure.EVENT);
        assertThat(indicator.getLevel()).isEqualTo("Level1");
        assertThat(indicator.getGoal()).isEqualTo("Achieve goal");
        assertThat(indicator.getCoordinator()).isEqualTo("Coordinator");
        assertThat(indicator.getOwner()).isEqualTo("Owner");
        assertThat(indicator.getStatus()).isEqualTo("valid");
    }

    @Test
    void testVersionComparator() {
        TargetIndicator ind1 = new TargetIndicator();
        ind1.setNumber("1.2.3");

        TargetIndicator ind2 = new TargetIndicator();
        ind2.setNumber("1.2.4");

        assertThat(TargetIndicator.VERSION_COMPARATOR.compare(ind1, ind2)).isNegative();
    }
}
