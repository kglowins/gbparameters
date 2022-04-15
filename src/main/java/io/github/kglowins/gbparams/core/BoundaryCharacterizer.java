package io.github.kglowins.gbparams.core;

import io.github.kglowins.gbparams.distance.AMDistanceToTilt180;
import io.github.kglowins.gbparams.distance.AMDistanceToSymmetricNegativeAxis;
import io.github.kglowins.gbparams.distance.AMDistanceToSymmetricPositiveAxis;
import io.github.kglowins.gbparams.distance.AMDistanceToTilt;
import io.github.kglowins.gbparams.distance.AMDistanceToTwistNegativeAxis;
import io.github.kglowins.gbparams.distance.AMDistanceToTwistPositiveAxis;
import io.github.kglowins.gbparams.representation.AxisAngle;
import io.github.kglowins.gbparams.representation.CSLMisorientation;
import io.github.kglowins.gbparams.representation.InterfaceMatrix;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import io.github.kglowins.gbparams.utils.SaferMath;
import io.github.kglowins.gbparams.enums.BoundaryParameterName;
import java.util.Collections;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.util.FastMath;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.kglowins.gbparams.enums.BoundaryParameterName.*;
import static java.lang.Math.PI;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;

@Slf4j
@Accessors(fluent = true)
public final class BoundaryCharacterizer {

	private static final double INFINITY = Double.MAX_VALUE;
	private static final double EPSILON_4 = 1e-4d;
	private static final double EPSILON_3 = 1e-3d;
	private static final MaxEval NO_LIMIT = new MaxEval(Integer.MAX_VALUE);

	private final Map<BoundaryParameterName, Boolean> parametersToCompute;
	private List<Matrix3x3> symmetryOperations;
	private boolean grainExchange;
	private boolean inversion;

	private double p;
	private double omega0;
	private List<CSLMisorientation> cslMisorientations;

	@Getter
	private double distanceToTwistAprx;
	@Getter
	private double distanceToTiltAprx;
	@Getter
	private double distanceToSymmetricAprx;
	@Getter
	private double distanceTo180tiltAprx;
		
	private double distanceToTwistAm;
	private double distanceToTiltAm;
	private double distanceToSymmetricAm;
	private double distanceTo180tiltAm;

	@Getter
	private int multiplicity;
	@Getter
	private CSLMisorientation lowestSigma;

	@Getter
	private double twistComponent;
	@Getter
	private double tiltComponent;

	
	public BoundaryCharacterizer() {
		parametersToCompute = new EnumMap<>(BoundaryParameterName.class);
		parametersToCompute.put(TILT_AM, false);
		parametersToCompute.put(TWIST_AM, false);
		parametersToCompute.put(SYMMETRIC_AM, false);
		parametersToCompute.put(TILT_180_AM, false);
		parametersToCompute.put(TILT_APRX, false);
		parametersToCompute.put(TWIST_APRX, false);
		parametersToCompute.put(SYMMETRIC_APRX, false);
		parametersToCompute.put(TILT_180_APRX, false);
		parametersToCompute.put(TILT_COMP, false);
		parametersToCompute.put(TWIST_COMP, false);

		p = 0.5;
		omega0 = Math.toRadians(15);
		lowestSigma = null;

		grainExchange = true;
		inversion = true;
		symmetryOperations = null;

		multiplicity = 0;
		cslMisorientations = null;
	}

	public BoundaryCharacterizer inversion(boolean inversion) {
		this.inversion = inversion;
		return this;
	}

	public BoundaryCharacterizer grainExchange(boolean grainExchange) {
		this.grainExchange = grainExchange;
		return this;
	}

	public BoundaryCharacterizer symmetryOperations(List<Matrix3x3> symmetryOperations) {
		this.symmetryOperations = symmetryOperations;
		return this;
	}

	public BoundaryCharacterizer compute(Set<BoundaryParameterName> params) {
		params.forEach(param -> this.compute(param, true));
		return this;
	}

	public BoundaryCharacterizer compute(BoundaryParameterName param, boolean isNeeded) {
		if (parametersToCompute.containsKey(param)) {
			parametersToCompute.replace(param, isNeeded);
		} else {
			log.warn("BoundaryCharacterizer.compute: ignoring unsupported parameter {}", param);
		}
		return this;
	}

	public BoundaryCharacterizer checkIfCSL(double p, double omega0, List<CSLMisorientation> cslMisorientations) {
		this.p = p;
		this.omega0 = omega0;
		this.cslMisorientations = cslMisorientations;
		return this;
	}

