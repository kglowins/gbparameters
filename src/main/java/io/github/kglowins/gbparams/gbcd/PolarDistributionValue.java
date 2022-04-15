package io.github.kglowins.gbparams.gbcd;

import io.github.kglowins.gbparams.enums.BoundaryParameterName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.math3.util.FastMath;

import java.util.EnumMap;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class PolarDistributionValue {
    final double polar;
    final double azimuth;
    final double stereographicX;
    final double stereographicY;
    //final double equalAreaX;
    //final double equalAreaY;
    final Map<BoundaryParameterName, Double> values;

    public PolarDistributionValue(double polar, double azimuth) {
        values = new EnumMap<>(BoundaryParameterName.class);
        this.polar = polar;
        this.azimuth = azimuth;
        final double stereographicR = FastMath.tan(0.5 * polar);
        stereographicX = stereographicR * FastMath.cos(azimuth);
        stereographicY = stereographicR * FastMath.sin(azimuth);
        //final double equalAreaR = FastMath.sin(0.5 * polar);
        //equalAreaX = equalAreaR * FastMath.cos(azimuth);
        //equalAreaY = equalAreaR * FastMath.sin(azimuth);
    }

    public void set(BoundaryParameterName param, double value) {
        values.put(param, value);
    }

    public double get(BoundaryParameterName param) {
        return values.get(param);
    }

}
