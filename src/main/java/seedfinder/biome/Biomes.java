package seedfinder.biome;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import seedfinder.Blocks;
import seedfinder.util.Storage3D;
import seedfinder.worldgen.NoiseGeneratorPerlin;
import seedfinder.worldgen.WorldGen;

public class Biomes {

	private Biomes() {
	}

	// @formatter:off
	public static final int[] ALL_BIOMES = {
		// Normal
		  0,   1,   2,   3,   4,   5,   6,   7,   8,   9,
		 10,  11,  12,  13,  14,  15,  16,  17,  18,  19,
		 20,  21,  22,  23,  24,  25,  26,  27,  28,  29,
		 30,  31,  32,  33,  34,  35,  36,  37,  38,  39,
		// Void
		127,
		// Mutated
		     129, 130, 131, 132, 133, 134,
		          140,
		     149,      151,                155, 156, 157,
		158,      160, 161, 162, 163, 164, 165, 166, 167,
	};
	// @formatter:on
	public static final Set<Integer> ALL_BIOMES_SET = setOf(ALL_BIOMES);

	private static final float[] BASE_HEIGHTS = new float[256];
	private static final float[] HEIGHT_VARS = new float[256];

	// @formatter:off
	public static final int
			OCEAN = 0,
			PLAINS = 1,
			DESERT = 2,
			EXTREME_HILLS = 3,
			FOREST = 4,
			TAIGA = 5,
			SWAMPLAND = 6,
			RIVER = 7,
			HELL = 8,
			SKY = 9,
			FROZEN_OCEAN = 10,
			FROZEN_RIVER = 11,
			ICE_FLATS = 12,
			ICE_MOUNTAINS = 13,
			MUSHROOM_ISLAND = 14,
			MUSHROOM_ISLAND_SHORE = 15,
			BEACHES = 16,
			DESERT_HILLS = 17,
			FOREST_HILLS = 18,
			TAIGA_HILLS = 19,
			SMALLER_EXTREME_HILLS = 20,
			JUNGLE = 21,
			JUNGLE_HILLS = 22,
			JUNGLE_EDGE = 23,
			DEEP_OCEAN = 24,
			STONE_BEACH = 25,
			COLD_BEACH = 26,
			BIRCH_FOREST = 27,
			BIRCH_FOREST_HILLS = 28,
			ROOFED_FOREST = 29,
			TAIGA_COLD = 30,
			TAIGA_COLD_HILLS = 31,
			REDWOOD_TAIGA = 32,
			REDWOOD_TAIGA_HILLS = 33,
			EXTREME_HILLS_WITH_TREES = 34,
			SAVANNA = 35,
			SAVANNA_ROCK = 36,
			MESA = 37,
			MESA_ROCK = 38,
			MESA_CLEAR_ROCK = 39,
			VOID = 127,
			MUTATED = 128;
	// @formatter:on

