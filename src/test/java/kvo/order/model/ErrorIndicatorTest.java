package kvo.order.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorIndicatorTest {

    @Test
    void testDivisionList() {
        ErrorIndicator indicator = new ErrorIndicator();
        indicator.setDivisions("Div1; Div2; Div3");

        List<String> divisionList = indicator.getDivisionList();
        assertThat(divisionList).containsExactly("Div1", "Div2", "Div3");

        indicator.setDivisionList(List.of("NewDiv1", "NewDiv2"));
        assertThat(indicator.getDivisions()).isEqualTo("NewDiv1; NewDiv2");
    }

    @Test
    void testLegacyDivisionMethods() {
        ErrorIndicator indicator = new ErrorIndicator();
        indicator.setDivisions("Div1; Div2");

        // Test getDivision (returns first)
        assertThat(indicator.getDivision()).isEqualTo("Div1");

        // Test setDivision
        indicator.setDivision("SingleDiv");
        assertThat(indicator.getDivisions()).isEqualTo("SingleDiv");
    }

    @Test
    void testGettersAndSetters() {
        ErrorIndicator indicator = new ErrorIndicator();
        indicator.setId(1L);
        indicator.setNumber("ERR001");
        indicator.setStructure(TargetIndicator.Structure.ERROR);
        indicator.setLevel("High");
        indicator.setGoal("Fix issue");
        indicator.setCoordinator("Coordinator");
        indicator.setOwner("Owner");
        indicator.setErrorMessage("Error message");

        assertThat(indicator.getId()).isEqualTo(1L);
        assertThat(indicator.getNumber()).isEqualTo("ERR001");
        assertThat(indicator.getStructure()).isEqualTo(TargetIndicator.Structure.ERROR);
        assertThat(indicator.getLevel()).isEqualTo("High");
        assertThat(indicator.getGoal()).isEqualTo("Fix issue");
        assertThat(indicator.getCoordinator()).isEqualTo("Coordinator");
        assertThat(indicator.getOwner()).isEqualTo("Owner");
        assertThat(indicator.getErrorMessage()).isEqualTo("Error message");
    }
}
