package io.github.kglowins.gbparameters.utils;


import io.github.kglowins.gbparameters.distances.AMDistanceToTilt180;
import io.github.kglowins.gbparameters.distances.AMDistanceToSymmetricNegativeAxis;
import io.github.kglowins.gbparameters.distances.AMDistanceToSymmetricPositiveAxis;
import io.github.kglowins.gbparameters.distances.AMDistanceToTilt;
import io.github.kglowins.gbparameters.distances.AMDistanceToTwistNegativeAxis;
import io.github.kglowins.gbparameters.distances.AMDistanceToTwistPositiveAxis;
import io.github.kglowins.gbparameters.representation.AxisAngle;
import io.github.kglowins.gbparameters.representation.CSLMisorientation;
import io.github.kglowins.gbparameters.representation.InterfaceMatrix;
import io.github.kglowins.gbparameters.representation.Matrix3x3;
import io.github.kglowins.gbparameters.representation.UnitVector;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.util.FastMath;

import static java.lang.Math.PI;
import static java.util.Objects.nonNull;
import static org.apache.commons.math3.util.FastMath.atan;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.tan;


public final class BoundaryCharacterizer {
	
	private static final double EPSILON = 1e-4d;

	// input
    private InterfaceMatrix Binit;

    private boolean BT;
    private boolean Bminus;
	private Matrix3x3[] symmetryTransformations;
	
	private boolean computeAMTiltDist;
	private boolean computeAMTwistDist;
	private boolean computeAMSymmetricDist;
	private boolean computeAMTilt180Dist;
	
	private boolean decompose;

	private boolean checkIfCSL;
    private double p;
    private double omega0;
    private CSLMisorientation[] cslMisorientations;

	private boolean computeAprxDistances;

    private boolean saveDetails;

    // output
    private CSLMisorientation lowestSigma;
		
	private double nearestTwistDist;
	private InterfaceMatrix equivalentForTwist;
	private InterfaceMatrix nearestTwist;
	private Matrix3x3 C1twist;
	private Matrix3x3 C2twist;
    private boolean twistT;
    private boolean twistMinus;

    private double nearestTiltDist;
	private InterfaceMatrix equivalentForTilt;
	private InterfaceMatrix nearestTilt;
	private Matrix3x3 C1tilt;
	private Matrix3x3 C2tilt;
    private boolean tiltT;
    private boolean tiltMinus;

    private double nearestSymDist;
	private InterfaceMatrix equivalentForSymmetric;
	private InterfaceMatrix nearestSymmetric;
	private Matrix3x3 C1symmetric;
	private Matrix3x3 C2symmetric;
    private boolean symmetricT;
    private boolean symmetricMinus;

    private double nearestTilt180Dist;
	private InterfaceMatrix equivalentForTilt180;
	private InterfaceMatrix nearestTilt180;
	private Matrix3x3 C1tilt180;
	private Matrix3x3 C2twist180;
    private boolean tilt180T;
    private boolean tilt180Minus;

	private double minimumTwistAngle;
	private double minimumTiltAngle;

	private double aprxTwistDist;
	private double aprxTiltDist;
	private double aprxSymmetricDist;
	private double aprxTilt180Dist;

	private int multiplicity;

	
	public BoundaryCharacterizer() {
				
		computeAMTiltDist = false;
		computeAMTwistDist = false;
		computeAMSymmetricDist = false;
		computeAMTilt180Dist = false;
		
		saveDetails = false;
		
		resetAMDistances();
		resetComponents();
				
		equivalentForTwist = null;
		nearestTwist = null;
		C1twist = null;
		C2twist = null;
		
		equivalentForTilt = null;
		nearestTilt = null;
		C1tilt = null;
		C2tilt = null;
		
		equivalentForSymmetric = null;
		nearestSymmetric = null;
		C1symmetric = null;
		C2symmetric = null;
		
		equivalentForTilt180 = null;
		nearestTilt180 = null;
		C1tilt180 = null;
		C2twist180 = null;
		

		checkIfCSL = false;
		p = 0.5d;
		omega0 = Math.toRadians(15d);
		lowestSigma = null;
		
		symmetryTransformations = null;
		cslMisorientations = null;
		
		Binit = null;
		
		BT = true;
		Bminus = true;

		computeAprxDistances = false;
	
		multiplicity = 0;
	}

	public void includeTransposition(boolean BT) {
	    this.BT = BT;
    }

    public void includeInversion(boolean Bminus) {
        this.Bminus = Bminus;
    }
		
