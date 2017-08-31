package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerBiome extends GenLayer {

	private static final int[] WARM_BIOMES = { Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.SAVANNA,
			Biomes.SAVANNA, Biomes.PLAINS };
	private static final int[] MEDIUM_BIOMES = { Biomes.FOREST, Biomes.ROOFED_FOREST, Biomes.EXTREME_HILLS,
			Biomes.PLAINS, Biomes.BIRCH_FOREST, Biomes.SWAMPLAND };
	private static final int[] COLD_BIOMES = { Biomes.FOREST, Biomes.EXTREME_HILLS, Biomes.TAIGA, Biomes.PLAINS };
	private static final int[] ICE_BIOMES = { Biomes.ICE_FLATS, Biomes.ICE_FLATS, Biomes.ICE_FLATS, Biomes.TAIGA_COLD };

	public GenLayerBiome(long uniquifier, GenLayer parent) {
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
				int oldValue = parentValues[dx + dz * width];
				int specialEdgeFlag = (oldValue & 0xf00) >> 8;
				oldValue = oldValue & 0xfffff0ff;

				if (Biomes.isOceanic(oldValue)) {
					values[dx + dz * width] = oldValue;
				} else if (oldValue == Biomes.MUSHROOM_ISLAND) {
					values[dx + dz * width] = oldValue;
				} else if (oldValue == 1) {
					if (specialEdgeFlag > 0) {
						if (nextInt(3) == 0) {
							values[dx + dz * width] = Biomes.MESA_CLEAR_ROCK;
						} else {
							values[dx + dz * width] = Biomes.MESA_ROCK;
						}
					} else {
						values[dx + dz * width] = WARM_BIOMES[nextInt(WARM_BIOMES.length)];
					}
				} else if (oldValue == 2) {
					if (specialEdgeFlag > 0) {
						values[dx + dz * width] = Biomes.JUNGLE;
					} else {
						values[dx + dz * width] = MEDIUM_BIOMES[nextInt(MEDIUM_BIOMES.length)];
					}
				} else if (oldValue == 3) {
					if (specialEdgeFlag > 0) {
						values[dx + dz * width] = Biomes.REDWOOD_TAIGA;
					} else {
						values[dx + dz * width] = COLD_BIOMES[nextInt(COLD_BIOMES.length)];
					}
				} else if (oldValue == 4) {
					values[dx + dz * width] = ICE_BIOMES[nextInt(ICE_BIOMES.length)];
				} else {
					values[dx + dz * width] = Biomes.MUSHROOM_ISLAND;
				}
			}
		}

		return values;
	}

}
