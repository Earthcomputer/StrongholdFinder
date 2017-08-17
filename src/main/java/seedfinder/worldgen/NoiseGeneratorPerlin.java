package seedfinder.worldgen;

import java.util.Arrays;
import java.util.Random;

public class NoiseGeneratorPerlin {

	private final NoiseGeneratorSimplex[] noiseLevels;
	private final int levels;

	public NoiseGeneratorPerlin(Random rand, int levels) {
		this.levels = levels;
		this.noiseLevels = new NoiseGeneratorSimplex[levels];

		for (int i = 0; i < levels; ++i) {
			this.noiseLevels[i] = new NoiseGeneratorSimplex(rand);
		}
	}

	public double getValue(double x, double y) {
		double output = 0.0D;
		double frequency = 1.0D;

		for (int i = 0; i < this.levels; i++) {
			output += this.noiseLevels[i].getValue(x * frequency, y * frequency) / frequency;
			frequency /= 2.0D;
		}

		return output;
	}

	public double[] getRegion(double[] values, double x, double y, int width, int height, double p_151599_8_,
			double p_151599_10_, double p_151599_12_) {
		return this.getRegion(values, x, y, width, height, p_151599_8_, p_151599_10_, p_151599_12_, 0.5D);
	}

	// TODO: deobfuscate further
	public double[] getRegion(double[] values, double x, double y, int width, int height, double p_151600_8_,
			double p_151600_10_, double p_151600_12_, double lacunarity) {
		if (values != null && values.length >= width * height) {
			Arrays.fill(values, 0);
		} else {
			values = new double[width * height];
		}

		double frequency = 1.0D;
		double d0 = 1.0D;

		for (int i = 0; i < this.levels; i++) {
			this.noiseLevels[i].add(values, x, y, width, height, p_151600_8_ * d0 * frequency,
					p_151600_10_ * d0 * frequency, 0.55D / frequency);
			d0 *= p_151600_12_;
			frequency *= lacunarity;
		}

		return values;
	}

}
