import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Class to test Triangle calculations
 */
public class TriangleTest {

    // Constant deviation value to be used to compare double values.
    public final double DELTA = 0.0000001;

    @Test
    public void validTriangle() {
        assertEquals(0.4330127018922193, Triangle.calcArea(1,1,1), DELTA);
    }

    @Test
    public void negativeValues() {
        assertFalse(Triangle.isValid(-1,1,1));
        assertFalse(Triangle.isValid(1,-1,1));
        assertFalse(Triangle.isValid(1,1,-1));
    }

    @Test
    public void invalidValues() {
        assertFalse(Triangle.isValid(1,1,2));
        assertFalse(Triangle.isValid(1,2,1));
        assertFalse(Triangle.isValid(2,1,1));
    }

    @Test(expected = NotATriangleException.class)
    public void testIllegalArgumentException_negativeValue() throws Exception{
        Triangle.calcArea(-1,1,1);
    }

    @Test(expected = NotATriangleException.class)
    public void testIllegalArgumentException_invalidValue() throws Exception{
        Triangle.calcArea(1,1,2);
    }

}
