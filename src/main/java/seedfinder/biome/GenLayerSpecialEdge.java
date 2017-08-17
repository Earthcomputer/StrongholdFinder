package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerSpecialEdge extends GenLayer {

	public GenLayerSpecialEdge(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] parentValues = parent.getValues(x, z, width, height);
		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				int value = parentValues[dx + dz * width];

				initChunkSeed(dx + x, dz + z);

				if (value != 0 && nextInt(13) == 0) {
					value |= 1 + nextInt(15) << 8 & 0xf00;
				}

				values[dx + dz * width] = value;
			}
		}

		return values;
	}

}
