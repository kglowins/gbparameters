package io.github.kglowins.gbparams.core;

import io.github.kglowins.gbparams.enums.PointGroup;
import io.github.kglowins.gbparams.representation.Matrix3x3;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SymmetryOperations {

  private static final Map<PointGroup, List<Matrix3x3>> POINT_GROUPS_TO_SYMMETRY_OPERATIONS = Map.of(
      PointGroup.M3M, symmetryTransformationsOh(),
      PointGroup._6MMM, symmetryTransformationsD6h(),
      PointGroup._4MMM, symmetryTransformationsD4h(),
      PointGroup.MMM, symmetryTransformationsD3d(),
      PointGroup._3M, symmetryTransformationsD2h(),
      PointGroup._2M, symmetryTransformationsC2h(),
      PointGroup._1, Collections.singletonList(new Matrix3x3())
  );

  private SymmetryOperations() {
  }

  public static final List<Matrix3x3> getSymmetryOperations(PointGroup pointGroup) {
    return POINT_GROUPS_TO_SYMMETRY_OPERATIONS.get(pointGroup);
  }

  private static List<Matrix3x3> symmetryTransformationsOh() {
    return asList(
        new Matrix3x3(1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(0d, 0d, 1d,
            1d, 0d, 0d,
            0d, 1d, 0d),
        new Matrix3x3(0d, 1d, 0d,
            0d, 0d, -1d,
            -1d, 0d, 0d),
        new Matrix3x3(0d, -1d, 0d,
            0d, 0d, -1d,
            1d, 0d, 0d),
        new Matrix3x3(0d, -1d, 0d,
            0d, 0d, 1d,
            -1d, 0d, 0d),
        new Matrix3x3(1d, 0d, 0d,
            0d, 0d, -1d,
            0d, 1d, 0d),
        new Matrix3x3(0d, 0d, 1d,
            0d, 1d, 0d,
            -1d, 0d, 0d),
        new Matrix3x3(0d, -1d, 0d,
            1d, 0d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(1d, 0d, 0d,
            0d, 0d, 1d,
            0d, -1d, 0d),
        new Matrix3x3(0d, 0d, -1d,
            0d, 1d, 0d,
            1d, 0d, 0d),
        new Matrix3x3(0d, 1d, 0d,
            -1d, 0d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(0d, 1d, 0d,
            0d, 0d, 1d,
            1d, 0d, 0d),
        new Matrix3x3(0d, 0d, -1d,
            1d, 0d, 0d,
            0d, -1d, 0d),
        new Matrix3x3(0d, 0d, 1d,
            -1d, 0d, 0d,
            0d, -1d, 0d),
        new Matrix3x3(0d, 0d, -1d,
            -1d, 0d, 0d,
            0d, 1d, 0d),
        new Matrix3x3(0d, 1d, 0d,
            1d, 0d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(0d, 0d, 1d,
            0d, -1d, 0d,
            1d, 0d, 0d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 0d, 1d,
            0d, 1d, 0d),
        new Matrix3x3(0d, -1d, 0d,
            -1d, 0d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(0d, 0d, -1d,
            0d, -1d, 0d,
            -1d, 0d, 0d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 0d, -1d,
            0d, -1d, 0d));
  }

  private static List<Matrix3x3> symmetryTransformationsD6h() {
    return asList(
        new Matrix3x3(1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(0.5d, 0.5d * Math.sqrt(3d), 0d,
            -0.5d * Math.sqrt(3d), 0.5d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-0.5d, 0.5d * Math.sqrt(3d), 0d,
            -0.5d * Math.sqrt(3d), -0.5d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(0.5d, -0.5d * Math.sqrt(3d), 0d,
            -0.5d * Math.sqrt(3d), -0.5d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(0.5d, 0.5d * Math.sqrt(3d), 0d,
            0.5d * Math.sqrt(3d), -0.5d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-0.5d, -0.5d * Math.sqrt(3d), 0d,
            -0.5d * Math.sqrt(3d), 0.5d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-0.5d, 0.5d * Math.sqrt(3d), 0d,
            0.5d * Math.sqrt(3d), 0.5d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(0.5d, -0.5d * Math.sqrt(3d), 0d,
            0.5d * Math.sqrt(3d), 0.5d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-0.5d, -0.5d * Math.sqrt(3d), 0d,
            0.5d * Math.sqrt(3d), -0.5d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, -1d));
  }

  private static List<Matrix3x3> symmetryTransformationsD4h() {
    return asList(
        new Matrix3x3(1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(0d, 1d, 0d,
            -1d, 0d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(0d, -1d, 0d,
            -1d, 0d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(0d, 1d, 0d,
            1d, 0d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(0d, -1d, 0d,
            1d, 0d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, -1d));
  }

  private static List<Matrix3x3> symmetryTransformationsD3d() {
    return asList(
        new Matrix3x3(1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-0.5d, 0.5d * Math.sqrt(3d), 0d,
            -0.5d * Math.sqrt(3d), -0.5d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-0.5d, -0.5d * Math.sqrt(3d), 0d,
            0.5d * Math.sqrt(3d), -0.5d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-0.5d, -0.5d * Math.sqrt(3d), 0d,
            -0.5d * Math.sqrt(3d), 0.5d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-0.5d, 0.5d * Math.sqrt(3d), 0d,
            0.5d * Math.sqrt(3d), 0.5d, 0d,
            0d, 0d, -1d));
  }

  private static List<Matrix3x3> symmetryTransformationsD2h() {
    return asList(
        new Matrix3x3(1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(1d, 0d, 0d,
            0d, -1d, 0d,
            0d, 0d, -1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, -1d));
  }

  private static List<Matrix3x3> symmetryTransformationsC2h() {
    return asList(
        new Matrix3x3(1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, 1d),
        new Matrix3x3(-1d, 0d, 0d,
            0d, 1d, 0d,
            0d, 0d, -1d));
  }
}