	static {
		BASE_HEIGHTS[OCEAN] = -1;
		BASE_HEIGHTS[PLAINS] = 0.125F;
		BASE_HEIGHTS[DESERT] = 0.125F;
		BASE_HEIGHTS[EXTREME_HILLS] = 1;
		BASE_HEIGHTS[FOREST] = 0.1F;
		BASE_HEIGHTS[TAIGA] = 0.2F;
		BASE_HEIGHTS[SWAMPLAND] = -0.2F;
		BASE_HEIGHTS[RIVER] = -0.5F;
		BASE_HEIGHTS[HELL] = 0.1F;
		BASE_HEIGHTS[SKY] = 0.1F;
		BASE_HEIGHTS[FROZEN_OCEAN] = -1;
		BASE_HEIGHTS[FROZEN_RIVER] = -0.5F;
		BASE_HEIGHTS[ICE_FLATS] = 0.125F;
		BASE_HEIGHTS[ICE_MOUNTAINS] = 0.45F;
		BASE_HEIGHTS[MUSHROOM_ISLAND] = 0.2F;
		BASE_HEIGHTS[MUSHROOM_ISLAND_SHORE] = 0;
		BASE_HEIGHTS[BEACHES] = 0;
		BASE_HEIGHTS[DESERT_HILLS] = 0.45F;
		BASE_HEIGHTS[FOREST_HILLS] = 0.45F;
		BASE_HEIGHTS[TAIGA_HILLS] = 0.45F;
		BASE_HEIGHTS[SMALLER_EXTREME_HILLS] = 0.8F;
		BASE_HEIGHTS[JUNGLE] = 0.1F;
		BASE_HEIGHTS[JUNGLE_HILLS] = 0.45F;
		BASE_HEIGHTS[JUNGLE_EDGE] = 0.1F;
		BASE_HEIGHTS[DEEP_OCEAN] = -1.8F;
		BASE_HEIGHTS[STONE_BEACH] = 0.1F;
		BASE_HEIGHTS[COLD_BEACH] = 0;
		BASE_HEIGHTS[BIRCH_FOREST] = 0.1F;
		BASE_HEIGHTS[BIRCH_FOREST_HILLS] = 0.45F;
		BASE_HEIGHTS[ROOFED_FOREST] = 0.1F;
		BASE_HEIGHTS[TAIGA_COLD] = 0.2F;
		BASE_HEIGHTS[TAIGA_COLD_HILLS] = 0.2F;
		BASE_HEIGHTS[REDWOOD_TAIGA] = 0.2F;
		BASE_HEIGHTS[REDWOOD_TAIGA_HILLS] = 0.45F;
		BASE_HEIGHTS[EXTREME_HILLS_WITH_TREES] = 1;
		BASE_HEIGHTS[SAVANNA] = 0.125F;
		BASE_HEIGHTS[SAVANNA_ROCK] = 1.5F;
		BASE_HEIGHTS[MESA] = 0.1F;
		BASE_HEIGHTS[MESA_ROCK] = 1.5F;
		BASE_HEIGHTS[MESA_CLEAR_ROCK] = 1.5F;
		BASE_HEIGHTS[VOID] = 0.1F;
		BASE_HEIGHTS[MUTATED | PLAINS] = 0.125F;
		BASE_HEIGHTS[MUTATED | DESERT] = 0.225F;
		BASE_HEIGHTS[MUTATED | EXTREME_HILLS] = 1;
		BASE_HEIGHTS[MUTATED | FOREST] = 0.1F;
		BASE_HEIGHTS[MUTATED | TAIGA] = 0.3F;
		BASE_HEIGHTS[MUTATED | SWAMPLAND] = -0.1F;
		BASE_HEIGHTS[MUTATED | ICE_FLATS] = 0.425F;
		BASE_HEIGHTS[MUTATED | JUNGLE] = 0.2F;
		BASE_HEIGHTS[MUTATED | JUNGLE_EDGE] = 0.2F;
		BASE_HEIGHTS[MUTATED | BIRCH_FOREST] = 0.2F;
		BASE_HEIGHTS[MUTATED | BIRCH_FOREST_HILLS] = 0.55F;
		BASE_HEIGHTS[MUTATED | ROOFED_FOREST] = 0.2F;
		BASE_HEIGHTS[MUTATED | TAIGA_COLD] = 0.3F;
		BASE_HEIGHTS[MUTATED | REDWOOD_TAIGA] = 0.2F;
		BASE_HEIGHTS[MUTATED | REDWOOD_TAIGA_HILLS] = 0.2F;
		BASE_HEIGHTS[MUTATED | EXTREME_HILLS_WITH_TREES] = 1;
		BASE_HEIGHTS[MUTATED | SAVANNA] = 0.3625F;
		BASE_HEIGHTS[MUTATED | SAVANNA_ROCK] = 1.05F;
		BASE_HEIGHTS[MUTATED | MESA] = 0.1F;
		BASE_HEIGHTS[MUTATED | MESA_ROCK] = 0.45F;
		BASE_HEIGHTS[MUTATED | MESA_CLEAR_ROCK] = 0.45F;

		HEIGHT_VARS[OCEAN] = 0.1F;
		HEIGHT_VARS[PLAINS] = 0.05F;
		HEIGHT_VARS[DESERT] = 0.05F;
		HEIGHT_VARS[EXTREME_HILLS] = 0.5F;
		HEIGHT_VARS[FOREST] = 0.2F;
		HEIGHT_VARS[TAIGA] = 0.2F;
		HEIGHT_VARS[SWAMPLAND] = 0.1F;
		HEIGHT_VARS[RIVER] = 0;
		HEIGHT_VARS[HELL] = 0.2F;
		HEIGHT_VARS[SKY] = 0.2F;
		HEIGHT_VARS[FROZEN_OCEAN] = 0.1F;
		HEIGHT_VARS[FROZEN_RIVER] = 0;
		HEIGHT_VARS[ICE_FLATS] = 0.05F;
		HEIGHT_VARS[ICE_MOUNTAINS] = 0.3F;
		HEIGHT_VARS[MUSHROOM_ISLAND] = 0.3F;
		HEIGHT_VARS[MUSHROOM_ISLAND_SHORE] = 0.025F;
		HEIGHT_VARS[BEACHES] = 0.025F;
		HEIGHT_VARS[DESERT_HILLS] = 0.3F;
		HEIGHT_VARS[FOREST_HILLS] = 0.3F;
		HEIGHT_VARS[TAIGA_HILLS] = 0.3F;
		HEIGHT_VARS[SMALLER_EXTREME_HILLS] = 0.3F;
		HEIGHT_VARS[JUNGLE] = 0.2F;
		HEIGHT_VARS[JUNGLE_HILLS] = 0.3F;
		HEIGHT_VARS[JUNGLE_EDGE] = 0.2F;
		HEIGHT_VARS[DEEP_OCEAN] = 0.1F;
		HEIGHT_VARS[STONE_BEACH] = 0.8F;
		HEIGHT_VARS[COLD_BEACH] = 0.025F;
		HEIGHT_VARS[BIRCH_FOREST] = 0.2F;
		HEIGHT_VARS[BIRCH_FOREST_HILLS] = 0.3F;
		HEIGHT_VARS[ROOFED_FOREST] = 0.2F;
		HEIGHT_VARS[TAIGA_COLD] = 0.2F;
		HEIGHT_VARS[TAIGA_COLD_HILLS] = 0.3F;
		HEIGHT_VARS[REDWOOD_TAIGA] = 0.2F;
		HEIGHT_VARS[REDWOOD_TAIGA_HILLS] = 0.3F;
		HEIGHT_VARS[EXTREME_HILLS_WITH_TREES] = 0.5F;
		HEIGHT_VARS[SAVANNA] = 0.05F;
		HEIGHT_VARS[SAVANNA_ROCK] = 0.025F;
		HEIGHT_VARS[MESA] = 0.2F;
		HEIGHT_VARS[MESA_ROCK] = 0.025F;
		HEIGHT_VARS[MESA_CLEAR_ROCK] = 0.025F;
		HEIGHT_VARS[VOID] = 0.2F;
		HEIGHT_VARS[MUTATED | PLAINS] = 0.05F;
		HEIGHT_VARS[MUTATED | DESERT] = 0.25F;
		HEIGHT_VARS[MUTATED | EXTREME_HILLS] = 0.5F;
		HEIGHT_VARS[MUTATED | FOREST] = 0.4F;
		HEIGHT_VARS[MUTATED | TAIGA] = 0.4F;
		HEIGHT_VARS[MUTATED | SWAMPLAND] = 0.3F;
		HEIGHT_VARS[MUTATED | ICE_FLATS] = 0.45000002F; // exactly
		HEIGHT_VARS[MUTATED | JUNGLE] = 0.4F;
		HEIGHT_VARS[MUTATED | JUNGLE_EDGE] = 0.4F;
		HEIGHT_VARS[MUTATED | BIRCH_FOREST] = 0.4F;
		HEIGHT_VARS[MUTATED | BIRCH_FOREST_HILLS] = 0.5F;
		HEIGHT_VARS[MUTATED | ROOFED_FOREST] = 0.4F;
		HEIGHT_VARS[MUTATED | TAIGA_COLD] = 0.4F;
		HEIGHT_VARS[MUTATED | REDWOOD_TAIGA] = 0.2F;
		HEIGHT_VARS[MUTATED | REDWOOD_TAIGA_HILLS] = 0.2F;
		HEIGHT_VARS[MUTATED | EXTREME_HILLS_WITH_TREES] = 0.5F;
		HEIGHT_VARS[MUTATED | SAVANNA] = 1.225F;
		HEIGHT_VARS[MUTATED | SAVANNA_ROCK] = 1.2125001F; // exactly
		HEIGHT_VARS[MUTATED | MESA] = 0.2F;
		HEIGHT_VARS[MUTATED | MESA_ROCK] = 0.3F;
		HEIGHT_VARS[MUTATED | MESA_CLEAR_ROCK] = 0.3F;
	}

