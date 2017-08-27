package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerAddIsland extends GenLayer {

	public GenLayerAddIsland(long uniquifier, GenLayer parent) {
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
				int valueLeftUp = parentValues[dx + dz * parentWidth];
				int valueRightUp = parentValues[dx + 2 + dz * parentWidth];
				int valueLeftDown = parentValues[dx + (dz + 2) * parentWidth];
				int valueRightDown = parentValues[dx + 2 + (dz + 2) * parentWidth];
				int valueHere = parentValues[dx + 1 + (dz + 1) * parentWidth];

				initChunkSeed(dx + x, dz + z);

				if (valueHere != 0
						|| valueLeftUp == 0 && valueRightUp == 0 && valueLeftDown == 0 && valueRightDown == 0) {
					if (valueHere > 0
							&& (valueLeftUp == 0 || valueRightUp == 0 || valueLeftDown == 0 || valueRightDown == 0)) {
						if (nextInt(5) == 0) {
							if (valueHere == 4) {
								values[dx + dz * width] = 4;
							} else {
								values[dx + dz * width] = 0;
							}
						} else {
							values[dx + dz * width] = valueHere;
						}
					} else {
						values[dx + dz * width] = valueHere;
					}
				} else {
					int rarity = 1;
					int oceanCount = 1;

					if (valueLeftUp != 0 && nextInt(rarity++) == 0) {
						oceanCount = valueLeftUp;
					}

					if (valueRightUp != 0 && nextInt(rarity++) == 0) {
						oceanCount = valueRightUp;
					}

					if (valueLeftDown != 0 && nextInt(rarity++) == 0) {
						oceanCount = valueLeftDown;
					}

					if (valueRightDown != 0 && nextInt(rarity++) == 0) {
						oceanCount = valueRightDown;
					}

					if (nextInt(3) == 0) {
						values[dx + dz * width] = oceanCount;
					} else if (oceanCount == 4) {
						values[dx + dz * width] = 4;
					} else {
						values[dx + dz * width] = 0;
					}
				}
			}
		}

		return values;
	}

}
