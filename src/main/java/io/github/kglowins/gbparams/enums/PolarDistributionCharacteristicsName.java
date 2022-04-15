package io.github.kglowins.gbparams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PolarDistributionCharacteristicsName {
    POLAR("Polar"),
    AZIMUTH("Azimuth"),
    STEREOGRAPHIC_X("X(StereogrProj)"),
    STEREOGRAPHIC_Y("Y(StereogrProj)"),
    VALUE("Value");

    String csvHeader;
}
