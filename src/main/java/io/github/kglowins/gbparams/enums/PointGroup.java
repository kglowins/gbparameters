package io.github.kglowins.gbparams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PointGroup {
	M3M("m3m"),
	_6MMM("6/mmm"),
	_4MMM("4/mmm"),
	MMM("mmm"),
	_2M("2/m"),
	_3M("3/m"),
	_1("1");

	String label;
}
