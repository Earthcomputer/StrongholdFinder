package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerSmooth extends GenLayer {

	public GenLayerSmooth(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int parentX = x - 1;
		int parentZ = z - 1;
		int parentWidth = width + 2;
		int parentHeight = height + 2;
		int[] parentValues = parent.getValues(parentX, parentZ, parentWidth, parentHeight);

		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; ++dz) {
			for (int dx = 0; dx < width; ++dx) {
				int valueLeft = parentValues[dx + (dz + 1) * parentWidth];
				int valueRight = parentValues[dx + 2 + (dz + 1) * parentWidth];
				int valueUp = parentValues[dx + 1 + dz * parentWidth];
				int valueDown = parentValues[dx + 1 + (dz + 2) * parentWidth];
				int valueHere = parentValues[dx + 1 + (dz + 1) * parentWidth];

				if (valueLeft == valueRight && valueUp == valueDown) {
					initChunkSeed(dx + x, dz + z);

					if (nextInt(2) == 0) {
						valueHere = valueLeft;
					} else {
						valueHere = valueUp;
					}
				} else {
					if (valueLeft == valueRight) {
						valueHere = valueLeft;
					}

					if (valueUp == valueDown) {
						valueHere = valueUp;
					}
				}

				values[dx + dz * width] = valueHere;
			}
		}

		return values;
	}

}