	private void resetBeforeNextBoundary() {
		if (parametersToCompute.get(TILT_AM)) {
			distanceToTiltAm = INFINITY;
		}
		if (parametersToCompute.get(TWIST_AM)) {
			distanceToTwistAm = INFINITY;
		}
		if (parametersToCompute.get(SYMMETRIC_AM)) {
			distanceToSymmetricAm = INFINITY;
		}
		if (parametersToCompute.get(TILT_180_AM)) {
			distanceTo180tiltAm = INFINITY;
		}
		if (isApproximateDistanceNeeded()) {
			distanceToTiltAprx = INFINITY;
			distanceToTwistAprx = INFINITY;
			distanceToSymmetricAprx = INFINITY;
			distanceTo180tiltAprx = INFINITY;
		}
		if (isComponentNeeded()) {
			tiltComponent = INFINITY;
			twistComponent = INFINITY;
		}
		if (nonNull(cslMisorientations)) {
			lowestSigma = null;
		}
		multiplicity = 0;
	}

	public void characterize(InterfaceMatrix B0) {
		resetBeforeNextBoundary();
		List<Boolean> transpose = grainExchange ? asList(false, true) : singletonList(false);
		List<Boolean> invert = inversion ? asList(false, true) : singletonList(false);

		for (boolean isTranspose : transpose) {
			for (boolean isInvert : invert) {
				for (Matrix3x3 C1 : symmetryOperations) {
					for (Matrix3x3 C2 : symmetryOperations) {

						InterfaceMatrix B = new InterfaceMatrix(B0);
						if (isTranspose) {
							B.transpose();
						}
						if (isInvert) {
							B.toMinus();
						}
						B.applySymmetry1(C1);
						B.applySymmetry2(C2);

						AxisAngle aa = new AxisAngle();
						aa.set(B.M());

					//	if (parametersToCompute.get(TILT_AM)) {
							AMDistanceToTilt distanceFunction = new AMDistanceToTilt(B);
							double distance = getMinimumValue(distanceFunction,
									aa.axis().zenith(), aa.axis().azimuth(), aa.angle(), B.m1().azimuth());
							if (distance < distanceToTiltAm) {
								distanceToTiltAm = distance;
							}
					//	}

		/*				if (parametersToCompute.get(TWIST_AM)) {
							AMDistanceToTwistPositiveAxis distancePlusAxis = new AMDistanceToTwistPositiveAxis(B);
							AMDistanceToTwistNegativeAxis distanceMinusAxis = new AMDistanceToTwistNegativeAxis(B);
							double distValPos = getMinimumValue(distancePlusAxis,
									aa.axis().zenith(), aa.axis().azimuth(), aa.angle());
							double distValNeg = getMinimumValue(distanceMinusAxis,
									aa.axis().zenith(), aa.axis().azimuth(), aa.angle());
							if (distValPos < distanceToTwistAm) {
								distanceToTwistAm = distValPos;
							}
							if (distValNeg < distanceToTwistAm) {
								distanceToTwistAm = distValNeg;
							}
						}

						if (parametersToCompute.get(SYMMETRIC_AM)) {
							AMDistanceToSymmetricPositiveAxis distanceFunctionPlusAxis = new AMDistanceToSymmetricPositiveAxis(B);
							AMDistanceToSymmetricNegativeAxis distanceFunctionMinusAxis = new AMDistanceToSymmetricNegativeAxis(B);
							double distancePlusAxis = getMinimumValue(distanceFunctionPlusAxis,
									aa.axis().zenith(), aa.axis().azimuth());
							double distanceMinusAxis = getMinimumValue(distanceFunctionMinusAxis,
									aa.axis().zenith(), aa.axis().azimuth());
							if (distancePlusAxis < distanceToSymmetricAm) {
								distanceToSymmetricAm = distancePlusAxis;
							}
							if (distanceMinusAxis < distanceToSymmetricAm) {
								distanceToSymmetricAm = distanceMinusAxis;
							}
						}

						if (parametersToCompute.get(TILT_180_AM)) {
							AMDistanceToTilt180 distanceFunction = new AMDistanceToTilt180(B);
							double distance = getMinimumValue(distanceFunction,
									aa.axis().zenith(), aa.axis().azimuth(), B.m1().azimuth());
							if (distance < distanceTo180tiltAm) {
								distanceTo180tiltAm = distance;
							}
						}

						if (isApproximateDistanceNeeded()) {
							double alpha = SaferMath.acos(Math.abs(aa.axis().dot(B.m1())));
							double ninetyMinusAlpha = Math.toRadians(90) - alpha;
							double deltaOmega = PI - aa.angle();
							deltaOmega = deltaOmega * deltaOmega;
							final double alphaScandSq = alpha * alpha + deltaOmega;
							final double alphaIcandSq = ninetyMinusAlpha * ninetyMinusAlpha + deltaOmega;
							if (ninetyMinusAlpha < distanceToTiltAprx) {
								distanceToTiltAprx = ninetyMinusAlpha;
							}
							if (alpha < distanceToTwistAprx) {
								distanceToTwistAprx = alpha;
							}
							if (alphaScandSq < distanceToSymmetricAprx) {
								distanceToSymmetricAprx = alphaScandSq;
							}
							if (alphaIcandSq < distanceTo180tiltAprx) {
								distanceTo180tiltAprx = alphaIcandSq;
							}
						}

						if (isComponentNeeded()) {
							final double dot = aa.axis().dot(B.m1());
							double Phi;
							double omega;
							if (Math.abs(dot) < EPSILON_3) {
								Phi = 0.;
								omega = aa.angle();
							} else {
								final double alpha = SaferMath.acos(aa.axis().dot(B.m1()));
								final double tg = FastMath.tan(alpha);
								final double cos = FastMath.cos(0.5 * aa.angle());
								Phi = 2. * FastMath.asin(FastMath.sin(0.5 * aa.angle()) / Math.sqrt(1. + tg * tg * cos * cos));
								omega = 2. * FastMath.asin(FastMath.sin(alpha) * FastMath.sin(0.5 * aa.angle()));
							}
							if (Phi < twistComponent) {
								twistComponent = Phi;
							}
							if (omega < tiltComponent) {
								tiltComponent = omega;
							}
						}

						if (nonNull(cslMisorientations)) {
							for (CSLMisorientation cslMisorientation : cslMisorientations) {
								Matrix3x3 M = new Matrix3x3(B.M());
								M.timesTransposed(cslMisorientation.getM());
								final double angle = SaferMath.acos(0.5 * (M.tr() - 1.));
								if (angle < omega0 / FastMath.pow(cslMisorientation.getSigma(), p)) {
									lowestSigma = cslMisorientation;
									break;
								}
							}
						}

						if (Math.abs(B.m1().x() - B0.m1().x()) < EPSILON_3
								&& Math.abs(B.m1().y() - B0.m1().y()) < EPSILON_3
								&& Math.abs(B.m1().z() - B0.m1().z()) < EPSILON_3
								&& Math.abs(B.M().e00() - B0.M().e00()) < EPSILON_3 &&
								Math.abs(B.M().e01() - B0.M().e01()) < EPSILON_3 &&
								Math.abs(B.M().e02() - B0.M().e02()) < EPSILON_3 &&
								Math.abs(B.M().e10() - B0.M().e10()) < EPSILON_3 &&
								Math.abs(B.M().e11() - B0.M().e11()) < EPSILON_3 &&
								Math.abs(B.M().e12() - B0.M().e12()) < EPSILON_3 &&
								Math.abs(B.M().e20() - B0.M().e20()) < EPSILON_3 &&
								Math.abs(B.M().e21() - B0.M().e21()) < EPSILON_3 &&
								Math.abs(B.M().e22() - B0.M().e22()) < EPSILON_3
								) {
							multiplicity++;
						}*/
					}
				}
			}
		}
	}

