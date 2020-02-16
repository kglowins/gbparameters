package io.github.kglowins.gbparameters.gbcd;

import io.github.kglowins.gbparameters.enums.PointGroup;
import io.github.kglowins.gbparameters.representation.AxisAngle;
import io.github.kglowins.gbparameters.representation.Matrix3x3;
import io.github.kglowins.gbparameters.representation.UnitVector;
import io.github.kglowins.gbparameters.utils.Transformations;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static io.github.kglowins.gbparameters.gbcd.ZonePointsUtil.getZonePoints;
import static java.lang.Math.PI;

public class GbcdSymmetriesFinder {

    private static final double O_01_DEG = 0.00017;

    public GbcdSymmetries find(Matrix3x3 M, PointGroup pointGroup) {
        List<SymmetryAxis> symmetryAxes = new ArrayList<>();
        List<List<Point2D>> mirrorLines = new ArrayList<>();

        List<Matrix3x3> groupOperators = getGroupOperators(M, pointGroup);
        completeGroup(groupOperators);

        for (Matrix3x3 g : groupOperators) {
            AxisAngle axisAngle = new AxisAngle();
            axisAngle.set(g);
            if (Math.abs(axisAngle.angle()) > O_01_DEG) {
                int multiplicity = (int) Math.round(2 * PI / axisAngle.angle());

                UnitVector negativeAxis = new UnitVector();
                negativeAxis.set(axisAngle.axis());
                negativeAxis.negate();

                if (axisAngle.axis().z() > -O_01_DEG) {
                    symmetryAxes.add(new SymmetryAxis(axisAngle.axis(), multiplicity));
                    if (multiplicity == 2) {
                        mirrorLines.add(getZonePoints(axisAngle.axis()));
                    }
                }

                if (negativeAxis.z() > -O_01_DEG) {
                    symmetryAxes.add(new SymmetryAxis(negativeAxis, multiplicity));
                    if (multiplicity == 2) {
                        mirrorLines.add(getZonePoints(negativeAxis));
                    }
                }
            }
        }
        return GbcdSymmetries.of(symmetryAxes, mirrorLines);
    }

    private void completeGroup(List<Matrix3x3> groupOperators) {
        boolean hasBeenAdded = true;
        while (hasBeenAdded) {
            hasBeenAdded = false;
            List<Matrix3x3> groupCp = new ArrayList<>(groupOperators);

            for (Matrix3x3 g1 : groupOperators) {
                for (Matrix3x3 g2 : groupOperators) {
                    Matrix3x3 product1 = new Matrix3x3(g1);
                    Matrix3x3 product2 = new Matrix3x3(g1);
                    product1.times(g2);
                    product2.leftMul(g2);
                    if (!listContains(groupCp, product1)) {
                        groupCp.add(product1);
                        hasBeenAdded = true;
                    }
                    if (!listContains(groupCp, product2)) {
                        groupCp.add(product2);
                        hasBeenAdded = true;
                    }
                }
            }
            groupOperators = new ArrayList<>(groupCp);
        }
    }

    private List<Matrix3x3> getGroupOperators(Matrix3x3 M, PointGroup pointGroup) {
        Matrix3x3[] symmetryOperations = Transformations.getSymmetryTransformations(pointGroup);
        List<Matrix3x3> groupOperators = new ArrayList<>();

        for (Matrix3x3 C1 : symmetryOperations) {
            for (Matrix3x3 C2 : symmetryOperations) {

                Matrix3x3 Mcp = new Matrix3x3(M);
                Mcp.leftMul(C1);
                Mcp.timesTransposed(C2);

                if (Mcp.isEqualTo(M) && !listContains(groupOperators, C1)) {
                    groupOperators.add(C1);
                }

                Matrix3x3 MTcp = new Matrix3x3(M);
                MTcp.transpose();
                MTcp.leftMul(C1);
                MTcp.timesTransposed(C2);

                if (MTcp.isEqualTo(M)) {
                    Matrix3x3 MC = new Matrix3x3(M);
                    MC.times(C2);
                    if (!listContains(groupOperators, MC)) {
                        groupOperators.add(MC);
                    }
                }
            }
        }
        return groupOperators;
    }

    private static boolean listContains(List<Matrix3x3> l, Matrix3x3 m) {
        for (Matrix3x3 r : l) {
            if (r.isEqualTo(m)) return true;
        }
        return false;
    }
}
