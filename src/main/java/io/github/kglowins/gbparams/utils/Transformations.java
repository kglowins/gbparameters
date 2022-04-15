package io.github.kglowins.gbparams.utils;

import io.github.kglowins.gbparams.representation.Matrix3x3;

public class Transformations {
	

	public final static Matrix3x3 getHexToCartesian(double a, double c) {
		
		return new Matrix3x3(a, -0.5d*a, 0d,
							 0d, 0.5d*Math.sqrt(3d)*a, 0d,
							 0d, 0d, c);
	}
	
	public final static Matrix3x3 getCartesianToHex(double a, double c) {
		
		return new Matrix3x3(1d/a, 1d/Math.sqrt(3d)/a, 0d,
							 0d, 2d/Math.sqrt(3d)/a, 0d,
							 0d, 0d, 1d/c);
	}
	
	public final static Matrix3x3 getTetrToCartesian(double a, double c) {
		return new Matrix3x3(a, 0d, 0d,
							 0d, a, 0d,
							 0d, 0d, c);
	}
	
	public final static Matrix3x3 getCartesianToTetr(double a, double c) {
		return new Matrix3x3(1/a, 0d, 0d,
				 			 0d, 1/a, 0d,
				 			 0d, 0d, 1/c);
	}
	
	public final static Matrix3x3 getOrthToCartesian(double a, double b, double c) {
		return new Matrix3x3(a, 0d, 0d,
				 			 0d, b, 0d,
				 			 0d, 0d, c);
	}
	
	public final static Matrix3x3 getCartesianToOrth(double a, double b, double c) {
		return new Matrix3x3(1/a, 0d, 0d,
				 			 0d, 1/b, 0d,
				 			 0d, 0d, 1/c);
	}
}