	private void resetAMDistances() {
		if (computeAMTwistDist) {
		    nearestTwistDist = Double.MAX_VALUE;
        }
		if (computeAMTiltDist) {
		    nearestTiltDist = Double.MAX_VALUE;
        }
		if (computeAMSymmetricDist) {
		    nearestSymDist = Double.MAX_VALUE;
        }
		if (computeAMTilt180Dist) {
		    nearestTilt180Dist = Double.MAX_VALUE;
        }
	}
	
	private void resetComponents() {
		if (decompose) {
			minimumTwistAngle = Double.MAX_VALUE;
			minimumTiltAngle = Double.MAX_VALUE;
		}
	}
	
	private void resetAprxDistances() {
		if (computeAprxDistances) {
			aprxTiltDist = Double.MAX_VALUE;
			aprxTwistDist = Double.MAX_VALUE;
			aprxSymmetricDist = Double.MAX_VALUE;
			aprxTilt180Dist = Double.MAX_VALUE;
		}
	}
	
	public void computeAMDistances(boolean toTilt, boolean toTwist, boolean toSymmetric, boolean toTilt180) {
		computeAMTiltDist = toTilt;
		computeAMTwistDist = toTwist;
		computeAMSymmetricDist = toSymmetric;
		computeAMTilt180Dist = toTilt180;
	}
	
	public void decompose(boolean b) {
		decompose = b;
	}

	public void computeAprxDistances(boolean b) {
		computeAprxDistances = b;
	}

	public void saveMinimizationDetails(boolean b) {
		saveDetails = b;
	}

	public void checkIfCSL(double p, double omega0, CSLMisorientation[] cslMisorientations) {
		checkIfCSL = true;
		this.p = p;
		this.omega0 = omega0;
		this.cslMisorientations = cslMisorientations;
	}

	public void setSymmetry(Matrix3x3[] symmetryTransformations) {
		this.symmetryTransformations = symmetryTransformations;
	}

	public void characterize(InterfaceMatrix Binit) {

		this.Binit = Binit;
		multiplicity = 0;
		resetAMDistances();
		resetComponents();
		resetAprxDistances();
		if (checkIfCSL) {
		    lowestSigma = null;
        }
		
		boolean[] transposeTF = BT ? new boolean[]{false, true} : new boolean[]{false};
		boolean[] minusTF = Bminus ? new boolean[]{false, true} : new boolean[]{false};

		for( boolean transpose : transposeTF) {
		    for(boolean minus : minusTF) {
                for (Matrix3x3 C1 : symmetryTransformations) {
                    for (Matrix3x3 C2 : symmetryTransformations) {


                        InterfaceMatrix B = new InterfaceMatrix(Binit);

                        if (transpose) B.transpose();
                        if (minus) B.toMinus();

                        B.applySymmetry1(C1);
                        B.applySymmetry2(C2);

                        AxisAngle aa = new AxisAngle();
                        aa.set(B.M());

                        if (computeAMTiltDist) {

                            SimplexOptimizer optimizer = new SimplexOptimizer(EPSILON, EPSILON);
                            AMDistanceToTilt dist = new AMDistanceToTilt(B);

                            PointValuePair minimum = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
                                new ObjectiveFunction(dist),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[]{aa.axis().zenith(), aa.axis().azimuth(), aa.angle(), B.m1().azimuth()}),
                                new NelderMeadSimplex(4));

                            double distVal = minimum.getValue();
                            double[] resPt = minimum.getPoint();

                            if (distVal < nearestTiltDist) {

                                nearestTiltDist = distVal;

                                if (saveDetails) {

                                    UnitVector resN = new UnitVector();
                                    resN.set(resPt[0], resPt[1]);

                                    AxisAngle resAa = new AxisAngle();
                                    resAa.set(resN, resPt[2]);

                                    Matrix3x3 resM = new Matrix3x3();
                                    resM.set(resAa);

                                    UnitVector resM1 = new UnitVector();
                                    resM1.set(atan(
                                        -1d / (
                                            tan(resPt[0]) * (cos(resPt[1]) * cos(resPt[3]) + sin(resPt[1]) * sin(resPt[3]))
                                        )
                                    ), resPt[3]);

                                    C1tilt = C1;
                                    C2tilt = C2;
                                    equivalentForTilt = B;
                                    nearestTilt = new InterfaceMatrix(resM, resM1);
                                    tiltT = transpose;
                                    tiltMinus = minus;
                                }
                            }
                        }


                        if (computeAMTwistDist) {
                            SimplexOptimizer optimizer = new SimplexOptimizer(EPSILON, EPSILON);

                            AMDistanceToTwistPositiveAxis distPos = new AMDistanceToTwistPositiveAxis(B);
                            AMDistanceToTwistNegativeAxis distNeg = new AMDistanceToTwistNegativeAxis(B);

                            PointValuePair minimumPos = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
                                new ObjectiveFunction(distPos),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[]{aa.axis().zenith(), aa.axis().azimuth(), aa.angle()}),
                                new NelderMeadSimplex(3));

                            PointValuePair minimumNeg = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
                                new ObjectiveFunction(distNeg),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[]{aa.axis().zenith(), aa.axis().azimuth(), aa.angle()}),
                                new NelderMeadSimplex(3));