	/**
	 * Creates a set containing the given biomes
	 */
	public static Set<Integer> setOf(int... biomes) {
		return Collections.unmodifiableSet(Arrays.stream(biomes).boxed().collect(Collectors.toSet()));
	}

	/**
	 * Creates a set containing all biomes except the given biomes
	 */
	public static Set<Integer> allBiomesExcept(int... biomes) {
		Set<Integer> biomesSet = new HashSet<>(ALL_BIOMES_SET);
		for (int biome : biomes) {
			biomesSet.remove(biome);
		}
		return Collections.unmodifiableSet(biomesSet);
	}

	/**
	 * Creates a set containing all biomes which do not satisfy the given
	 * predicate
	 */
	public static Set<Integer> allBiomesExcept(IntPredicate except) {
		Set<Integer> biomesSet = new HashSet<>(ALL_BIOMES_SET);
		biomesSet.removeIf(except::test);
		return Collections.unmodifiableSet(biomesSet);
	}

	/**
	 * Returns whether the given integer is a valid biome ID
	 */
	public static boolean isBiome(int biome) {
		return ALL_BIOMES_SET.contains(biome);
	}

	/**
	 * Gets the type of the given biome
	 */
	public static EnumType getType(int biome) {
		if (!isBiome(biome)) {
			return null;
		}

		if ((biome & MUTATED) != 0) {
			biome &= ~MUTATED;
		}

		switch (biome) {
		case OCEAN:
		case FROZEN_OCEAN:
		case DEEP_OCEAN:
			return EnumType.OCEAN;
		case PLAINS:
			return EnumType.PLAINS;
		case DESERT:
		case DESERT_HILLS:
			return EnumType.DESERT;
		case EXTREME_HILLS:
		case SMALLER_EXTREME_HILLS:
		case EXTREME_HILLS_WITH_TREES:
			return EnumType.HILLS;
		case FOREST:
		case FOREST_HILLS:
		case BIRCH_FOREST:
		case BIRCH_FOREST_HILLS:
		case ROOFED_FOREST:
			return EnumType.FOREST;
		case TAIGA:
		case TAIGA_HILLS:
		case TAIGA_COLD:
		case TAIGA_COLD_HILLS:
		case REDWOOD_TAIGA:
		case REDWOOD_TAIGA_HILLS:
			return EnumType.TAIGA;
		case SWAMPLAND:
			return EnumType.SWAMP;
		case RIVER:
		case FROZEN_RIVER:
			return EnumType.RIVER;
		case HELL:
			return EnumType.HELL;
		case SKY:
			return EnumType.END;
		case ICE_FLATS:
		case ICE_MOUNTAINS:
			return EnumType.SNOW;
		case MUSHROOM_ISLAND:
		case MUSHROOM_ISLAND_SHORE:
			return EnumType.MUSHROOM_ISLAND;
		case BEACHES:
		case COLD_BEACH:
			return EnumType.BEACH;
		case JUNGLE:
		case JUNGLE_HILLS:
		case JUNGLE_EDGE:
			return EnumType.JUNGLE;
		case STONE_BEACH:
			return EnumType.STONE_BEACH;
		case SAVANNA:
		case SAVANNA_ROCK:
			return EnumType.SAVANNA;
		case MESA:
		case MESA_ROCK:
		case MESA_CLEAR_ROCK:
			return EnumType.MESA;
		case VOID:
			return EnumType.VOID;
		default:
			throw new AssertionError();
		}
	}

