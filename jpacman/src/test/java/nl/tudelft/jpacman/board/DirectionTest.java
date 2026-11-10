package nl.tudelft.jpacman.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import net.jqwik.api.*;
import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.constraints.*;

/**
 * A very simple (and not particularly useful)
 * test class to have a starting point where to put tests.
 *
 * @author Arie van Deursen
 */
public class DirectionTest {

    //specification-based test: each cardinal direction must use the expected delta values.
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testDirectionDeltasFollowSpecification(Direction direction) {
        int expectedDeltaX;
        int expectedDeltaY;

        switch (direction) {
            case NORTH -> {
                expectedDeltaX = 0;
                expectedDeltaY = -1;
            }
            case SOUTH -> {
                expectedDeltaX = 0;
                expectedDeltaY = 1;
            }
            case WEST -> {
                expectedDeltaX = -1;
                expectedDeltaY = 0;
            }
            case EAST -> {
                expectedDeltaX = 1;
                expectedDeltaY = 0;
            }
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        }

        assertThat(direction.getDeltaX()).isEqualTo(expectedDeltaX);
        assertThat(direction.getDeltaY()).isEqualTo(expectedDeltaY);
    }

    //specification-based test: a direction is cardinal, not diagonal.
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testDirectionIsCardinal(Direction direction) {
        int deltaX = direction.getDeltaX();
        int deltaY = direction.getDeltaY();

        assertThat(deltaX == 0 || deltaY == 0).isTrue();
        assertThat(deltaX != 0 || deltaY != 0).isTrue();
    }

    //specification-based test: direction delta values represent unit steps.
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testDirectionDeltasAreUnitSteps(Direction direction) {
        assertThat(direction.getDeltaX()).isBetween(-1, 1);
        assertThat(direction.getDeltaY()).isBetween(-1, 1);
    }

    //specification-based test: enum names can be converted back to the same instance.
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testValueOfByNameReturnsSameDirection(Direction direction) {
        assertThat(Direction.valueOf(direction.name())).isSameAs(direction);
    }

    //specification-based test: valid names map to the expected enum values.
    @Test
    void testValueOfValidInputs() {
        assertThat(Direction.valueOf("NORTH")).isEqualTo(Direction.NORTH);
        assertThat(Direction.valueOf("SOUTH")).isEqualTo(Direction.SOUTH);
        assertThat(Direction.valueOf("EAST")).isEqualTo(Direction.EAST);
        assertThat(Direction.valueOf("WEST")).isEqualTo(Direction.WEST);
    }

    //specification-based test: invalid names are rejected.
    @Test
    void testValueOfInvalidInput() {
        assertThatThrownBy(() -> Direction.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    //property test: each direction moves exactly one step on the grid
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testDirectionManhattanDistanceIsOne(Direction direction) {
        int manhattanDistance = Math.abs(direction.getDeltaX()) + Math.abs(direction.getDeltaY());
        assertThat(manhattanDistance).isEqualTo(1);
    }

    //property test: exactly one coordinate changes for every cardinal direction
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testExactlyOneCoordinateChanges(Direction direction) {
        int zeroCount = 0;
        if (direction.getDeltaX() == 0) zeroCount++;
        if (direction.getDeltaY() == 0) zeroCount++;

        assertThat(zeroCount).as("Direction %s should have exactly one zero coordinate", direction)
            .isEqualTo(1);
    }

    //property test: opposite directions cancel each other on the appropriate axis
    @Test
    void testOppositeDirectionsCancel() {
        assertThat(Direction.NORTH.getDeltaY() + Direction.SOUTH.getDeltaY()).isZero();
        assertThat(Direction.WEST.getDeltaX() + Direction.EAST.getDeltaX()).isZero();
    }

    //property test: direction delta values remain stable across repeated calls
    @ParameterizedTest
    @EnumSource(Direction.class)
    void testDeltaValuesAreStable(Direction direction) {
        int initialDeltaX = direction.getDeltaX();
        int initialDeltaY = direction.getDeltaY();

        for (int i = 0; i < 10; i++) {
            assertThat(direction.getDeltaX()).isEqualTo(initialDeltaX);
            assertThat(direction.getDeltaY()).isEqualTo(initialDeltaY);
        }
    }

    //mutation test: vertical directions must keep X fixed at zero.
    @ParameterizedTest
    @EnumSource(names = {"NORTH", "SOUTH"})
    void mutationTestVerticalDirectionsHaveZeroDeltaX(Direction direction) {
        assertThat(direction.getDeltaX()).isZero();
        assertThat(direction.getDeltaY()).isNotZero();
    }

    //mutation test: horizontal directions must keep Y fixed at zero.
    @ParameterizedTest
    @EnumSource(names = {"WEST", "EAST"})
    void mutationTestHorizontalDirectionsHaveZeroDeltaY(Direction direction) {
        assertThat(direction.getDeltaY()).isZero();
        assertThat(direction.getDeltaX()).isNotZero();
    }

    //mutation test: direction signs must match intended movement.
    @Test
    void mutationTestDirectionSignSemantics() {
        assertThat(Direction.NORTH.getDeltaY()).isNegative();
        assertThat(Direction.SOUTH.getDeltaY()).isPositive();
        assertThat(Direction.WEST.getDeltaX()).isNegative();
        assertThat(Direction.EAST.getDeltaX()).isPositive();
    }

    //mutation test: active direction axes must always be unit steps.
    @ParameterizedTest
    @EnumSource(Direction.class)
    void mutationTestActiveAxisIsUnitStep(Direction direction) {
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            assertThat(direction.getDeltaX()).isZero();
            assertThat(Math.abs(direction.getDeltaY())).isEqualTo(1);
        } else {
            assertThat(direction.getDeltaY()).isZero();
            assertThat(Math.abs(direction.getDeltaX())).isEqualTo(1);
        }
    }

    @Provide
    Arbitrary<Direction> directions() {
        return Arbitraries.of(Direction.values());
    }

    //jqwik property: opposite directions cancel each other out.
    @Property
    void propertyOppositeDirectionsCancel(@ForAll Direction dir) {
        Direction opposite = getOpposite(dir);

        assertThat(dir.getDeltaX() + opposite.getDeltaX()).isZero();
        assertThat(dir.getDeltaY() + opposite.getDeltaY()).isZero();
    }

    //jqwik property: the opposite of the opposite is the original direction.
    @Property
    void propertyOppositeIsSymmetric(@ForAll Direction dir) {
        Direction opposite = getOpposite(dir);

        assertThat(getOpposite(opposite)).isEqualTo(dir);
    }

    //jqwik property: each direction has exactly one non-zero delta.
    @Property
    void propertyExactlyOneDeltaNonZero(@ForAll Direction dir) {
        int nonZeroCount = 0;
        if (dir.getDeltaX() != 0) nonZeroCount++;
        if (dir.getDeltaY() != 0) nonZeroCount++;

        assertThat(nonZeroCount).isEqualTo(1);
    }

    //jqwik property: direction deltas are always unit steps (magnitude 1).
    @Property
    void propertyDeltasAreUnitMagnitude(@ForAll Direction dir) {
        int deltaX = dir.getDeltaX();
        int deltaY = dir.getDeltaY();

        assertThat(Math.abs(deltaX) + Math.abs(deltaY)).isEqualTo(1);
    }

    private static Direction getOpposite(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case EAST -> Direction.WEST;
            case WEST -> Direction.EAST;
        };
    }
}
