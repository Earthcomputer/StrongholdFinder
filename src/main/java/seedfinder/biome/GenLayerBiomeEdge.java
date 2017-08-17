package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerBiomeEdge extends GenLayer {

	public GenLayerBiomeEdge(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int[] parentValues = this.parent.getValues(x - 1, z - 1, width + 2, height + 2);
		int[] values = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				initChunkSeed(dx + x, dz + z);

				int oldValue = parentValues[dx + 1 + (dz + 1) * (width + 2)];

				if (!replaceBiomeEdgeIfNecessary(parentValues, values, dx, dz, width, oldValue, Biomes.EXTREME_HILLS,
						Biomes.SMALLER_EXTREME_HILLS)
						&& !replaceBiomeEdge(parentValues, values, dx, dz, width, oldValue, Biomes.MESA_ROCK,
								Biomes.MESA)
						&& !replaceBiomeEdge(parentValues, values, dx, dz, width, oldValue, Biomes.MESA_CLEAR_ROCK,
								Biomes.MESA)
						&& !replaceBiomeEdge(parentValues, values, dx, dz, width, oldValue, Biomes.REDWOOD_TAIGA,
								Biomes.TAIGA)) {
					if (oldValue == Biomes.DESERT) {
						int valueUp = parentValues[dx + 1 + dz * (width + 2)];
						int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
						int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
						int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

						if (valueUp != Biomes.ICE_FLATS && valueRight != Biomes.ICE_FLATS
								&& valueLeft != Biomes.ICE_FLATS && valueDown != Biomes.ICE_FLATS) {
							values[dx + dz * width] = oldValue;
						} else {
							values[dx + dz * width] = Biomes.EXTREME_HILLS_WITH_TREES;
						}
					} else if (oldValue == Biomes.SWAMPLAND) {
						int valueUp = parentValues[dx + 1 + dz * (width + 2)];
						int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
						int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
						int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

						if (valueUp != Biomes.DESERT && valueRight != Biomes.DESERT && valueLeft != Biomes.DESERT
								&& valueDown != Biomes.DESERT && valueUp != Biomes.TAIGA_COLD
								&& valueRight != Biomes.TAIGA_COLD && valueLeft != Biomes.TAIGA_COLD
								&& valueDown != Biomes.TAIGA_COLD && valueUp != Biomes.ICE_FLATS
								&& valueRight != Biomes.ICE_FLATS && valueLeft != Biomes.ICE_FLATS
								&& valueDown != Biomes.ICE_FLATS) {
							if (valueUp != Biomes.JUNGLE && valueDown != Biomes.JUNGLE && valueRight != Biomes.JUNGLE
									&& valueLeft != Biomes.JUNGLE) {
								values[dx + dz * width] = oldValue;
							} else {
								values[dx + dz * width] = Biomes.JUNGLE_EDGE;
							}
						} else {
							values[dx + dz * width] = Biomes.PLAINS;
						}
					} else {
						values[dx + dz * width] = oldValue;
					}
				}
			}
		}

		return values;
	}

	private static boolean replaceBiomeEdgeIfNecessary(int[] parentValues, int[] values, int dx, int dz, int width,
			int oldValue, int biomeToReplace, int edgeBiome) {
		if (!Biomes.sameType(oldValue, biomeToReplace)) {
			return false;
		} else {
			int valueUp = parentValues[dx + 1 + dz * (width + 2)];
			int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
			int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
			int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

			if (canBiomesBeNeighbors(valueUp, biomeToReplace) && canBiomesBeNeighbors(valueRight, biomeToReplace)
					&& canBiomesBeNeighbors(valueLeft, biomeToReplace)
					&& canBiomesBeNeighbors(valueDown, biomeToReplace)) {
				values[dx + dz * width] = oldValue;
			} else {
				values[dx + dz * width] = edgeBiome;
			}

			return true;
		}
	}

	private static boolean replaceBiomeEdge(int[] parentValues, int[] values, int dx, int dz, int width, int oldValue,
			int biomeToReplace, int newBiome) {
		if (oldValue != biomeToReplace) {
			return false;
		} else {
			int valueUp = parentValues[dx + 1 + dz * (width + 2)];
			int valueRight = parentValues[dx + 2 + (dz + 1) * (width + 2)];
			int valueLeft = parentValues[dx + (dz + 1) * (width + 2)];
			int valueDown = parentValues[dx + 1 + (dz + 2) * (width + 2)];

			if (Biomes.sameType(valueUp, biomeToReplace) && Biomes.sameType(valueRight, biomeToReplace)
					&& Biomes.sameType(valueLeft, biomeToReplace) && Biomes.sameType(valueDown, biomeToReplace)) {
				values[dx + dz * width] = oldValue;
			} else {
				values[dx + dz * width] = newBiome;
			}

			return true;
		}
	}

	private static boolean canBiomesBeNeighbors(int a, int b) {
		if (Biomes.sameType(a, b)) {
			return true;
		} else {
			if (Biomes.isBiome(a) && Biomes.isBiome(b)) {
				Biomes.EnumTempCategory tempA = Biomes.getTempCategory(a);
				Biomes.EnumTempCategory tempB = Biomes.getTempCategory(b);
				return tempA == tempB || tempA == Biomes.EnumTempCategory.MEDIUM
						|| tempB == Biomes.EnumTempCategory.MEDIUM;
			} else {
				return false;
			}
		}
	}

}
