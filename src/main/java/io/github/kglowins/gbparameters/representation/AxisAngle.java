package io.github.kglowins.gbparameters.representation;


import io.github.kglowins.gbparameters.utils.SaferMath;

import static java.lang.Math.IEEEremainder;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;

public class AxisAngle {

	private static final double TWO_PI = 2 * PI;
	private static final double EPSILON = 1e-4d;

	private UnitVector axis;
    private double angle;

    public UnitVector axis() {
    	return axis;
    }

    public double angle() {
    	return angle;
    }

    public AxisAngle() {
    	axis = new UnitVector();
    	angle = 0;
    }

    public AxisAngle(AxisAngle other) {
    	axis = new UnitVector(other.axis());
    	angle = other.angle();
    }

    public void set(AxisAngle other) {
    	axis.set(other.axis());
    	angle = other.angle();
    }

    public void set(UnitVector axis, double angle) {
    	this.axis.set(axis);
    	double w = angle;
    	
    	if (abs(w) > TWO_PI) {
    		w = IEEEremainder(w, TWO_PI);
		}
    	if (w < 0d) {
    		w += TWO_PI;
		}
    	if (w > PI) {
    		w = TWO_PI - w;
    		this.axis.negate();
    	}
    	this.angle = w;    	
    }

    public final void set(Matrix3x3 M) {
    	if (M.isSymmetric()) {
    		EulerAngles eul = new EulerAngles();
    		eul.set(M);    		
    		set(eul);
    	} else {
    		double x = M.e21() - M.e12();
    		double y = M.e02() - M.e20();
    		double z = M.e10() - M.e01();
    		double norm = Math.sqrt(x*x + y*y + z*z);
    		
    		x /= norm;
    		y /= norm;
    		z /= norm;
    		
            angle = SaferMath.acos((M.tr() - 1) * 0.5);
            axis.set(x, y, z);
    	}
    }

    public void set(EulerAngles eul) {
    	Quaternion quat = new Quaternion();
    	quat.set(eul);
    	set(quat);
    }

    public void set(Quaternion Q) {
    	double x, y, z, w;
    	if(abs(Q.q0() - 1) < EPSILON) {
    		x = 0;
			y = 0;
			z = 1;
			w = 0;
    	} else {
    		double S = SaferMath.sqrt(1 - Q.q0() * Q.q0());
    		x = Q.q1() / S;
    		y = Q.q2() / S;
    		z = Q.q3() / S;    		
    		w = 2 * SaferMath.acos(Q.q0());
    	}
       	UnitVector n = new UnitVector();
    	n.set(x, y, z);
    	set(n, w);
    }

    public void set(RodriguesParams G) {
    	double x, y, z, w;
    	if (G.isHalfTurn()) {
    		throw new IllegalArgumentException("Gibbs vector is infinite");
    	} else {    		
    		double rSq = Math.sqrt(G.r1()*G.r1() + G.r2()*G.r2() + G.r3()*G.r3());
    		if(rSq > EPSILON) {
    			x = G.r1() / rSq;
    			y = G.r2() / rSq;
    			z = G.r3() / rSq;
    			w = 2 * SaferMath.atan(rSq);
    		} else {
    			x = 0;
    			y = 0;
    			z = 1;
    			w = 0;
    		}
    	}
    	UnitVector n = new UnitVector();
    	n.set(x, y, z);
    	set(n, w);
    }

    @Override
    public String toString() {
	    return toDegrees(angle) + "; " + axis;
    } 
}