	/**
	 * Gets the temperature category of the given biome
	 */
	public static EnumTempCategory getTempCategory(int biome) {
		if (!isBiome(biome)) {
			return null;
		}

		if ((biome & MUTATED) != 0) {
			// Savanna Plateau M is the only mutated biome with a different
			// temperature category to its non-mutated variant.
			if (biome == (MUTATED | SAVANNA_ROCK)) {
				return EnumTempCategory.MEDIUM;
			}

			biome &= ~MUTATED;
		}

		switch (getType(biome)) {
		case OCEAN:
			return EnumTempCategory.OCEAN;
		case RIVER:
			return biome == FROZEN_RIVER ? EnumTempCategory.COLD : EnumTempCategory.MEDIUM;
		case TAIGA:
			return biome == TAIGA_COLD || biome == TAIGA_COLD_HILLS ? EnumTempCategory.COLD : EnumTempCategory.MEDIUM;
		case BEACH:
			return biome == COLD_BEACH ? EnumTempCategory.COLD : EnumTempCategory.MEDIUM;
		case SNOW:
			return EnumTempCategory.COLD;
		case PLAINS:
		case HILLS:
		case FOREST:
		case SWAMP:
		case END:
		case MUSHROOM_ISLAND:
		case JUNGLE:
		case STONE_BEACH:
		case VOID:
			return EnumTempCategory.MEDIUM;
		case DESERT:
		case HELL:
		case SAVANNA:
		case MESA:
			return EnumTempCategory.WARM;
		default:
			throw new AssertionError();
		}
	}

