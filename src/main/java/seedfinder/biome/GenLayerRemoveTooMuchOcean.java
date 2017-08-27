package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerRemoveTooMuchOcean extends GenLayer {

	public GenLayerRemoveTooMuchOcean(long uniquifier, GenLayer parent) {
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
				int valueUp = parentValues[dx + 1 + dz * parentWidth];
				int valueRight = parentValues[dx + 2 + (dz + 1) * parentWidth];
				int valueLeft = parentValues[dx + (dz + 1) * parentWidth];
				int valueDown = parentValues[dx + 1 + (dz + 2) * parentWidth];
				int valueHere = parentValues[dx + 1 + (dz + 1) * parentWidth];
				values[dx + dz * width] = valueHere;

				initChunkSeed(x + dx, z + dz);

				if (valueHere == 0 && valueUp == 0 && valueRight == 0 && valueLeft == 0 && valueDown == 0
						&& nextInt(2) == 0) {
					values[dx + dz * width] = 1;
				}
			}
		}

		return values;
	}

}
