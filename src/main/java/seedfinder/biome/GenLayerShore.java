package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerShore extends GenLayer {

	public GenLayerShore(long uniquifier, GenLayer parent) {
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

				if (oldValue == Biomes.MUSHROOM_ISLAND) {
					int valueUp = parentValues[dx + 1 + dz * (width + 2)];
					int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
					int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
					int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

					if (valueUp != Biomes.OCEAN && valueRight != Biomes.OCEAN && valueLeft != Biomes.OCEAN
							&& valueDown != Biomes.OCEAN) {
						values[dx + dz * width] = oldValue;
					} else {
						values[dx + dz * width] = Biomes.MUSHROOM_ISLAND_SHORE;
					}
				} else if (Biomes.getType(oldValue) == Biomes.EnumType.JUNGLE) {
					int valueUp = parentValues[dx + 1 + dz * (width + 2)];
					int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
					int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
					int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

					if (isJungleCompatible(valueUp) && isJungleCompatible(valueRight) && isJungleCompatible(valueLeft)
							&& isJungleCompatible(valueDown)) {
						if (!Biomes.isOceanic(valueUp) && !Biomes.isOceanic(valueRight) && !Biomes.isOceanic(valueLeft)
								&& !Biomes.isOceanic(valueDown)) {
							values[dx + dz * width] = oldValue;
						} else {
							values[dx + dz * width] = Biomes.BEACHES;
						}
					} else {
						values[dx + dz * width] = Biomes.JUNGLE_EDGE;
					}
				} else if (oldValue != Biomes.EXTREME_HILLS && oldValue != Biomes.EXTREME_HILLS_WITH_TREES
						&& oldValue != Biomes.SMALLER_EXTREME_HILLS) {
					if (Biomes.isSnowy(oldValue)) {
						replaceIfNeighborOcean(parentValues, values, dx, dz, width, oldValue, Biomes.COLD_BEACH);
					} else if (oldValue != Biomes.MESA && oldValue != Biomes.MESA_ROCK) {
						if (oldValue != Biomes.OCEAN && oldValue != Biomes.DEEP_OCEAN && oldValue != Biomes.RIVER
								&& oldValue != Biomes.SWAMPLAND) {
							int valueUp = parentValues[dx + 1 + dz * (width + 2)];
							int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
							int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
							int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

							if (!Biomes.isOceanic(valueUp) && !Biomes.isOceanic(valueRight)
									&& !Biomes.isOceanic(valueLeft) && !Biomes.isOceanic(valueDown)) {
								values[dx + dz * width] = oldValue;
							} else {
								values[dx + dz * width] = Biomes.BEACHES;
							}
						} else {
							values[dx + dz * width] = oldValue;
						}
					} else {
						int valueUp = parentValues[dx + 1 + dz * (width + 2)];
						int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
						int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
						int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

						if (!Biomes.isOceanic(valueUp) && !Biomes.isOceanic(valueRight) && !Biomes.isOceanic(valueLeft)
								&& !Biomes.isOceanic(valueDown)) {
							if (isMesa(valueUp) && isMesa(valueRight) && isMesa(valueLeft) && isMesa(valueDown)) {
								values[dx + dz * width] = oldValue;
							} else {
								values[dx + dz * width] = Biomes.DESERT;
							}
						} else {
							values[dx + dz * width] = oldValue;
						}
					}
				} else {
					replaceIfNeighborOcean(parentValues, values, dx, dz, width, oldValue, Biomes.STONE_BEACH);
				}
			}
		}

		return values;
	}

	private static void replaceIfNeighborOcean(int[] parentValues, int[] values, int dx, int dz, int width,
			int oldValue, int newValue) {
		if (Biomes.isOceanic(oldValue)) {
			values[dx + dz * width] = oldValue;
		} else {
			int valueUp = parentValues[dx + 1 + dz * (width + 2)];
			int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
			int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
			int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

			if (!Biomes.isOceanic(valueUp) && !Biomes.isOceanic(valueRight) && !Biomes.isOceanic(valueLeft)
					&& !Biomes.isOceanic(valueDown)) {
				values[dx + dz * width] = oldValue;
			} else {
				values[dx + dz * width] = newValue;
			}
		}
	}

	private static boolean isJungleCompatible(int biome) {
		if (Biomes.getType(biome) == Biomes.EnumType.JUNGLE) {
			return true;
		} else {
			return biome == Biomes.FOREST || biome == Biomes.TAIGA || Biomes.isOceanic(biome);
		}
	}

	private static boolean isMesa(int biome) {
		return Biomes.getType(biome) == Biomes.EnumType.MESA;
	}

}