	/**
	 * Gets the base height of the given biome
	 */
	public static float getBaseHeight(int biome) {
		if (biome < 0 || biome >= 256) {
			return 0;
		}
		return BASE_HEIGHTS[biome];
	}

	/**
	 * Gets the height variation of the given biome
	 */
	public static float getHeightVariation(int biome) {
		if (biome < 0 || biome >= 256) {
			return 0;
		}
		return HEIGHT_VARS[biome];
	}

	/**
	 * Returns whether the given biome is snowy
	 */
	public static boolean isSnowy(int biome) {
		return getTempCategory(biome) == EnumTempCategory.COLD || biome == FROZEN_OCEAN;
	}

	/**
	 * Returns whether the given biome is oceanic
	 */
	public static boolean isOceanic(int biome) {
		return biome == OCEAN || biome == DEEP_OCEAN || biome == FROZEN_OCEAN;
	}

	/**
	 * Returns whether the given biomes are the same type
	 */
	public static boolean sameType(int a, int b) {
		if (a == b) {
			return true;
		}
		if (!isBiome(a) || !isBiome(b)) {
			return false;
		}
		if (a == Biomes.MESA_ROCK || a == Biomes.MESA_CLEAR_ROCK) {
			return b == Biomes.MESA_ROCK || b == Biomes.MESA_CLEAR_ROCK;
		}
		return getType(a) == getType(b);
	}

	private static final NoiseGeneratorPerlin GRASS_COLOR_NOISE = new NoiseGeneratorPerlin(new Random(2345), 1);

