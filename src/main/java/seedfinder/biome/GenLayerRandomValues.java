package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerRandomValues extends GenLayer {

	public GenLayerRandomValues(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] parentValues = parent.getValues(x, z, width, height);
		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				initChunkSeed(dx + x, dz + z);
				values[dx + dz * width] = parentValues[dx + dz * width] > 0 ? nextInt(299999) + 2 : 0;
			}
		}

		return values;
	}

}