	public Optional<Integer> getClosestSigmaValue() {
		if (lowestSigma != null) {
			return Optional.of(lowestSigma.getSigma());
		}
		return Optional.empty();
	}

	public double getDistanceToTwistAm() {
		return Math.sqrt(distanceToTwistAm);
	}

	public double getDistanceToTiltAm() {
		return Math.sqrt(distanceToTiltAm);
	}

	public double getDistanceToSymmetricAm() {
		return Math.sqrt(distanceToSymmetricAm);
	}

	public double getDistanceTo180tiltAm() {
		return Math.sqrt(distanceTo180tiltAm);
	}

	private double getMinimumValue(MultivariateFunction function, double... initialGuess) {
		SimplexOptimizer optimizer = new SimplexOptimizer(EPSILON_4, EPSILON_4);
		PointValuePair minimum = optimizer.optimize(NO_LIMIT,
				new ObjectiveFunction(function),
				MINIMIZE,
				new InitialGuess(initialGuess),
				new NelderMeadSimplex(initialGuess.length));
		return minimum.getValue();
	}

	private boolean isApproximateDistanceNeeded() {
		return parametersToCompute.get(TILT_APRX)
				|| parametersToCompute.get(TWIST_APRX)
				|| parametersToCompute.get(SYMMETRIC_APRX)
				|| parametersToCompute.get(TILT_180_APRX);
	}

	private boolean isComponentNeeded() {
		return parametersToCompute.get(TILT_COMP) || parametersToCompute.get(TWIST_COMP);
	}
}
