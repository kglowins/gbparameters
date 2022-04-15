package io.github.kglowins.gbparams.random;

import io.github.kglowins.gbparams.representation.EulerAngles;
import io.github.kglowins.gbparams.utils.SaferMath;

import static java.lang.Math.PI;

public final class RandomMisorientationGenerator {

	private final MitchellMooreGenerator generator;
	
	public RandomMisorientationGenerator() {
		generator = new MitchellMooreGenerator();
	}
	
	public EulerAngles next() {
		double phi1 = 2 * PI * generator.nextDouble();
		double phi2 = 2 * PI * generator.nextDouble();
		double Phi = SaferMath.acos(2 * generator.nextDouble() - 1);
		EulerAngles angles = new EulerAngles();
		angles.set(phi1, Phi, phi2);
		return angles;
	}
}
