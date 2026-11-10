package nl.tudelft.jpacman.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.jqwik.api.*;
import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.constraints.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test suite to confirm that {@link Unit}s correctly (de)occupy squares.
 *
 * @author Jeroen Roosen 
 *
 */
class OccupantTest {

    //unit under test.
    private Unit unit;

    //resets the unit under test.
    @BeforeEach
    void setUp() {
        unit = new BasicUnit();
    }

    //specification: a newly created unit does not occupy a square.
    @Test
    void specificationUnitHasNoStartSquare() {
        assertThat(unit.hasSquare()).isFalse();
    }

    //specification: occupying a square associates the unit and the square.
    @Test
    void specificationOccupyAssociatesUnitWithSquare() {
        Square square = new BasicSquare();

        unit.occupy(square);

        assertThat(unit.hasSquare()).isTrue();
        assertThat(unit.getSquare()).isSameAs(square);
        assertThat(square.getOccupants()).containsExactly(unit);
    }

    //specification: when a unit occupies a new square, it is removed from the old square.
    @Test
    void specificationReoccupyRemovesFromPreviousSquare() {
        Square firstSquare = new BasicSquare();
        Square secondSquare = new BasicSquare();

        unit.occupy(firstSquare);
        unit.occupy(secondSquare);

        assertThat(unit.getSquare()).isSameAs(secondSquare);
        assertThat(secondSquare.getOccupants()).containsExactly(unit);
        assertThat(firstSquare.getOccupants()).isEmpty();
    }

    //specification: reoccupying the same square should not duplicate the unit.
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void specificationReoccupySameOrDifferentSquare(boolean useSameSquare) {
        Square firstSquare = new BasicSquare();
        Square secondSquare = useSameSquare ? firstSquare : new BasicSquare();

        unit.occupy(firstSquare);
        unit.occupy(secondSquare);

        assertThat(unit.getSquare()).isSameAs(secondSquare);
        assertThat(secondSquare.getOccupants()).containsExactly(unit);

        if (useSameSquare) {
            assertThat(firstSquare.getOccupants()).containsExactly(unit);
        } else {
            assertThat(firstSquare.getOccupants()).isEmpty();
        }
    }

    //specification: leaving a square clears the unit's square reference and removes the unit from the square.
    @Test
    void specificationLeaveSquareClearsOccupancy() {
        Square square = new BasicSquare();

        unit.occupy(square);
        unit.leaveSquare();

        assertThat(unit.hasSquare()).isFalse();
        assertThat(square.getOccupants()).doesNotContain(unit);
    }

    //specification: a unit that leaves a square may reoccupy another square later.
    @Test
    void specificationCanReoccupyAfterLeaving() {
        Square firstSquare = new BasicSquare();
        Square secondSquare = new BasicSquare();

        unit.occupy(firstSquare);
        unit.leaveSquare();
        unit.occupy(secondSquare);

        assertThat(unit.getSquare()).isSameAs(secondSquare);
        assertThat(firstSquare.getOccupants()).doesNotContain(unit);
        assertThat(secondSquare.getOccupants()).containsExactly(unit);
    }

    //property: occupancy is symmetric; the unit's square and square occupants stay consistent.
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void propertyOccupancyInvariant(boolean moveToDifferentSquare) {
        Square firstSquare = new BasicSquare();
        Square secondSquare = moveToDifferentSquare ? new BasicSquare() : firstSquare;

        unit.occupy(firstSquare);
        unit.occupy(secondSquare);

        assertThat(unit.hasSquare()).isTrue();
        assertThat(unit.getSquare()).isSameAs(secondSquare);
        assertThat(secondSquare.getOccupants()).containsExactly(unit);
        assertThat(secondSquare.getOccupants()).allMatch(occupant -> occupant.getSquare() == secondSquare);

        if (moveToDifferentSquare) {
            assertThat(firstSquare.getOccupants()).doesNotContain(unit);
        }
    }

    //property: leaving a square always removes the unit from that square.
    @Test
    void propertyLeaveRemovesUnitFromSquare() {
        Square square = new BasicSquare();

        unit.occupy(square);
        unit.leaveSquare();

        assertThat(unit.hasSquare()).isFalse();
        assertThat(square.getOccupants()).doesNotContain(unit);
    }

    //jqwik example: occupying a square associates the unit with that square.
    @Example
    void propertyOccupyAssociatesUnitWithSquare() {
        Square square = new BasicSquare();
        Unit testUnit = new BasicUnit();

        testUnit.occupy(square);

        assertThat(testUnit.hasSquare()).isTrue();
        assertThat(testUnit.getSquare()).isSameAs(square);
        assertThat(square.getOccupants()).containsExactly(testUnit);
    }

    //jqwik property: after multiple occupations, the unit is only on the last square.
    @Property
    void propertyMultipleOccupationsEndOnLastSquare(@ForAll @IntRange(min = 1, max = 5) int numOccupations) {
        List<Square> squares = IntStream.range(0, numOccupations)
            .mapToObj(i -> new BasicSquare())
            .collect(Collectors.toList());
        Unit testUnit = new BasicUnit();

        for (Square sq : squares) {
            testUnit.occupy(sq);
        }

        Square lastSquare = squares.get(squares.size() - 1);
        assertThat(testUnit.getSquare()).isSameAs(lastSquare);
        assertThat(lastSquare.getOccupants()).containsExactly(testUnit);

        for (int i = 0; i < squares.size() - 1; i++) {
            assertThat(squares.get(i).getOccupants()).isEmpty();
        }
    }

    //jqwik example: leaving a square clears the occupancy.
    @Example
    void propertyLeaveClearsOccupancy() {
        Square square = new BasicSquare();
        Unit testUnit = new BasicUnit();

        testUnit.occupy(square);
        testUnit.leaveSquare();

        assertThat(testUnit.hasSquare()).isFalse();
        assertThat(square.getOccupants()).doesNotContain(testUnit);
    }

    //jqwik property: reoccupying the same square does not duplicate the unit.
    @Property
    void propertyReoccupySameSquareNoDuplicates(@ForAll @IntRange(min = 1, max = 5) int numReoccupations) {
        Square square = new BasicSquare();
        Unit testUnit = new BasicUnit();

        for (int i = 0; i <= numReoccupations; i++) {
            testUnit.occupy(square);
        }

        assertThat(square.getOccupants()).containsExactly(testUnit);
        assertThat(testUnit.getSquare()).isSameAs(square);
    }
}
