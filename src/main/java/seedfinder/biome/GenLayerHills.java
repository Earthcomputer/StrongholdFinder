package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerHills extends GenLayer {

	private GenLayer randomValuesLayer;

	public GenLayerHills(long uniquifier, GenLayer parent, GenLayer randomValuesLayer) {
		super(uniquifier);
		this.parent = parent;
		this.randomValuesLayer = randomValuesLayer;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] parentValues = parent.getValues(x - 1, z - 1, width + 2, height + 2);
		int[] randomValues = randomValuesLayer.getValues(x - 1, z - 1, width + 2, height + 2);
		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				initChunkSeed(dx + x, dz + z);

				int parentValue = parentValues[dx + 1 + (dz + 1) * (width + 2)];
				int randomValue = randomValues[dx + 1 + (dz + 1) * (width + 2)];
				boolean mutateHills = (randomValue - 2) % 29 == 0;

				if (parentValue > 255) {
					System.err.printf("old! %d\n", parentValue);
				}

				boolean isParentValueMutated = Biomes.isBiome(parentValue) && (parentValue & Biomes.MUTATED) != 0;

				if (parentValue != 0 && randomValue >= 2 && (randomValue - 2) % 29 == 1 && !isParentValueMutated) {
					int parentValueMutated = parentValue | Biomes.MUTATED;
					values[dx + dz * width] = !Biomes.isBiome(parentValueMutated) ? parentValue : parentValueMutated;
				} else if (nextInt(3) != 0 && !mutateHills) {
					values[dx + dz * width] = parentValue;
				} else {
					int newBiome = parentValue;

					if (parentValue == Biomes.DESERT) {
						newBiome = Biomes.DESERT_HILLS;
					} else if (parentValue == Biomes.FOREST) {
						newBiome = Biomes.FOREST_HILLS;
					} else if (parentValue == Biomes.BIRCH_FOREST) {
						newBiome = Biomes.BIRCH_FOREST_HILLS;
					} else if (parentValue == Biomes.ROOFED_FOREST) {
						newBiome = Biomes.PLAINS;
					} else if (parentValue == Biomes.TAIGA) {
						newBiome = Biomes.TAIGA_HILLS;
					} else if (parentValue == Biomes.REDWOOD_TAIGA) {
						newBiome = Biomes.REDWOOD_TAIGA_HILLS;
					} else if (parentValue == Biomes.TAIGA_COLD) {
						newBiome = Biomes.TAIGA_COLD_HILLS;
					} else if (parentValue == Biomes.PLAINS) {
						if (nextInt(3) == 0) {
							newBiome = Biomes.FOREST_HILLS;
						} else {
							newBiome = Biomes.FOREST;
						}
					} else if (parentValue == Biomes.ICE_FLATS) {
						newBiome = Biomes.ICE_MOUNTAINS;
					} else if (parentValue == Biomes.JUNGLE) {
						newBiome = Biomes.JUNGLE_HILLS;
					} else if (parentValue == Biomes.OCEAN) {
						newBiome = Biomes.DEEP_OCEAN;
					} else if (parentValue == Biomes.EXTREME_HILLS) {
						newBiome = Biomes.EXTREME_HILLS_WITH_TREES;
					} else if (parentValue == Biomes.SAVANNA) {
						newBiome = Biomes.SAVANNA_ROCK;
					} else if (Biomes.sameType(parentValue, Biomes.MESA_ROCK)) {
						newBiome = Biomes.MESA;
					} else if (parentValue == Biomes.DEEP_OCEAN && nextInt(3) == 0) {
						if (nextInt(2) == 0) {
							newBiome = Biomes.PLAINS;
						} else {
							newBiome = Biomes.FOREST;
						}
					}

					if (mutateHills && newBiome != parentValue) {
						int mutated = newBiome | Biomes.MUTATED;
						newBiome = !Biomes.isBiome(mutated) ? parentValue : mutated;
					}

					if (newBiome == parentValue) {
						values[dx + dz * width] = parentValue;
					} else {
						int parentValueUp = parentValues[dx + 1 + (dz + 0) * (width + 2)];
						int parentValueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
						int parentValueLeft = parentValues[dx + 0 + (dz + 1) * (width + 2)];
						int parentValueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];
						int surroundingSameType = 0;

						if (Biomes.sameType(parentValueUp, parentValue)) {
							surroundingSameType++;
						}

						if (Biomes.sameType(parentValueRight, parentValue)) {
							surroundingSameType++;
						}

						if (Biomes.sameType(parentValueLeft, parentValue)) {
							surroundingSameType++;
						}

						if (Biomes.sameType(parentValueDown, parentValue)) {
							surroundingSameType++;
						}

						if (surroundingSameType >= 3) {
							values[dx + dz * width] = newBiome;
						} else {
							values[dx + dz * width] = parentValue;
						}
					}
				}
			}
		}

		return values;
	}

}
