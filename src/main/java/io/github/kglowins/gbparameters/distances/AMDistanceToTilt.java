package io.github.kglowins.gbparameters.distances;

import io.github.kglowins.gbparameters.representation.AxisAngle;
import io.github.kglowins.gbparameters.representation.InterfaceMatrix;
import io.github.kglowins.gbparameters.representation.Matrix3x3;
import io.github.kglowins.gbparameters.utils.SaferMath;
import io.github.kglowins.gbparameters.representation.UnitVector;
import org.apache.commons.math3.analysis.MultivariateFunction;

import static org.apache.commons.math3.util.FastMath.atan;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.tan;


public final class AMDistanceToTilt implements MultivariateFunction {
	
	private final InterfaceMatrix B;

	public AMDistanceToTilt(InterfaceMatrix B) {
		this.B = B;
	}

	@Override
	public double value(double[] arg) {

		// misorientation M' axis
		UnitVector n = new UnitVector();
		n.set(arg[0], arg[1]);
		
		// M' matrix		
		AxisAngle aa = new AxisAngle();
		aa.set(n, arg[2]);
		
		Matrix3x3 Mprim = new Matrix3x3();
		Mprim.set(aa);
		
		// M' = R M
		Matrix3x3 R = new Matrix3x3(Mprim);
		R.timesTransposed(B.M());
		
		double m1primAzimuth = arg[3];
		double omega = SaferMath.acos(0.5 * (R.tr() - 1));
		double m1primZenith = atan(-1 /
			(
				tan(n.zenith()) * (
					cos(n.azimuth()) * cos(m1primAzimuth)
						+ sin(n.azimuth()) * sin(m1primAzimuth)
				)
			)
		);
		
		UnitVector m1prim = new UnitVector();
		m1prim.set(m1primZenith, m1primAzimuth);
		
		UnitVector m2prim = new UnitVector(m1prim);
		m2prim.transposedTransform(Mprim);
		m2prim.negate();
		
		double theta1 = SaferMath.acos(B.m1().dot(m1prim));
		double theta2 = SaferMath.acos(B.m2().dot(m2prim));
		
		return omega * omega + 0.5 * (theta1 * theta1 + theta2 * theta2);
	}
}
