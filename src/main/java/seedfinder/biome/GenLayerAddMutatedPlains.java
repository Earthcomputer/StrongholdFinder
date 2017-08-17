package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerAddMutatedPlains extends GenLayer {

	public GenLayerAddMutatedPlains(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] parentValues = parent.getValues(x - 1, z - 1, width + 2, height + 2);
		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				initChunkSeed(dx + x, dz + z);

				int oldValue = parentValues[dx + 1 + (dz + 1) * (width + 2)];

				if (nextInt(57) == 0) {
					if (oldValue == Biomes.PLAINS) {
						values[dx + dz * width] = Biomes.MUTATED | Biomes.PLAINS;
					} else {
						values[dx + dz * width] = oldValue;
					}
				} else {
					values[dx + dz * width] = oldValue;
				}
			}
		}

		return values;
	}

}
