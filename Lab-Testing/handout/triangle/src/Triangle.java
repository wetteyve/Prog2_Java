/*
 * Triangle calculations
 */
public class Triangle {

    /**
     * Calculate area of the given Triangle.
     *
     * @param a  lenght of side a of the triangle
     * @param b  lenght of side b of the triangle
     * @param c  lenght of side c of the triangle
     * @return   calculated area of the triangle
     * @throws   IllegalArgumentException, if invalid parameters are declared
     */
    public static double calcArea(double a, double b, double c) {
        if (isValid(a,b,c)) {
            double area = 0;
            double s = (a + b + c) / 2;
            area = Math.sqrt(s * (s - a) * (s - b) * (s - c));
            return area;
        }
        else throw new NotATriangleException("Invalid parameters!");
    }

    public static boolean isValid(double a, double b, double c) {
        if(a+b > c &&
            b+c > a &&
            a+c > b) return true;
        else {
            return false;
        }
    }

}
