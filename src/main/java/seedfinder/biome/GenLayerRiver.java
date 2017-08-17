package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerRiver extends GenLayer {

	public GenLayerRiver(long uniquifier, GenLayer randomValues) {
		super(uniquifier);
		this.parent = randomValues;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int parentX = x - 1;
		int parentZ = z - 1;
		int parentWidth = width + 2;
		int parentHeight = height + 2;
		int[] randomValues = this.parent.getValues(parentX, parentZ, parentWidth, parentHeight);

		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				int valLeft = riverFilter(randomValues[dx + (dz + 1) * parentWidth]);
				int valRight = riverFilter(randomValues[dx + 2 + (dz + 1) * parentWidth]);
				int valUp = riverFilter(randomValues[dx + 1 + dz * parentWidth]);
				int valDown = riverFilter(randomValues[dx + 1 + (dz + 2) * parentWidth]);
				int valHere = riverFilter(randomValues[dx + 1 + (dz + 1) * parentWidth]);

				if (valHere == valLeft && valHere == valUp && valHere == valRight && valHere == valDown) {
					values[dx + dz * width] = -1;
				} else {
					values[dx + dz * width] = Biomes.RIVER;
				}
			}
		}

		return values;
	}

	private static int riverFilter(int val) {
		return val >= 2 ? 2 + (val & 1) : val;
	}

}
