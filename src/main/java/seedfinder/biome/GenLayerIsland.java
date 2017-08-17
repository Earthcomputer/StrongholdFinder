package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerIsland extends GenLayer {

	public GenLayerIsland(long uniquifier) {
		super(uniquifier);
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				initChunkSeed(x + dx, z + dz);
				values[dx + dz * width] = nextInt(10) == 0 ? 1 : 0;
			}
		}

		if (x > -width && x <= 0 && z > -height && z <= 0) {
			values[-x + -z * width] = 1;
		}

		return values;
	}

}