	public static void genTerrainBlocks(Storage3D chunk, Random rand, int x, int z, double noiseVal, int biome) {
		// Note: these are swapped in vanilla as well
		int zInChunk = x & 15;
		int xInChunk = z & 15;

		int originalTopBlock = Blocks.GRASS;
		int originalFillerBlock = Blocks.DIRT;

		switch (getType(biome)) {
		case HILLS:
			if ((noiseVal < -1 || noiseVal > 2) && (biome & MUTATED) != 0) {
				originalTopBlock = Blocks.GRAVEL;
				originalFillerBlock = Blocks.GRAVEL;
			} else if (noiseVal > 1 && biome != SMALLER_EXTREME_HILLS && biome != EXTREME_HILLS_WITH_TREES) {
				originalTopBlock = Blocks.STONE;
				originalFillerBlock = Blocks.STONE;
			}
			break;
		case MESA:
			originalTopBlock = Blocks.SAND;
			originalFillerBlock = Blocks.HARDENED_CLAY;
			genMesaTerrainBlocks(chunk, rand, x, z, noiseVal, biome);
			return;
		case SAVANNA:
			if ((biome & MUTATED) != 0) {
				if (noiseVal > 1.75) {
					originalTopBlock = Blocks.STONE;
					originalFillerBlock = Blocks.STONE;
				} else if (noiseVal > -0.5) {
					originalTopBlock = Blocks.DIRT;
				}
			}
			break;
		case SWAMP:
			double grassColor = GRASS_COLOR_NOISE.getValue(x * 0.25, z * 0.25);
			if (grassColor > 0) {
				for (int y = 255; y >= 0; y--) {
					if (!Blocks.isAir(chunk.get(xInChunk, y, zInChunk))) {
						if (y == 62 && chunk.get(xInChunk, y, zInChunk) != Blocks.WATER) {
							chunk.set(xInChunk, y, zInChunk, Blocks.WATER);
							if (grassColor < 0.12) {
								chunk.set(xInChunk, y + 1, zInChunk, Blocks.WATERLILY);
							}
						}
						break;
					}
				}
			}
			break;
		case TAIGA:
			if ((biome & ~MUTATED) == REDWOOD_TAIGA || (biome & ~MUTATED) == REDWOOD_TAIGA_HILLS) {
				if (noiseVal > -0.95) {
					originalTopBlock = Blocks.DIRT;
				}
			}
			break;
		case DESERT:
		case BEACH:
			originalTopBlock = Blocks.SAND;
			originalFillerBlock = Blocks.SAND;
			break;
		case END:
			originalTopBlock = Blocks.DIRT;
			break;
		case MUSHROOM_ISLAND:
			originalTopBlock = Blocks.MYCELIUM;
			break;
		case SNOW:
			originalTopBlock = Blocks.SNOW;
			break;
		case STONE_BEACH:
			originalTopBlock = Blocks.STONE;
			originalFillerBlock = Blocks.STONE;
			break;
		default:
			break;
		}

		final int seaLevel = 63;
		int fillerBlocksLeft = -1;
		int numFillerBlocks = (int) (noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int topBlock = originalTopBlock;
		int fillerBlock = originalFillerBlock;

		for (int y = 255; y >= 0; --y) {
			if (y <= rand.nextInt(5)) {
				chunk.set(xInChunk, y, zInChunk, Blocks.BEDROCK);
			} else {
				int block = chunk.get(xInChunk, y, zInChunk);

				if (Blocks.isAir(block)) {
					fillerBlocksLeft = -1;
				} else if (block == Blocks.STONE) {
					if (fillerBlocksLeft == -1) {
						if (numFillerBlocks <= 0) {
							topBlock = Blocks.AIR;
							fillerBlock = Blocks.STONE;
						} else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
							topBlock = originalTopBlock;
							fillerBlock = originalFillerBlock;
						}

						if (y < seaLevel && Blocks.isAir(topBlock)) {
							if (getTempCategory(biome) == EnumTempCategory.COLD || biome == FROZEN_OCEAN) {
								topBlock = Blocks.ICE;
							} else {
								topBlock = Blocks.WATER;
							}
						}

						fillerBlocksLeft = numFillerBlocks;

						if (y >= seaLevel - 1) {
							chunk.set(xInChunk, y, zInChunk, topBlock);
						} else if (y < seaLevel - 7 - numFillerBlocks) {
							topBlock = Blocks.AIR;
							fillerBlock = Blocks.STONE;
							chunk.set(xInChunk, y, zInChunk, Blocks.GRAVEL);
						} else {
							chunk.set(xInChunk, y, zInChunk, fillerBlock);
						}
					} else if (fillerBlocksLeft > 0) {
						fillerBlocksLeft--;
						chunk.set(xInChunk, y, zInChunk, fillerBlock);

						if (fillerBlocksLeft == 0 && fillerBlock == Blocks.SAND && numFillerBlocks > 1) {
							fillerBlocksLeft = rand.nextInt(4) + Math.max(0, y - 63);
							fillerBlock = Blocks.SANDSTONE;
						}
					}
				}
			}
		}
	}