                            double distValPos = minimumPos.getValue();
                            double distValNeg = minimumNeg.getValue();

                            double[] resultPos = minimumPos.getPoint();
                            double[] resultNeg = minimumNeg.getPoint();

                            if (distValPos < nearestTwistDist) {
                                nearestTwistDist = distValPos;
                                if (saveDetails) {

                                    UnitVector resN = new UnitVector();
                                    resN.set(resultPos[0], resultPos[1]);

                                    AxisAngle resAa = new AxisAngle();
                                    resAa.set(resN, resultPos[2]);

                                    Matrix3x3 resM = new Matrix3x3();
                                    resM.set(resAa);

                                    equivalentForTwist = B;
                                    nearestTwist = new InterfaceMatrix(resM, resN);
                                    C1twist = C1;
                                    C2twist = C2;
                                    twistT = transpose;
                                    twistMinus = minus;
                                }
                            }

                            if (distValNeg < nearestTwistDist) {
                                nearestTwistDist = distValNeg;
                                if (saveDetails) {

                                    UnitVector resN = new UnitVector();
                                    resN.set(resultNeg[0], resultNeg[1]);

                                    AxisAngle resAa = new AxisAngle();
                                    resAa.set(resN, resultNeg[2]);

                                    Matrix3x3 resM = new Matrix3x3();
                                    resM.set(resAa);

                                    resN.negate();

                                    equivalentForTwist = B;
                                    nearestTwist = new InterfaceMatrix(resM, resN);
                                    C1twist = C1;
                                    C2twist = C2;
                                    twistT = transpose;
                                    twistMinus = minus;
                                }
                            }
                        }


                        if (computeAMSymmetricDist) {

                            SimplexOptimizer optimizer = new SimplexOptimizer(EPSILON, EPSILON);

                            AMDistanceToSymmetricPositiveAxis distPos = new AMDistanceToSymmetricPositiveAxis(B);
                            AMDistanceToSymmetricNegativeAxis distNeg = new AMDistanceToSymmetricNegativeAxis(B);

                            PointValuePair minimumPos = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
                                new ObjectiveFunction(distPos),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[]{aa.axis().zenith(), aa.axis().azimuth()}),
                                new NelderMeadSimplex(2));

                            PointValuePair minimumNeg = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
                                new ObjectiveFunction(distNeg),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[]{aa.axis().zenith(), aa.axis().azimuth()}),
                                new NelderMeadSimplex(2));

                            double distValPos = minimumPos.getValue();
                            double distValNeg = minimumNeg.getValue();

                            double[] resultPos = minimumPos.getPoint();
                            double[] resultNeg = minimumNeg.getPoint();

                            if (distValPos < nearestSymDist) {

                                nearestSymDist = distValPos;

                                if (saveDetails) {

                                    UnitVector resN = new UnitVector();
                                    resN.set(resultPos[0], resultPos[1]);

                                    AxisAngle resAa = new AxisAngle();
                                    resAa.set(resN, PI);

                                    Matrix3x3 resM = new Matrix3x3();
                                    resM.set(resAa);

                                    equivalentForSymmetric = B;
                                    nearestSymmetric = new InterfaceMatrix(resM, resN);
                                    C1symmetric = C1;
                                    C2symmetric = C2;
                                    symmetricT = transpose;
                                    symmetricMinus = minus;
                                }
                            }

                            if (distValNeg < nearestSymDist) {

                                nearestSymDist = distValNeg;

                                if (saveDetails) {

                                    UnitVector resN = new UnitVector();
                                    resN.set(resultNeg[0], resultNeg[1]);

                                    AxisAngle resAa = new AxisAngle();
                                    resAa.set(resN, PI);

                                    Matrix3x3 resM = new Matrix3x3();
                                    resM.set(resAa);

                                    resN.negate();

                                    equivalentForSymmetric = B;
                                    nearestSymmetric = new InterfaceMatrix(resM, resN);
                                    C1symmetric = C1;
                                    C2symmetric = C2;
                                    symmetricT = transpose;
                                    symmetricMinus = minus;
                                }
                            }
                        }


                        if (computeAMTilt180Dist) {

                            SimplexOptimizer optimizer = new SimplexOptimizer(EPSILON, EPSILON);
                            AMDistanceToTilt180 dist = new AMDistanceToTilt180(B);

                            PointValuePair minimum = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
                                new ObjectiveFunction(dist),
                                GoalType.MINIMIZE,
                                new InitialGuess(new double[]{aa.axis().zenith(), aa.axis().azimuth(), B.m1().azimuth()}),
                                new NelderMeadSimplex(3));

                            double distVal = minimum.getValue();
                            double[] resPt = minimum.getPoint();

                            if (distVal < nearestTilt180Dist) {

                                nearestTilt180Dist = distVal;

                                if (saveDetails) {

                                    UnitVector resN = new UnitVector();
                                    resN.set(resPt[0], resPt[1]);

                                    AxisAngle resAa = new AxisAngle();
                                    resAa.set(resN, PI);

                                    Matrix3x3 resM = new Matrix3x3();
                                    resM.set(resAa);

                                    UnitVector resM1 = new UnitVector();
                                    resM1.set(atan(-1d / (tan(resPt[0]) * (cos(resPt[1]) * cos(resPt[2]) + sin(resPt[1]) * sin(resPt[2])))), resPt[2]);

                                    equivalentForTilt180 = B;
                                    nearestTilt180 = new InterfaceMatrix(resM, resM1);
                                    C1tilt180 = C1;
                                    C2twist180 = C2;
                                    tilt180T = transpose;
                                    tilt180Minus = minus;
                                }
                            }
                        }


                        if (computeAprxDistances) {
                            double alpha = SaferMath.acos(Math.abs(aa.axis().dot(B.m1())));
                            double ninetyMinAlpha = Math.toRadians(90d) - alpha;

                            double deltaOmega = PI - aa.angle();

                            deltaOmega = deltaOmega * deltaOmega;

                            double alphaScandSq = alpha * alpha + deltaOmega;
                            double alphaIcandSq = ninetyMinAlpha * ninetyMinAlpha + deltaOmega;

                            if (ninetyMinAlpha < aprxTiltDist) aprxTiltDist = ninetyMinAlpha;

                            if (alpha < aprxTwistDist) aprxTwistDist = alpha;

                            if (alphaScandSq < aprxSymmetricDist) aprxSymmetricDist = alphaScandSq;
                            if (alphaIcandSq < aprxTilt180Dist) aprxTilt180Dist = alphaIcandSq;
                        }


                        if (decompose) {
                            double dot = aa.axis().dot(B.m1());
                            double Phi;
                            double omega;

                            if (Math.abs(dot) < 1e-3d) {
                                Phi = 0d;
                                omega = aa.angle();
                            } else {
                                double alpha = SaferMath.acos(aa.axis().dot(B.m1()));
                                double tg = tan(alpha);
                                double cos = cos(0.5d * aa.angle());

                                Phi = FastMath.asin(sin(0.5d * aa.angle()) / Math.sqrt(1d + tg * tg * cos * cos));
                                omega = FastMath.asin(sin(alpha) * sin(0.5d * aa.angle()));

                                Phi = 2d * Phi;
                                omega = 2d * omega;
                            }

                            if (Phi < minimumTwistAngle) {
                                minimumTwistAngle = Phi;
                            }

                            if (omega < minimumTiltAngle) {
                                minimumTiltAngle = omega;
                            }
                        }

                        if (checkIfCSL) {

                            for (CSLMisorientation csl : cslMisorientations) {

                                Matrix3x3 M = new Matrix3x3(B.M());
                                M.timesTransposed(csl.getM());

                                double angle = SaferMath.acos(0.5 * (M.tr() - 1));

                                if (angle < omega0 / pow(csl.getSigma(), p)) {

                                    lowestSigma = csl;
                                    break;
                                }
                            }
                        }


                        if (Math.abs(B.m1().x() - Binit.m1().x()) < 1e-3d
                            && Math.abs(B.m1().y() - Binit.m1().y()) < 1e-3d
                            && Math.abs(B.m1().z() - Binit.m1().z()) < 1e-3d
                            && Math.abs(B.M().e00() - Binit.M().e00()) < 1e-3d &&
                            Math.abs(B.M().e01() - Binit.M().e01()) < 1e-3d &&
                            Math.abs(B.M().e02() - Binit.M().e02()) < 1e-3d &&

                            Math.abs(B.M().e10() - Binit.M().e10()) < 1e-3d &&
                            Math.abs(B.M().e11() - Binit.M().e11()) < 1e-3d &&
                            Math.abs(B.M().e12() - Binit.M().e12()) < 1e-3d &&

                            Math.abs(B.M().e20() - Binit.M().e20()) < 1e-3d &&
                            Math.abs(B.M().e21() - Binit.M().e21()) < 1e-3d &&
                            Math.abs(B.M().e22() - Binit.M().e22()) < 1e-3d
                        ) {


                            multiplicity++;
                        }
                    }
                }
            }
        }
	}

	public int getMinimumSigma() {
		return nonNull(lowestSigma) ? lowestSigma.getSigma() : 0;
	}

	public double getNearestTwistDist() {
		return Math.sqrt(nearestTwistDist);
	}

	public InterfaceMatrix getEquivalentForTwist() {
		return equivalentForTwist;
	}

	public InterfaceMatrix getNearestTwist() {
		return nearestTwist;
	}

	public Matrix3x3 getC1twist() {
		return C1twist;
	}

	public Matrix3x3 getC2twist() {
		return C2twist;
	}

	public double getNearestTiltDist() {
		return Math.sqrt(nearestTiltDist);
	}

	public InterfaceMatrix getEquivalentForTilt() {
		return equivalentForTilt;
	}

	public InterfaceMatrix getNearestTilt() {
		return nearestTilt;
	}

	public Matrix3x3 getC1tilt() {
		return C1tilt;
	}

	public Matrix3x3 getC2tilt() {
		return C2tilt;
	}

	public double getNearestSymDist() {
		return Math.sqrt(nearestSymDist);
	}

	public InterfaceMatrix getEquivalentForSymmetric() {
		return equivalentForSymmetric;
	}

	public InterfaceMatrix getNearestSymmetric() {
		return nearestSymmetric;
	}

	public Matrix3x3 getC1symmetric() {
		return C1symmetric;
	}

	public Matrix3x3 getC2symmetric() {
		return C2symmetric;
	}

	public double getNearestTilt180Dist() {
		return Math.sqrt(nearestTilt180Dist);
	}

	public InterfaceMatrix getEquivalentForTilt180() {
		return equivalentForTilt180;
	}

	public InterfaceMatrix getNearestTilt180() {
		return nearestTilt180;
	}

	public Matrix3x3 getC1tilt180() {
		return C1tilt180;
	}

	public Matrix3x3 getC2twist180() {
		return C2twist180;
	}

	public double getMinimumTiltAngle() {
		return minimumTiltAngle;
	}
	
	public double getMinimumTwistAngle() {
		return minimumTwistAngle;
	}

	public double getAprxTwistDist() {
		return aprxTwistDist;
	}
	
	public double getAprxTiltDist() {
		return aprxTiltDist;
	}
	
	public double getAprxSymmetricDist() {
		return Math.sqrt(aprxSymmetricDist);
	}
	
	public double getAprxTilt180Dist() {
		return Math.sqrt(aprxTilt180Dist);
	}

	public boolean isComputeAMTiltDist() {
		return computeAMTiltDist;
	}

	public boolean isComputeAMTwistDist() {
		return computeAMTwistDist;
	}

	public boolean isComputeAMSymmetricDist() {
		return computeAMSymmetricDist;
	}

	public boolean isComputeAMTilt180Dist() {
		return computeAMTilt180Dist;
	}

    public boolean isTwistT() {
        return twistT;
    }

    public boolean isTwistMinus() {
        return twistMinus;
    }

    public boolean isTiltT() {
        return tiltT;
    }

    public boolean isTiltMinus() {
        return tiltMinus;
    }

    public boolean isSymmetricT() {
        return symmetricT;
    }

    public boolean isSymmetricMinus() {
        return symmetricMinus;
    }

    public boolean isTilt180T() {
        return tilt180T;
    }

    public boolean isTilt180Minus() {
        return tilt180Minus;
    }

    public boolean isDecompose() {
		return decompose;
	}

	public boolean isCheckIfCSL() {
		return checkIfCSL;
	}

	public InterfaceMatrix getBinit() {
		return Binit;
	}

	public int getMultiplicity() {
		return multiplicity;
	}
}
