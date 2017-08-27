package seedfinder.worldgen;

import java.util.Random;

public class NoiseGeneratorSimplex {

	// TODO: deobfuscate

	private static final int[][] grad3 = new int[][] { { 1, 1, 0 }, { -1, 1, 0 }, { 1, -1, 0 }, { -1, -1, 0 },
			{ 1, 0, 1 }, { -1, 0, 1 }, { 1, 0, -1 }, { -1, 0, -1 }, { 0, 1, 1 }, { 0, -1, 1 }, { 0, 1, -1 },
			{ 0, -1, -1 } };
	public static final double SQRT_3 = Math.sqrt(3.0D);
	private final int[] p;
	public double xo;
	public double yo;
	public double zo;
	private static final double F2 = 0.5D * (SQRT_3 - 1.0D);
	private static final double G2 = (3.0D - SQRT_3) / 6.0D;

	public NoiseGeneratorSimplex() {
		this(new Random());
	}

	public NoiseGeneratorSimplex(Random rand) {
		this.p = new int[512];
		this.xo = rand.nextDouble() * 256.0D;
		this.yo = rand.nextDouble() * 256.0D;
		this.zo = rand.nextDouble() * 256.0D;

		for (int i = 0; i < 256; this.p[i] = i++) {
			;
		}

		for (int l = 0; l < 256; ++l) {
			int j = rand.nextInt(256 - l) + l;
			int k = this.p[l];
			this.p[l] = this.p[j];
			this.p[j] = k;
			this.p[l + 256] = this.p[l];
		}
	}

	private static int fastFloor(double value) {
		return value > 0.0D ? (int) value : (int) value - 1;
	}

	private static double dot(int[] a, double b0, double b1) {
		return a[0] * b0 + a[1] * b1;
	}

	public double getValue(double x, double y) {
		// https://en.wikipedia.org/wiki/Simplex_noise
		double s = (x + y) * F2;
		int i = fastFloor(x + s);
		int j = fastFloor(y + s);
		double t = (i + j) * G2;
		double X0 = i - t;
		double Y0 = j - t;
		double x0 = x - X0;
		double y0 = y - Y0;
		int i1;
		int j1;

		if (x0 > y0) {
			i1 = 1;
			j1 = 0;
		} else {
			i1 = 0;
			j1 = 1;
		}

		double x1 = x0 - i1 + G2;
		double y1 = y0 - j1 + G2;
		double x2 = x0 - 1.0D + 2.0D * G2;
		double y2 = y0 - 1.0D + 2.0D * G2;
		i &= 255;
		j &= 255;
		int gradIdx0 = this.p[i + this.p[j]] % 12;
		int gradIdx1 = this.p[i + i1 + this.p[j + j1]] % 12;
		int gradIdx2 = this.p[i + 1 + this.p[j + 1]] % 12;
		double t0 = 0.5D - x0 * x0 - y0 * y0;

		double n0;
		if (t0 < 0.0D) {
			n0 = 0.0D;
		} else {
			t0 = t0 * t0;
			n0 = t0 * t0 * dot(grad3[gradIdx0], x0, y0);
		}

		double t1 = 0.5D - x1 * x1 - y1 * y1;

		double n1;
		if (t1 < 0.0D) {
			n1 = 0.0D;
		} else {
			t1 = t1 * t1;
			n1 = t1 * t1 * dot(grad3[gradIdx1], x1, y1);
		}

		double t2 = 0.5D - x2 * x2 - y2 * y2;

		double n2;
		if (t2 < 0.0D) {
			n2 = 0.0D;
		} else {
			t2 = t2 * t2;
			n2 = t2 * t2 * dot(grad3[gradIdx2], x2, y2);
		}

		return 70.0D * (n0 + n1 + n2);
	}

	public void add(double[] values, double x, double y, int width, int height, double p_151606_8_, double p_151606_10_,
			double amplitude) {
		int index = 0;

		for (int dy = 0; dy < height; dy++) {
			double yHere = (y + dy) * p_151606_10_ + this.yo;

			for (int dx = 0; dx < width; dx++) {
				double xHere = (x + dx) * p_151606_8_ + this.xo;
				double s = (xHere + yHere) * F2;
				int i = fastFloor(xHere + s);
				int j = fastFloor(yHere + s);
				double d6 = (i + j) * G2;
				double X0 = i - d6;
				double X1 = j - d6;
				double x0 = xHere - X0;
				double y0 = yHere - X1;
				int i1;
				int j1;

				if (x0 > y0) {
					i1 = 1;
					j1 = 0;
				} else {
					i1 = 0;
					j1 = 1;
				}

				double x1 = x0 - i1 + G2;
				double y1 = y0 - j1 + G2;
				double x2 = x0 - 1.0D + 2.0D * G2;
				double y2 = y0 - 1.0D + 2.0D * G2;
				i &= 255;
				j &= 255;
				int gradIdx0 = this.p[i + this.p[j]] % 12;
				int gradIdx1 = this.p[i + i1 + this.p[j + j1]] % 12;
				int gradIdx2 = this.p[i + 1 + this.p[j + 1]] % 12;
				double t0 = 0.5D - x0 * x0 - y0 * y0;
				double n0;

				if (t0 < 0.0D) {
					n0 = 0.0D;
				} else {
					t0 = t0 * t0;
					n0 = t0 * t0 * dot(grad3[gradIdx0], x0, y0);
				}

				double t1 = 0.5D - x1 * x1 - y1 * y1;
				double n1;

				if (t1 < 0.0D) {
					n1 = 0.0D;
				} else {
					t1 = t1 * t1;
					n1 = t1 * t1 * dot(grad3[gradIdx1], x1, y1);
				}

				double t2 = 0.5D - x2 * x2 - y2 * y2;
				double n2;

				if (t2 < 0.0D) {
					n2 = 0.0D;
				} else {
					t2 = t2 * t2;
					n2 = t2 * t2 * dot(grad3[gradIdx2], x2, y2);
				}

				values[index++] += 70.0D * (n0 + n1 + n2) * amplitude;
			}
		}
	}

}
