package io.github.kglowins.gbparameters.random;

import io.github.kglowins.gbparameters.representation.EulerAngles;
import io.github.kglowins.gbparameters.representation.InterfaceMatrix;
import io.github.kglowins.gbparameters.representation.Matrix3x3;
import io.github.kglowins.gbparameters.utils.SaferMath;
import io.github.kglowins.gbparameters.representation.UnitVector;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public final class RandomBoundaryGenerator {

	private final MitchellMooreGenerator generator;
	
	public RandomBoundaryGenerator() {
		generator = new MitchellMooreGenerator();
	}

	public InterfaceMatrix nextGB() {
				
		// generate Euler angles/misorientation
		double phi1 = 2 * PI * generator.nextDouble();
		double phi2 = 2 * PI * generator.nextDouble();
		double Phi = SaferMath.acos(2 * generator.nextDouble() - 1);
		
		EulerAngles angles = new EulerAngles();
		angles.set(phi1, Phi, phi2);
		Matrix3x3 M = new Matrix3x3();
		M.set(angles);

		// generate normal to boundary plane
		double theta = 2 * PI * generator.nextDouble();
		double u = -1 + 2 * generator.nextDouble();
		double sq = Math.sqrt(1 - u * u);
		
		UnitVector m1 = new UnitVector();
		m1.set(cos(theta) * sq, sin(theta) * sq, u);
						
		InterfaceMatrix B = new InterfaceMatrix(M, m1);
		return B;
	}
}
