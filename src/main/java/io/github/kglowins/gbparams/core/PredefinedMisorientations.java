package io.github.kglowins.gbparams.core;

import io.github.kglowins.gbparams.representation.AxisAngle;
import io.github.kglowins.gbparams.representation.UnitVector;

import java.util.Map;

import static java.util.Map.entry;

public class PredefinedMisorientations {

  private PredefinedMisorientations() {
  }

  public static final String S3 = "\u03A33 (60\u00b0; [111])";
  public static final String S5 = "\u03A35: (36.87\u00b0; [100])";
  public static final String S7 = "\u03A37: (38.21\u00b0; [111])";
  public static final String S9 = "\u03A39: (38.94\u00b0; [110])";
  public static final String S11 = "\u03A311: (50.48\u00b0; [110])";
  public static final String S13a = "\u03A313a: (22.62\u00b0; [100])";
  public static final String S13b = "\u03A313b: (27.8\u00b0; [111])";
  public static final String S15 = "\u03A315: (48.19\u00b0; [210])";
  public static final String S17a = "\u03A317a: (28.07\u00b0; [100])";
  public static final String S17b = "\u03A317b: (61.93\u00b0; [221])";
  public static final String S19a = "\u03A319a: (26.53\u00b0; [110])";
  public static final String S19b = "\u03A319b: (46.83\u00b0; [111])";
  public static final String S21a = "\u03A321a: (21.79\u00b0; [111])";
  public static final String S21b = "\u03A321b: (44.42\u00b0; [211])";
  public static final String S23 = "\u03A323: (40.46\u00b0; [311])";
  public static final String S25a = "\u03A325a: (16.26\u00b0; [100])";
  public static final String S25b = "\u03A325b: (51.68\u00b0; [331])";
  public static final String S27a = "\u03A327a: (31.59\u00b0; [110])";
  public static final String S27b = "\u03A327b: (35.43\u00b0; [210])";
  public static final String S29a = "\u03A329a: (43.6\u00b0; [100])";
  public static final String S29b = "\u03A329b: (46.4\u00b0; [221])";
  public static final String S31a = "\u03A331a: (17.9\u00b0; [111])";
  public static final String S31b = "\u03A331b: (52.2\u00b0; [211])";
  public static final String S39b = "\u03A339b: (50.13\u00b0; [321])";

  public static Object[] createOptionsOh() {
    return new Object[]{S3, S5, S7, S9, S11, S13a, S13b, S15, S17a, S17b, S19a, S19b, S21a, S21b,
        S23, S25a, S25b, S27a, S27b, S29a, S29b, S31a, S31b, S39b};
  }

  public static Map<String, AxisAngle> createOptionsToAxisAngleOh() {
    return Map.ofEntries(
        entry(S3, createAxisAngle(1d, 1d, 1d, 60d)),
        entry(S5, createAxisAngle(1d, 0d, 0d, 36.8699d)),
        entry(S7, createAxisAngle(1d, 1d, 1d, 38.2132d)),
        entry(S9, createAxisAngle(1d, 1d, 0d, 38.9424d)),
        entry(S11, createAxisAngle(1d, 1d, 0d, 50.4788d)),
        entry(S13a, createAxisAngle(1d, 0d, 0d, 22.6199d)),
        entry(S13b, createAxisAngle(1d, 1d, 1d, 27.7958d)),
        entry(S15, createAxisAngle(2d, 1d, 0d, 48.1897d)),
        entry(S17a, createAxisAngle(1d, 0d, 0d, 28.0725d)),
        entry(S17b, createAxisAngle(2d, 2d, 1d, 61.9275d)),
        entry(S19a, createAxisAngle(1d, 1d, 0d, 26.5254d)),
        entry(S19b, createAxisAngle(1d, 1d, 1d, 46.8264d)),
        entry(S21a, createAxisAngle(1d, 1d, 1d, 21.7868d)),
        entry(S21b, createAxisAngle(2d, 1d, 1d, 44.4153d)),
        entry(S23, createAxisAngle(3d, 1d, 1d, 40.4591d)),
        entry(S25a, createAxisAngle(1d, 0d, 0d, 16.2602d)),
        entry(S25b, createAxisAngle(3d, 3d, 1d, 51.6839d)),
        entry(S27a, createAxisAngle(1d, 1d, 0d, 31.5863d)),
        entry(S27b, createAxisAngle(2d, 1d, 0d, 35.4309d)),
        entry(S29a, createAxisAngle(1d, 0d, 0d, 43.6028d)),
        entry(S29b, createAxisAngle(2d, 2d, 1d, 46.3972d)),
        entry(S31a, createAxisAngle(1d, 1d, 1d, 17.8966d)),
        entry(S31b, createAxisAngle(2d, 1d, 1d, 52.2003d)),
        entry(S39b, createAxisAngle(3d, 2d, 1d, 50.132d))
    );
  }

  private static AxisAngle createAxisAngle(double x, double y, double z, double angle) {
    UnitVector axis = new UnitVector();
    axis.set(x, y, z);
    double angleRad = Math.toRadians(angle);
    AxisAngle axisAngle = new AxisAngle();
    axisAngle.set(axis, angleRad);
    return axisAngle;
  }
}
