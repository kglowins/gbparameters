package io.github.kglowins.gbparams.utils.tuple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class DoubleTriple {
  private Double x;
  private Double y;
  private Double z;

  public static DoubleTriple of(Double x, Double y, Double z) {
    return new DoubleTriple(x, y, z);
  }
}
