package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerAddMushroomIsland extends GenLayer {

	public GenLayerAddMushroomIsland(long uniquifier, GenLayer parent) {
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
				int valueLeftUp = parentValues[dx + 0 + (dz + 0) * parentWidth];
				int valueRightUp = parentValues[dx + 2 + (dz + 0) * parentWidth];
				int valueLeftDown = parentValues[dx + 0 + (dz + 2) * parentWidth];
				int valueRightDown = parentValues[dx + 2 + (dz + 2) * parentWidth];
				int valueHere = parentValues[dx + 1 + (dz + 1) * parentWidth];

				initChunkSeed(dx + x, dz + z);

				if (valueHere == 0 && valueLeftUp == 0 && valueRightUp == 0 && valueLeftDown == 0 && valueRightDown == 0
						&& nextInt(100) == 0) {
					values[dx + dz * width] = Biomes.MUSHROOM_ISLAND;
				} else {
					values[dx + dz * width] = valueHere;
				}
			}
		}

		return values;
	}

}
