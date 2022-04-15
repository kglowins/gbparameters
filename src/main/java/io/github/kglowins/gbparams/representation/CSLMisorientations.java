package io.github.kglowins.gbparams.representation;

import io.github.kglowins.gbparams.utils.MillerIndices;
import io.github.kglowins.gbparams.utils.Transformations;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;
import static org.apache.commons.math3.util.ArithmeticUtils.gcd;


public final class CSLMisorientations {

	private static final double EPSILON = 1e-5d;

	private CSLMisorientations() {
	}

	public static CSLMisorientation[] cubic(int maxSigma) {
		List<CSLMisorientation> csl = new ArrayList<>();
		csl.add(new CSLMisorientation(new Matrix3x3(), 1));

		int sqrtOfMaxSigma = (int) ceil(sqrt(maxSigma));
			
		for (int m = 1; m <= sqrtOfMaxSigma; m++) {
			for (int U = 0; U <= m; U++) {
				for (int V = 0; V <= U; V++) {
					for (int W = 0; W <= V; W++) {
						if (U != 0 || V != 0 || W != 0) {
							if (gcd(m, gcd(U, gcd(V, W))) == 1) {

								int N = 0;
								if (m % 2 == 1) N++;
								if (U % 2 == 1) N++;
								if (V % 2 == 1) N++;
								if (W % 2 == 1) N++;

								if (N == 1 || N == 3) {

									int msq = m * m;
									int Usq = U * U;
									int Vsq = V * V;
									int Wsq = W * W;
									int S = msq + Usq + Vsq + Wsq;

									if (S <= maxSigma) {

										int UV = 2 * U * V;
										int UW = 2 * U * W;
										int VW = 2 * V * W;

										int mU = 2 * m * U;
										int mV = 2 * m * V;
										int mW = 2 * m * W;

										Matrix3x3 M = new Matrix3x3(
											(double) (msq + Usq - Vsq - Wsq) / (double) S, (double) (UV - mW) / (double) S, (double) (UW + mV) / (double) S,
											(double) (UV + mW) / (double) S, (double) (msq - Usq + Vsq - Wsq) / (double) S, (double) (VW - mU) / (double) S,
											(double) (UW - mV) / (double) S, (double) (VW + mU) / (double) S, (double) (msq - Usq - Vsq + Wsq) / (double) S
										);
										csl.add(new CSLMisorientation(M, S));

										AxisAngle aa = new AxisAngle();
										aa.set(M);
									}
								}
							}
						}
					}
				}
			}			
		}	
		Collections.sort(csl);
		CSLMisorientation[] arrM = new CSLMisorientation[csl.size()];
		return csl.toArray(arrM);
	}
	
	
	public static CSLMisorientation[] hexagonal(int maxSigma, int mu, int nu) {
		
		//c^2 = mu, a^2 = nu
		
		final Matrix3x3 S = Transformations.getHexToCartesian(sqrt(nu), sqrt(mu));
				
		final ArrayList<CSLMisorientation> CSL = new ArrayList<CSLMisorientation>();
		
		CSL.add(new CSLMisorientation(new Matrix3x3(), 1));

		for(int m = 1; m <= maxSigma; m++) {
			
			final int maxU = (int) Math.round( sqrt(4d*mu/nu) * m );
			
			for(int U = 0; U <= maxU; U++) {
				
				final int maxV = U / 2;
				
				for(int V = 0; V <= maxV; V++) {
					
					final int maxW = (int) Math.round( m / (2d/ sqrt(3d) + 1d));
					
					for(int W = 0; W <= maxW; W++) {

						if(U != 0 || V != 0 || W != 0)
						if(gcd(m, gcd(U, gcd(V,W))) == 1)
							
							if( m >= sqrt( nu / (4d*mu) ) * U )
							if( m >= sqrt( nu / (12d*mu) ) * (2d*U - V) )
							if( m >= (2d/ sqrt(3d) + 1d) * W)
						{
						
								
							if(Math.abs( m - sqrt( nu/(4d*mu) ) * U ) < EPSILON) {
								if( W > sqrt( nu/(4d*mu) ) * (2d*U - V) ) continue;
							}
																									
							if(Math.abs( m - sqrt( nu/(12d*mu) ) * (2d*U - V)) < EPSILON) {
								if( W > sqrt( 3d*nu/(4d*mu) ) * V ) continue;
							}
								
							if(Math.abs( m - (2d / sqrt(3d) + 1d) * W) < EPSILON) {
								if( U < (2d + sqrt(3d) * V) ) continue;
							}			
								
							
							int F = mu*(3*m*m + W*W) + nu*(U*U - U*V + V*V);
							int F1 = gcd(2, gcd(U, gcd(V, m+W)));
							int F2 = gcd(3, gcd(U+V, W));
							int F3 = gcd(2/F1, gcd(nu,m+W));
							int F4 = gcd(nu/F3, gcd(2*W/(F1*F2), m+W));
							int F5 = gcd(mu, gcd(3*U/(F1*F2),(U+V)/F1));
							
							int sigma = F/(F1*F1*F2*F3*F4*F5);
							
							if(sigma <= maxSigma) {
								
								final double theta = 2d * FastMath.atan(sqrt((double)(nu*(U*U - U*V + V*V) + mu*W*W) / (3*mu*m*m)));
								
								final MillerIndices hexAxis = new MillerIndices();
								hexAxis.set(U, V, W);
								
								UnitVector axis = new UnitVector();
								axis.setAsNonCubicAxis(hexAxis, S);
								
								
								AxisAngle aa = new AxisAngle();
								aa.set(axis, theta);
						
								Matrix3x3 M = new Matrix3x3();
								M.set(aa);
								
								CSL.add(new CSLMisorientation(M, sigma));
							}
						}			
					}	
				}	
			}		
		}
	
		Collections.sort(CSL);
		CSLMisorientation[] arrM = new CSLMisorientation[CSL.size()];
		return CSL.toArray(arrM);
	}
}
