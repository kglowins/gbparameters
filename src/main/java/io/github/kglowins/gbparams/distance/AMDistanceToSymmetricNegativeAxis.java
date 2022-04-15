package io.github.kglowins.gbparams.distance;

import io.github.kglowins.gbparams.representation.AxisAngle;
import io.github.kglowins.gbparams.representation.InterfaceMatrix;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import io.github.kglowins.gbparams.utils.SaferMath;
import io.github.kglowins.gbparams.representation.UnitVector;
import org.apache.commons.math3.analysis.MultivariateFunction;

import static java.lang.Math.PI;

public final class AMDistanceToSymmetricNegativeAxis implements MultivariateFunction {

	private final InterfaceMatrix B;

	public AMDistanceToSymmetricNegativeAxis(InterfaceMatrix B) {
		this.B = B;
	}

	@Override
	public double value(double[] arg) {

		// misorientation M' axis
		UnitVector n = new UnitVector();
		n.set(arg[0], arg[1]);
				
		// M' matrix		
		AxisAngle aa = new AxisAngle();
		aa.set(n, PI);
				
		Matrix3x3 Mprim = new Matrix3x3();
		Mprim.set(aa);
					
		// M' = R M
		Matrix3x3 R = new Matrix3x3(Mprim);
		R.timesTransposed(B.M());
				
		double omega = SaferMath.acos(0.5 * (R.tr() - 1));

		// m1' = - axis(R) 
		UnitVector m1prim = new UnitVector(aa.axis());
		UnitVector m2prim = new UnitVector(aa.axis());
		m2prim.transposedTransform(Mprim);
		m1prim.negate();
		
		double theta1 = SaferMath.acos(B.m1().dot(m1prim));
		double theta2 = SaferMath.acos(B.m2().dot(m2prim));
		
		return omega * omega + 0.5 * (theta1 * theta1 + theta2 * theta2);
	}
}
