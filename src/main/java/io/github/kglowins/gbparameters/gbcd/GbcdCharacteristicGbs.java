package io.github.kglowins.gbparameters.gbcd;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GbcdCharacteristicGbs {
    private List<GbcdGbLocation> twist;
    private List<GbcdGbLocation> symmetric;
    private List<GbcdGbLocation> tilt;
    private List<GbcdGbLocation> tilt180;
}
