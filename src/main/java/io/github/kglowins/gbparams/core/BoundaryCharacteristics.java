package io.github.kglowins.gbparams.core;

import io.github.kglowins.gbparams.representation.EulerAngles;
import io.github.kglowins.gbparams.representation.UnitVector;
import io.github.kglowins.gbparams.enums.PointGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.math3.util.Precision.round;

@Getter
@Setter
@Builder
@Slf4j
public class BoundaryCharacteristics {
  private EulerAngles leftEulerAngles;
  private EulerAngles rightEulerAngles;
  private UnitVector normal;
  private int phaseId;
  private PointGroup pointGroup;
  private double area;
  private int faces;

  private Double tiltAprx;
  private Double twistAprx;
  private Double symmetricAprx;
  private Double tilt180Aprx;

  private Double tiltAM;
  private Double twistAM;
  private Double symmetricAM;
  private Double tilt180AM;

  private Double tiltComponent;
  private Double twistComponent;

  private Double disorientation;
  private Double tiltTwistCompParam;

  private Double energy;


  public String[] toCsvRecord() { // order matters!
    List<String> values = new ArrayList<>();

    values.add(String.valueOf(round(leftEulerAngles.phi1(),4)));
    values.add(String.valueOf(round(leftEulerAngles.Phi(),4)));
    values.add(String.valueOf(round(leftEulerAngles.phi2(),4)));
    values.add(String.valueOf(round(rightEulerAngles.phi1(),4)));
    values.add(String.valueOf(round(rightEulerAngles.Phi(),4)));
    values.add(String.valueOf(round(rightEulerAngles.phi2(),4)));
    values.add(String.valueOf(round(normal.zenith(),4)));
    values.add(String.valueOf(round(normal.azimuth(),4)));
    values.add(String.valueOf(phaseId));
    values.add(String.valueOf(pointGroup.getLabel()));
    values.add(String.valueOf(round(area, 8)));
    values.add(String.valueOf(faces));

    addToRecordIfNonNull(tiltAprx, values);
    addToRecordIfNonNull(twistAprx, values);
    addToRecordIfNonNull(symmetricAprx, values);
    addToRecordIfNonNull(tilt180Aprx, values);
    addToRecordIfNonNull(tiltAM, values);
    addToRecordIfNonNull(twistAM, values);
    addToRecordIfNonNull(symmetricAM, values);
    addToRecordIfNonNull(tilt180AM, values);
    addToRecordIfNonNull(tiltComponent, values);
    addToRecordIfNonNull(twistComponent, values);
    addToRecordIfNonNull(disorientation, values);
    addToRecordIfNonNull(energy, values);

    return values.stream().toArray(String[]::new);
  }

  private void addToRecordIfNonNull(Double value, List<String> values) {
    if (value != null) {
      values.add(String.valueOf(round(value, 4)));
    }
  }
}
