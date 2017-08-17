package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerAddSnow extends GenLayer {

	public GenLayerAddSnow(long uniquifier, GenLayer parent) {
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

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				int oldValue = parentValues[dx + 1 + (dz + 1) * parentWidth];

				initChunkSeed(dx + x, dz + z);

				if (oldValue == 0) {
					values[dx + dz * width] = 0;
				} else {
					int newValue = nextInt(6);

					if (newValue == 0) {
						newValue = 4;
					} else if (newValue <= 1) {
						newValue = 3;
					} else {
						newValue = 1;
					}

					values[dx + dz * width] = newValue;
				}
			}
		}

		return values;
	}

}
