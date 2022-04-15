package io.github.kglowins.gbparams.gbcd;

import io.github.kglowins.gbparams.representation.UnitVector;

public class SymmetryAxis {
	private final UnitVector axis;
	private final int multiplicity;

	public SymmetryAxis(UnitVector axis, int multiplicity) {
		this.axis = axis;
		this.multiplicity = multiplicity;
	}

	public int getMultiplicity() {
		return multiplicity;
	}

	public UnitVector getAxis() {
		return axis;
	}
}