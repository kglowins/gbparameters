package io.github.kglowins.gbparameters.utils;

import static java.lang.Math.PI;

public class SaferMath {
	
	private static final double TWO_PI = 2 * PI;

	private SaferMath() {
    }
	    
    public static double sqrt(double x) {
        return x < 0 ? 0 : Math.sqrt(x);
    }

    public static double acos(double x) {
    	if (x > 1) {
    	    return 0;
        } else if (x < -1) {
    	    return PI;
        } else {
    	    return Math.acos(x);
        }
    }

    public static double atan2(double y, double x) {
    	double angle = Math.atan2(y, x);
    	return angle < 0 ? TWO_PI + angle : angle;
    }

    public static double atan(double arg) {
    	double angle = Math.atan(arg);
    	return (angle < 0) ? PI + angle : angle;
    }    

    public static int gcd(int x, int y) {
        while (y != 0) {
            int temp = y;
            y = x % y;
            x = temp;
        }
        return x;
    }
}
