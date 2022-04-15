package io.github.kglowins.gbparams.gbcd;

import lombok.Value;
import lombok.experimental.Accessors;

import java.awt.geom.Point2D;
import java.util.List;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
public class GbcdSymmetries {
    List<SymmetryAxis> symmetryAxes;
    List<List<Point2D>> mirrorLines;
}
