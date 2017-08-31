package seedfinder.worldgen;

import java.util.Arrays;
import java.util.Random;

import seedfinder.util.MathHelper;

public class NoiseGeneratorOctaves {

	private final NoiseGeneratorImproved[] generatorCollection;
	private final int octaves;

	public NoiseGeneratorOctaves(Random rand, int octaves) {
		this.octaves = octaves;
		this.generatorCollection = new NoiseGeneratorImproved[octaves];

		for (int i = 0; i < octaves; i++) {
			this.generatorCollection[i] = new NoiseGeneratorImproved(rand);
		}
	}

	public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize,
			int ySize, int zSize, double xScale, double yScale, double zScale) {
		if (noiseArray == null) {
			noiseArray = new double[xSize * ySize * zSize];
		} else {
			Arrays.fill(noiseArray, 0);
		}

		double scale = 1;

		for (int i = 0; i < octaves; i++) {
			double scaledXOffset = xOffset * scale * xScale;
			double scaledYOffset = yOffset * scale * yScale;
			double scaledZOffset = zOffset * scale * zScale;
			long a = MathHelper.lfloor(scaledXOffset);
			long b = MathHelper.lfloor(scaledYOffset);
			scaledXOffset -= a;
			scaledZOffset -= b;
			a %= 16777216;
			b %= 16777216;
			scaledXOffset += a;
			scaledZOffset += b;
			generatorCollection[i].populateNoiseArray(noiseArray, scaledXOffset, scaledYOffset, scaledZOffset, xSize,
					ySize, zSize, xScale * scale, yScale * scale, zScale * scale, scale);
			scale /= 2;
		}

		return noiseArray;
	}

	public double[] generateNoiseOctaves(double[] noiseArray, int xOffset, int zOffset, int xSize, int zSize,
			double xScale, double zScale, double exponent) {
		return generateNoiseOctaves(noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1, zScale);
	}

}
