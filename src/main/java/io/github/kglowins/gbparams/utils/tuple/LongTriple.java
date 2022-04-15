package io.github.kglowins.gbparams.utils.tuple;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class LongTriple {
  private Long left;
  private Long middle;
  private Long right;

  public static LongTriple of(Long left, Long middle, Long right) {
    return new LongTriple(left, middle, right);
  }
}
