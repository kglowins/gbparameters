package io.github.kglowins.gbparams.gbcd;

import io.github.kglowins.gbparams.core.SymmetryOperations;
import io.github.kglowins.gbparams.representation.AxisAngle;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import io.github.kglowins.gbparams.enums.PointGroup;
import io.github.kglowins.gbparams.representation.UnitVector;
import io.github.kglowins.gbparams.utils.SaferMath;
import java.awt.geom.Point2D;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import static io.github.kglowins.gbparams.gbcd.ZonePointsUtil.getZonePoints;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.util.stream.Collectors.toList;

public class GbcdCharacteristicGbFinder {

    private static final double O_01_DEG = 0.00017;

    public GbcdCharacteristicGbs find(Matrix3x3 fixedM, PointGroup pointGroup) {

        List<Matrix3x3> symmetryOperations = SymmetryOperations.getSymmetryOperations(pointGroup);

        List<GbcdGbLocation> uniqueTwistGbLocations = new ArrayList<>();
        List<GbcdGbLocation> uniqueSymmetricGbLocations = new ArrayList<>();

        for (Matrix3x3 C1 : symmetryOperations) {
            for (Matrix3x3 C2 : symmetryOperations) {

                Matrix3x3 M = new Matrix3x3(fixedM);
                M.leftMul(C1);
                M.timesTransposed(C2);

                AxisAngle misorAxisAngle = new AxisAngle();
                misorAxisAngle.set(M);

                UnitVector misorAxis = new UnitVector(misorAxisAngle.axis());
                misorAxis.transposedTransform(C1);
                UnitVector minusMisorAxis = new UnitVector(misorAxis);
                minusMisorAxis.negate();

                AbstractMap.SimpleImmutableEntry<List<GbcdGbLocation>, List<GbcdGbLocation>> locations
                        = findLocations(misorAxis, misorAxisAngle.angle());

                AbstractMap.SimpleImmutableEntry<List<GbcdGbLocation>, List<GbcdGbLocation>> minusLocations
                        = findLocations(minusMisorAxis, misorAxisAngle.angle());

                appendUniqueIfNotPresent(uniqueTwistGbLocations, locations.getKey());
                appendUniqueIfNotPresent(uniqueTwistGbLocations, minusLocations.getKey());
                appendUniqueIfNotPresent(uniqueSymmetricGbLocations, locations.getValue());
                appendUniqueIfNotPresent(uniqueSymmetricGbLocations, minusLocations.getValue());
            }
        }

        List<GbcdGbLocation> uniqueTiltGbsZones = uniqueTwistGbLocations.stream()
                .map(this::transformTwistToTilt)
                .collect(toList());

        List<GbcdGbLocation> uniqueTilt180GbsZones = uniqueSymmetricGbLocations.stream()
                .map(this::transformTwistToTilt)
                .collect(toList());

        return GbcdCharacteristicGbs.builder()
                .twist(uniqueTwistGbLocations)
                .symmetric(uniqueSymmetricGbLocations)
                .tilt(uniqueTiltGbsZones)
                .tilt180(uniqueTilt180GbsZones)
                .build();
    }

    private static void appendUniqueIfNotPresent(List<GbcdGbLocation> uniqueLocations, List<GbcdGbLocation> locations) {
        for (GbcdGbLocation location : locations) {
            boolean isIncluded = false;
            for (GbcdGbLocation uniqueLocation : uniqueLocations) {
                if (abs(location.coords().getX() - uniqueLocation.coords().getX()) < O_01_DEG &&
                    abs(location.coords().getY() - uniqueLocation.coords().getY()) < O_01_DEG) {
                    isIncluded = true;
                    break;
                }
            }
            if (!isIncluded) {
                uniqueLocations.add(location);
            }
        }
    }

    private GbcdGbLocation transformTwistToTilt(GbcdGbLocation twistLocation) {
        return GbcdGbLocation.builder()
                .axis(twistLocation.axis())
                .zone(getZonePoints(twistLocation.axis()))
                //.miller
                .build();
    }

    private AbstractMap.SimpleImmutableEntry<List<GbcdGbLocation>, List<GbcdGbLocation>> findLocations(UnitVector misorAxis, double misorAngle) {
        List<GbcdGbLocation> twistGbLocations = new ArrayList<>();
        List<GbcdGbLocation> symmetricGbLocations = new ArrayList<>();
        double theta = FastMath.atan2(misorAxis.y(), misorAxis.x());
        double phi = SaferMath.acos(misorAxis.z());
        double r = FastMath.tan(0.5 * phi);

        if (isBelowOrEqualOne(r)) {

      /*  final MillerIndices indices = new MillerIndices();
        final MillerIndices indices2 = new MillerIndices();

        switch(ptGrp) {
            case M3M:
                indices.setAsCubic(misorAxis, maxIndex);
                indices2.setAsCubic(m2, maxIndex);
                break;

            case _6MMM:
                indices.setAsNonCubicPlane(misorAxis, maxIndex, Transformations.getHexToCartesian(a, c));
                indices2.setAsNonCubicPlane(m2, maxIndex, Transformations.getHexToCartesian(a, c));
                break;

            case _4MMM:
                indices.setAsNonCubicPlane(misorAxis, maxIndex, Transformations.getTetrToCartesian(a, c));
                indices2.setAsNonCubicPlane(m2, maxIndex, Transformations.getTetrToCartesian(a, c));
                break;

            case MMM:
                indices.setAsNonCubicPlane(misorAxis, maxIndex, Transformations.getOrthToCartesian(a, b, c));
                indices2.setAsNonCubicPlane(m2, maxIndex, Transformations.getOrthToCartesian(a, b, c));
                break;

            default: break;
        }*/

            double rX =  r * FastMath.cos(theta);
            double rY =  r * FastMath.sin(theta);
            GbcdGbLocation location = GbcdGbLocation.builder()
                    .axis(misorAxis)
                    .coords(new Point2D.Double(rX, rY))
                    //.miller1 & 2
                    .build();
            twistGbLocations.add(location);
            if (isPi(misorAngle)) {
                symmetricGbLocations.add(location);
            }
        }
        return new SimpleImmutableEntry<>(twistGbLocations, symmetricGbLocations);
    }

    private static boolean isPi(double angle) {
        return abs(PI - angle) < O_01_DEG;
    }

    private static boolean isBelowOrEqualOne(double r) {
        return r < 1. + O_01_DEG;
    }
}
