package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerCoolWarmEdge extends GenLayer {

	public GenLayerCoolWarmEdge(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int parentX = x - 1;
		int parentZ = z - 1;
		int parentWidth = width + 2;
		int parentHeight = height + 2;
		int[] parentValues = this.parent.getValues(parentX, parentZ, parentWidth, parentHeight);

		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				int valueHere = parentValues[dx + 1 + (dz + 1) * parentWidth];

				initChunkSeed(dx + x, dz + z);

				if (valueHere == 1) {
					int valueUp = parentValues[dx + 1 + dz * parentWidth];
					int valueRight = parentValues[dx + 2 + (dz + 1) * parentWidth];
					int valueLeft = parentValues[dx + (dz + 1) * parentWidth];
					int valueDown = parentValues[dx + 1 + (dz + 2) * parentWidth];
					boolean any3 = valueUp == 3 || valueRight == 3 || valueLeft == 3 || valueDown == 3;
					boolean any4 = valueUp == 4 || valueRight == 4 || valueLeft == 4 || valueDown == 4;

					if (any3 || any4) {
						valueHere = 2;
					}
				}

				values[dx + dz * width] = valueHere;
			}
		}

		return values;
	}

}
