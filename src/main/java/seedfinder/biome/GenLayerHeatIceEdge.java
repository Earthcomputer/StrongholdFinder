package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerHeatIceEdge extends GenLayer {

	public GenLayerHeatIceEdge(long uniquifier, GenLayer parent) {
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
				int valueHere = parentValues[dx + 1 + (dz + 1) * parentWidth];

				if (valueHere == 4) {
					int valueUp = parentValues[dx + 1 + (dz + 1 - 1) * parentWidth];
					int valueRight = parentValues[dx + 1 + 1 + (dz + 1) * parentWidth];
					int valueLeft = parentValues[dx + 1 - 1 + (dz + 1) * parentWidth];
					int valueDown = parentValues[dx + 1 + (dz + 1 + 1) * parentWidth];
					boolean any2 = valueUp == 2 || valueRight == 2 || valueLeft == 2 || valueDown == 2;
					boolean any1 = valueUp == 1 || valueRight == 1 || valueLeft == 1 || valueDown == 1;

					if (any1 || any2) {
						valueHere = 3;
					}
				}

				values[dx + dz * width] = valueHere;
			}
		}

		return values;
	}

}
