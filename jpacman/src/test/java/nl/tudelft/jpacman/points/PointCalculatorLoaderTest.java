package nl.tudelft.jpacman.points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import net.jqwik.api.*;
import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.api.constraints.*;

class PointCalculatorLoaderTest {

    @BeforeEach
    void resetPointCalculatorLoaderCache() throws ReflectiveOperationException {
        setPointCalculatorLoaderClass(null);
    }

    @AfterEach
    void cleanupPointCalculatorLoaderCache() throws ReflectiveOperationException {
        setPointCalculatorLoaderClass(null);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5})
    void specificationRepeatedLoadReturnsFreshInstances(int repeatCount) {
        PointCalculatorLoader loader = new PointCalculatorLoader();
        assertFreshDefaultCalculators(loader, repeatCount);
    }

    @Test
    void specificationLoadCachesLoadedClass() throws ReflectiveOperationException {
        PointCalculatorLoader loader = new PointCalculatorLoader();

        loader.load();

        Class<?> cachedClass = getPointCalculatorLoaderClass();
        assertEquals(DefaultPointCalculator.class, cachedClass);
    }

    @Test
    void propertyLoadedClassMatchesCachedClass() throws ReflectiveOperationException {
        PointCalculatorLoader loader = new PointCalculatorLoader();

        PointCalculator first = loader.load();
        Class<?> cachedClass = getPointCalculatorLoaderClass();

        assertNotNull(cachedClass);
        assertEquals(first.getClass(), cachedClass);

        PointCalculator second = loader.load();
        assertEquals(cachedClass, second.getClass());
    }

    @Test
    void propertyRepeatedLoadKeepsSameCachedClass() throws ReflectiveOperationException {
        PointCalculatorLoader loader = new PointCalculatorLoader();

        loader.load();
        Class<?> firstCached = getPointCalculatorLoaderClass();
        loader.load();
        Class<?> secondCached = getPointCalculatorLoaderClass();

        assertEquals(firstCached, secondCached);
    }

    @ParameterizedTest
    @MethodSource("invalidCachedClasses")
    void specificationLoadThrowsRuntimeExceptionForInvalidCachedClass(Class<?> invalidClazz) throws ReflectiveOperationException {
        setPointCalculatorLoaderClass(invalidClazz);
        PointCalculatorLoader loader = new PointCalculatorLoader();

        RuntimeException exception = assertThrows(RuntimeException.class, loader::load);
        assertEquals(ClassCastException.class, exception.getCause().getClass());
    }

    @Provide
    static Arbitrary<Class<?>> invalidCachedClassArbitrary() {
        return Arbitraries.of(Object.class, NonPointCalculator.class);
    }

    static Stream<Class<?>> invalidCachedClasses() {
        return Stream.of(Object.class, NonPointCalculator.class);
    }

    public static class NonPointCalculator {
    }

    @Property
    void propertyLoadAlwaysReturnsDefaultCalculator(@ForAll @IntRange(min = 1, max = 10) int repeatCount) throws ReflectiveOperationException {
        clearPointCalculatorLoaderCache();
        PointCalculatorLoader loader = new PointCalculatorLoader();
        assertFreshDefaultCalculators(loader, repeatCount);
    }

    @Property
    void propertyCachedClassMatchesLoadedType(@ForAll @IntRange(min = 1, max = 5) int extraLoads) throws ReflectiveOperationException {
        clearPointCalculatorLoaderCache();
        PointCalculatorLoader loader = new PointCalculatorLoader();
        assertCachedClassMatchesLoadedType(loader, extraLoads);
    }

    @Property
    void propertyInvalidCachedClassThrowsClassCast(@ForAll("invalidCachedClassArbitrary") Class<?> invalidClazz) throws ReflectiveOperationException {
        clearPointCalculatorLoaderCache();
        setPointCalculatorLoaderClass(invalidClazz);
        PointCalculatorLoader loader = new PointCalculatorLoader();

        RuntimeException exception = assertThrows(RuntimeException.class, loader::load);
        assertEquals(ClassCastException.class, exception.getCause().getClass());
    }

    private static void assertFreshDefaultCalculators(PointCalculatorLoader loader, int repeatCount) {
        PointCalculator first = loader.load();
        assertEquals(DefaultPointCalculator.class, first.getClass());

        for (int i = 1; i < repeatCount; i++) {
            PointCalculator subsequent = loader.load();
            assertEquals(DefaultPointCalculator.class, subsequent.getClass());
            assertNotSame(first, subsequent);
        }
    }

    private static void assertCachedClassMatchesLoadedType(PointCalculatorLoader loader, int extraLoads) throws ReflectiveOperationException {
        PointCalculator first = loader.load();
        Class<?> cachedClass = getPointCalculatorLoaderClass();

        assertNotNull(cachedClass);
        assertEquals(first.getClass(), cachedClass);

        for (int i = 0; i < extraLoads; i++) {
            PointCalculator subsequent = loader.load();
            assertEquals(cachedClass, subsequent.getClass());
        }
    }

    private static void clearPointCalculatorLoaderCache() throws ReflectiveOperationException {
        setPointCalculatorLoaderClass(null);
    }

    private static void setPointCalculatorLoaderClass(Class<?> clazz) throws ReflectiveOperationException {
        Field field = PointCalculatorLoader.class.getDeclaredField("clazz");
        field.setAccessible(true);
        field.set(null, clazz);
    }

    private static Class<?> getPointCalculatorLoaderClass() throws ReflectiveOperationException {
        Field field = PointCalculatorLoader.class.getDeclaredField("clazz");
        field.setAccessible(true);
        return (Class<?>) field.get(null);
    }

}