	private static void genMesaTerrainBlocks(Storage3D chunk, Random rand, int x, int z, double noiseVal, int biome) {
		boolean brycePillars = biome == (MUTATED | MESA);
		boolean hasForest = (biome & ~MUTATED) == MESA_ROCK;

		// TODO: deobfuscate

		double d4 = 0.0D;

		if (brycePillars) {
			int i = (x & -16) + (z & 15);
			int j = (z & -16) + (x & 15);
			double d0 = Math.min(Math.abs(noiseVal), WorldGen.mesaPillarNoise.getValue(i * 0.25D, j * 0.25D));

			if (d0 > 0.0D) {
				final double d1 = 0.001953125D;
				double d2 = Math.abs(WorldGen.mesaPillarRoofNoise.getValue(i * d1, j * 0.001953125D));
				d4 = d0 * d0 * 2.5D;
				double d3 = Math.ceil(d2 * 50.0D) + 14.0D;

				if (d4 > d3) {
					d4 = d3;
				}

				d4 = d4 + 64.0D;
			}
		}

		int k1 = x & 15;
		int l1 = z & 15;
		final int i2 = 63;
		int iblockstate = Blocks.HARDENED_CLAY;
		int iblockstate3 = Blocks.HARDENED_CLAY;
		int k = (int) (noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		boolean flag = Math.cos(noiseVal / 3.0D * Math.PI) > 0.0D;
		int l = -1;
		int i1 = 0;

		for (int j1 = 255; j1 >= 0; --j1) {
			if (Blocks.isAir(chunk.get(l1, j1, k1)) && j1 < (int) d4) {
				chunk.set(l1, j1, k1, Blocks.STONE);
			}

			if (j1 <= rand.nextInt(5)) {
				chunk.set(l1, j1, k1, Blocks.BEDROCK);
			} else if (i1 < 15 || brycePillars) {
				int iblockstate1 = chunk.get(l1, j1, k1);

				if (Blocks.isAir(iblockstate1)) {
					l = -1;
				} else if (iblockstate1 == Blocks.STONE) {
					if (l == -1) {

						if (k <= 0) {
							iblockstate = Blocks.AIR;
							iblockstate3 = Blocks.STONE;
						} else if (j1 >= i2 - 4 && j1 <= i2 + 1) {
							iblockstate = Blocks.HARDENED_CLAY;
							iblockstate3 = Blocks.HARDENED_CLAY;
						}

						if (j1 < i2 && Blocks.isAir(iblockstate)) {
							iblockstate = Blocks.WATER;
						}

						l = k + Math.max(0, j1 - i2);

						if (j1 >= i2 - 1) {
							if (hasForest && j1 > 86 + k * 2) {
								if (flag) {
									chunk.set(l1, j1, k1, Blocks.DIRT);
								} else {
									chunk.set(l1, j1, k1, Blocks.GRASS);
								}
							} else if (j1 > i2 + 3 + k) {
								int iblockstate2;

								iblockstate2 = Blocks.HARDENED_CLAY;

								chunk.set(l1, j1, k1, iblockstate2);
							} else {
								chunk.set(l1, j1, k1, Blocks.SAND);
							}
						} else {
							chunk.set(l1, j1, k1, iblockstate3);

							if (iblockstate3 == Blocks.HARDENED_CLAY) {
								chunk.set(l1, j1, k1, Blocks.HARDENED_CLAY);
							}
						}
					} else if (l > 0) {
						--l;

						chunk.set(l1, j1, k1, Blocks.HARDENED_CLAY);
					}

					++i1;
				}
			}
		}
	}

	public static enum EnumType {
		OCEAN, PLAINS, DESERT, HILLS, FOREST, TAIGA, SWAMP, RIVER, HELL, END, SNOW, MUSHROOM_ISLAND, BEACH, JUNGLE, STONE_BEACH, SAVANNA, MESA, VOID
	}

	public static enum EnumTempCategory {
		OCEAN, COLD, MEDIUM, WARM
	}

}
