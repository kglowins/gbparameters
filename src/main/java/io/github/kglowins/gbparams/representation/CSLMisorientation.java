package io.github.kglowins.gbparams.representation;

public final class CSLMisorientation implements Comparable<CSLMisorientation> {
	
	private final Matrix3x3 M;
	private final int sigma;

	public CSLMisorientation(Matrix3x3 M, int sigma) {
		this.sigma = sigma;
		this.M = M;
	}

	public Matrix3x3 getM() {
		return M;
	}

	public int getSigma() {
		return sigma;
	}

	@Override
	public int compareTo(CSLMisorientation that) {
		return Integer.compare(sigma, that.getSigma());
	}
}
