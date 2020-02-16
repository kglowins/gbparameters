package io.github.kglowins.gbparameters.gbcd;

import io.github.kglowins.gbparameters.representation.UnitVector;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.awt.geom.Point2D;
import java.util.List;

@Value
@Accessors(fluent = true)
@Builder
public class GbcdGbLocation {
    private UnitVector axis;
    private Point2D coords;
    private List<Point2D> zone;
    // private axis miller indices
}
