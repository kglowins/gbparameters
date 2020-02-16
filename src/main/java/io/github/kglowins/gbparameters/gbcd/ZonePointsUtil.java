package io.github.kglowins.gbparameters.gbcd;

import io.github.kglowins.gbparameters.representation.UnitVector;
import io.github.kglowins.gbparameters.utils.SaferMath;
import org.apache.commons.math3.util.FastMath;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;

public class ZonePointsUtil {

    private static final int NUMBER_OF_ZONE_POINTS = 1024;
    private static final double TWO_PI = 2. * PI;
    private static final double EPSILON = 1e-4;

    public static List<Point2D> getZonePoints(UnitVector axis) {
        List<Point2D> zonePoints = new ArrayList<>();
        double t0 = FastMath.atan2(axis.y(), axis.x());
        double[] t = new double[NUMBER_OF_ZONE_POINTS + 1];
        double dt = TWO_PI / NUMBER_OF_ZONE_POINTS;
        for (int i = 0; i <= NUMBER_OF_ZONE_POINTS; ++i) {
            t[i] = t0 + i * dt;
        }

        double xSq = axis.x() * axis.x();
        double ySq = axis.y() * axis.y();
        double xSqPlusYSq = xSq + ySq;

        if (Math.abs(xSqPlusYSq) > EPSILON) {

            double zSq = axis.z() * axis.z();
            double oneMinusZ = 1. - axis.z();
            double ksi = oneMinusZ / xSqPlusYSq;
            double oneMinZSq = 1. - zSq;
            double lambda =  SaferMath.sqrt(oneMinZSq) / Math.sqrt(xSqPlusYSq);

            double xy = axis.x() * axis.y();

            double o11 = axis.z() + ySq * ksi;
            double o12 = -xy * ksi;
            double o21 = o12;
            double o22 = axis.z() + xSq * ksi;
            double o31 = -axis.x() * lambda;
            double o32 = -axis.y() * lambda;

            for (int i = 0; i <= NUMBER_OF_ZONE_POINTS; ++i) {

                double cosT = FastMath.cos(t[i]);
                double sinT = FastMath.sin(t[i]);

                double vX = o11 * cosT + o12 * sinT;
                double vY = o21 * cosT + o22 * sinT;
                double vZ = o31 * cosT + o32 * sinT;

                double theta = FastMath.atan2(vY, vX);
                double phi = FastMath.acos(vZ);
                double r = FastMath.tan(0.5 * phi);

                if (isBelowOrEqualOne(r)) {
                    zonePoints.add(new Point2D.Double(r * FastMath.cos(theta),  r * FastMath.sin(theta)));
                }
            }
        }
        else {
            for (int i = 0; i <= NUMBER_OF_ZONE_POINTS; ++i) {
                zonePoints.add(new Point2D.Double(FastMath.cos(t[i]),  FastMath.sin(t[i])));
            }
        }
        return zonePoints;
    }

    private static boolean isBelowOrEqualOne(double r) {
        return r < 1. + EPSILON;
    }
}
