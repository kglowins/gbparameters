package io.github.kglowins.gbparams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BoundaryParameterName {
    LEFT_PHI1("Left_phi1", true),
    LEFT_PHI("Left_Phi", true),
    LEFT_PHI2("Left_phi2", true),

    RIGHT_PHI1("Right_phi1", true),
    RIGHT_PHI("Right_Phi", true),
    RIGHT_PHI2("Right_phi2", true),

    POLAR("Polar", true),
    AZIMUTH("Azimuth", true),

    PHASE_ID("PhaseId", true),
    POINT_GROUP("PointGroup", true),

    AREA("Area", true),
    FACES("Faces", true),

    TILT_APRX("Tilt(Aprx)", false),
    TWIST_APRX("Twist(Aprx)", false),
    SYMMETRIC_APRX("Symmetric(Aprx)", false),
    TILT_180_APRX("180-tilt(Aprx)", false),

    TILT_AM("Tilt(AM)", false),
    TWIST_AM("Twist(AM)", false),
    SYMMETRIC_AM("Symmetric(AM)", false),
    TILT_180_AM("180-tilt(AM)", false),

    TILT_COMP("TiltCompnt", false),
    TWIST_COMP("TwistCompnt", false),
    DISOR("DisorAngl", false),
    //TTC("TTC(Disor)", false),
    ENERGY("Energy", false);

    String csvHeader;
    boolean required;
}
