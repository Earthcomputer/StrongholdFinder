package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerRiverMix extends GenLayer {

	private GenLayer biomeLayer;
	private GenLayer riverLayer;

	public GenLayerRiverMix(long uniquifier, GenLayer biomeLayer, GenLayer riverLayer) {
		super(uniquifier);
		this.biomeLayer = biomeLayer;
		this.riverLayer = riverLayer;
	}

	@Override
	public void initWorldSeed(long seed) {
		biomeLayer.initWorldSeed(seed);
		riverLayer.initWorldSeed(seed);
		super.initWorldSeed(seed);
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] biomeValues = this.biomeLayer.getValues(x, z, width, height);
		int[] riverValues = this.riverLayer.getValues(x, z, width, height);
		int[] values = IntCache.get(width * height);

		for (int index = 0; index < width * height; index++) {
			if (biomeValues[index] != Biomes.OCEAN && biomeValues[index] != Biomes.DEEP_OCEAN) {
				if (riverValues[index] == Biomes.RIVER) {
					if (biomeValues[index] == Biomes.ICE_FLATS) {
						values[index] = Biomes.FROZEN_RIVER;
					} else if (biomeValues[index] != Biomes.MUSHROOM_ISLAND
							&& biomeValues[index] != Biomes.MUSHROOM_ISLAND_SHORE) {
						values[index] = riverValues[index] & 255;
					} else {
						values[index] = Biomes.MUSHROOM_ISLAND_SHORE;
					}
				} else {
					values[index] = biomeValues[index];
				}
			} else {
				values[index] = biomeValues[index];
			}
		}

		return values;
	}

}
