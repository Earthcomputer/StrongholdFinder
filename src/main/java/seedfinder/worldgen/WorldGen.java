package seedfinder.worldgen;

import java.util.Random;

import seedfinder.Blocks;
import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;
import seedfinder.structure.MineshaftFinder;
import seedfinder.structure.StrongholdFinder;
import seedfinder.structure.VillageFinder;
import seedfinder.task.DoneEnoughException;
import seedfinder.util.AABB;
import seedfinder.util.ChunkPos;
import seedfinder.util.MathHelper;
import seedfinder.util.Storage3D;

public class WorldGen {

	private static int[] biomesForGeneration;
	private static final double[] heightMap = new double[825];
	private static final float[] biomeWeights = new float[25];
	private static double[] depthRegion;
	private static double[] mainNoiseRegion;
	private static double[] minLimitRegion;
	private static double[] maxLimitRegion;
	private static double[] depthBuffer = new double[256];

	private static NoiseGeneratorOctaves minLimitPerlinNoise;
	private static NoiseGeneratorOctaves maxLimitPerlinNoise;
	private static NoiseGeneratorOctaves mainPerlinNoise;
	private static NoiseGeneratorPerlin surfaceNoise;
	@SuppressWarnings("unused") // in case we need to use it later
	private static NoiseGeneratorOctaves scaleNoise;
	private static NoiseGeneratorOctaves depthNoise;
	@SuppressWarnings("unused") // in case we need to use it later
	private static NoiseGeneratorOctaves forestNoise;
	public static NoiseGeneratorPerlin mesaPillarNoise;
	public static NoiseGeneratorPerlin mesaPillarRoofNoise;

	private WorldGen() {
	}

	static {
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				biomeWeights[x + 2 + (z + 2) * 5] = 10.0F / (float) Math.sqrt(x * x + z * z + 0.2F);
			}
		}
	}

	public static void setWorldSeed(Random rand, long seed) {
		rand.setSeed(seed);
		minLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
		maxLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
		mainPerlinNoise = new NoiseGeneratorOctaves(rand, 8);
		surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
		scaleNoise = new NoiseGeneratorOctaves(rand, 10);
		depthNoise = new NoiseGeneratorOctaves(rand, 16);
		forestNoise = new NoiseGeneratorOctaves(rand, 8);
		rand.setSeed(seed);
		mesaPillarNoise = new NoiseGeneratorPerlin(rand, 4);
		mesaPillarRoofNoise = new NoiseGeneratorPerlin(rand, 1);
	}

	public static void createOverworld(Random rand, long seed, int x, int z, Storage3D chunk) {
		ChunkPos pos = new ChunkPos(x, z);
		chunk.moveAll(x * -16, 0, z * -16);

		rand.setSeed(x * 341873128712L + z * 132897987541L);
		setBlocksInChunk(x, z, chunk);
		biomesForGeneration = BiomeProvider.getBiomesForGeneration(biomesForGeneration, x * 16, z * 16, 16, 16);
		replaceBiomeBlocks(rand, x, z, chunk, biomesForGeneration);

		CaveGen.generate(rand, seed, x, z, chunk);
		RavineGen.generate(rand, seed, x, z, chunk);

		MineshaftFinder.INSTANCE.findStructurePositionsAffectingChunk(rand, seed, pos);
		VillageFinder.INSTANCE.findStructurePositionsAffectingChunk(rand, seed, pos);
		StrongholdFinder.INSTANCE.findStructurePositionsAffectingChunk(rand, seed, pos);

		chunk.moveAll(x * 16, 0, z * 16);
	}

	public static void populateOverworld(Random rand, long seed, int x, int z, Storage3D chunk) {
		setSeedForPopulation(rand, seed, x, z);

		MineshaftFinder.INSTANCE.populate(chunk, rand, seed, x, z);
		VillageFinder.INSTANCE.populate(chunk, rand, seed, x, z);
		StrongholdFinder.INSTANCE.populate(chunk, rand, seed, x, z);
	}

	public static void createAndPopulatePosOverworld(Storage3D world, Random rand, long seed, int blockX, int blockZ) {
		createAndPopulateBBOverworld(world, rand, seed, new AABB(blockX, 0, blockZ, blockX, 255, blockZ));
	}

	public static void createAndPopulateBBOverworld(Storage3D world, Random rand, long seed, AABB bounds) {
		int minChunkX = bounds.getMinX() - 8 >> 4;
		int minChunkZ = bounds.getMinZ() - 8 >> 4;
		int maxChunkX = bounds.getMaxX() - 8 >> 4;
		int maxChunkZ = bounds.getMaxZ() - 8 >> 4;

		world.eraseAndAllocate(minChunkX << 4, 0, minChunkZ << 4, (maxChunkX + 1 << 4) + 15, 255,
				(maxChunkZ + 1 << 4) + 15);

		for (int chunkX = minChunkX; chunkX <= maxChunkX + 1; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ + 1; chunkZ++) {
				createOverworld(rand, seed, chunkX, chunkZ, world);
			}
		}

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				try {
					populateOverworld(rand, seed, chunkX, chunkZ, world);
				} catch (DoneEnoughException e) {
				}
			}
		}
	}

	private static void setBlocksInChunk(int x, int z, Storage3D chunk) {
		biomesForGeneration = BiomeProvider.getBiomesForGeneration(biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
		generateHeightmap(x * 4, 0, z * 4);

		for (int highX = 0; highX < 4; highX++) {
			int flatXIndexHere = highX * 5;
			int flatXIndexRight = (highX + 1) * 5;

			for (int highZ = 0; highZ < 4; highZ++) {
				int indexTopLeft = (flatXIndexHere + highZ) * 33;
				int indexBottomLeft = (flatXIndexHere + highZ + 1) * 33;
				int indexTopRight = (flatXIndexRight + highZ) * 33;
				int indexBottomRight = (flatXIndexRight + highZ + 1) * 33;

				for (int highY = 0; highY < 32; highY++) {
					final double yIncrementMultiplier = 0.125D;
					double densityTopLeft = heightMap[indexTopLeft + highY];
					double densityBottomLeft = heightMap[indexBottomLeft + highY];
					double densityTopRight = heightMap[indexTopRight + highY];
					double densityBottomRight = heightMap[indexBottomRight + highY];
					double topLeftIncrement = (heightMap[indexTopLeft + highY + 1] - densityTopLeft)
							* yIncrementMultiplier;
					double bottomLeftIncrement = (heightMap[indexBottomLeft + highY + 1] - densityBottomLeft)
							* yIncrementMultiplier;
					double topRightIncrement = (heightMap[indexTopRight + highY + 1] - densityTopRight)
							* yIncrementMultiplier;
					double bottomRightIncrement = (heightMap[indexBottomRight + highY + 1] - densityBottomRight)
							* yIncrementMultiplier;

					for (int lowY = 0; lowY < 8; lowY++) {
						final double xIncrementMultiplier = 0.25D;
						double densityTop = densityTopLeft;
						double densityBottom = densityBottomLeft;
						double densityTopIncrement = (densityTopRight - densityTopLeft) * xIncrementMultiplier;
						double densityBottomIncrement = (densityBottomRight - densityBottomLeft) * xIncrementMultiplier;

						for (int lowX = 0; lowX < 4; lowX++) {
							final double zIncrementMultiplier = 0.25D;
							double densityIncrement = (densityBottom - densityTop) * zIncrementMultiplier;
							double density = densityTop - densityIncrement;

							for (int lowZ = 0; lowZ < 4; lowZ++) {
								if ((density += densityIncrement) > 0.0D) {
									chunk.set(highX * 4 + lowX, highY * 8 + lowY, highZ * 4 + lowZ, Blocks.STONE);
								} else if (highY * 8 + lowY < 63) {
									chunk.set(highX * 4 + lowX, highY * 8 + lowY, highZ * 4 + lowZ, Blocks.WATER);
								}
							}

							densityTop += densityTopIncrement;
							densityBottom += densityBottomIncrement;
						}

						densityTopLeft += topLeftIncrement;
						densityBottomLeft += bottomLeftIncrement;
						densityTopRight += topRightIncrement;
						densityBottomRight += bottomRightIncrement;
					}
				}
			}
		}
	}

	private static void generateHeightmap(int x, int y, int z) {
		depthRegion = depthNoise.generateNoiseOctaves(depthRegion, x, z, 5, 5, 200.0, 200.0, 0.5);
		final float coordScale = 684.412F;
		final float heightScale = 684.412F;
		mainNoiseRegion = mainPerlinNoise.generateNoiseOctaves(mainNoiseRegion, x, y, z, 5, 33, 5, coordScale / 80,
				heightScale / 160, coordScale / 80);
		minLimitRegion = minLimitPerlinNoise.generateNoiseOctaves(minLimitRegion, x, y, z, 5, 33, 5, coordScale,
				heightScale, coordScale);
		maxLimitRegion = maxLimitPerlinNoise.generateNoiseOctaves(maxLimitRegion, x, y, z, 5, 33, 5, coordScale,
				heightScale, coordScale);
		int heightMapIdx = 0;
		int depthRegionIdx = 0;

		for (int highX = 0; highX < 5; highX++) {
			for (int highZ = 0; highZ < 5; highZ++) {
				float weightedAvgVariation = 0.0F;
				float weightedAvgHeight = 0.0F;
				float totalWeight = 0.0F;
				int middleBiome = biomesForGeneration[highX + 2 + (highZ + 2) * 10];

				for (int lowX = -2; lowX <= 2; lowX++) {
					for (int lowZ = -2; lowZ <= 2; lowZ++) {
						int biome = biomesForGeneration[highX + lowX + 2 + (highZ + lowZ + 2) * 10];
						float baseHeight = Biomes.getBaseHeight(biome);
						float heightVariation = Biomes.getHeightVariation(biome);

						float biomeWeight = biomeWeights[lowX + 2 + (lowZ + 2) * 5] / (baseHeight + 2.0F);

						if (Biomes.getBaseHeight(biome) > Biomes.getBaseHeight(middleBiome)) {
							biomeWeight /= 2.0F;
						}

						weightedAvgVariation += heightVariation * biomeWeight;
						weightedAvgHeight += baseHeight * biomeWeight;
						totalWeight += biomeWeight;
					}
				}

				weightedAvgVariation = weightedAvgVariation / totalWeight;
				weightedAvgHeight = weightedAvgHeight / totalWeight;
				weightedAvgVariation = weightedAvgVariation * 0.9F + 0.1F;
				weightedAvgHeight = (weightedAvgHeight * 4.0F - 1.0F) / 8.0F;
				double heightOffset = depthRegion[depthRegionIdx] / 8000.0D;

				if (heightOffset < 0.0D) {
					heightOffset = -heightOffset * 0.3D;
				}

				heightOffset = heightOffset * 3.0D - 2.0D;

				if (heightOffset < 0.0D) {
					heightOffset = heightOffset / 2.0D;

					if (heightOffset < -1.0D) {
						heightOffset = -1.0D;
					}

					heightOffset = heightOffset / 1.4D;
					heightOffset = heightOffset / 2.0D;
				} else {
					if (heightOffset > 1.0D) {
						heightOffset = 1.0D;
					}

					heightOffset = heightOffset / 8.0D;
				}

				depthRegionIdx++;
				double biomeHeight = weightedAvgHeight;
				double biomeHeightVariation = weightedAvgVariation;
				biomeHeight = biomeHeight + heightOffset * 0.2D;
				biomeHeight = biomeHeight * 8.5 / 8.0D;
				double surface = 8.5 + biomeHeight * 4.0D;

				for (int highY = 0; highY < 33; highY++) {
					double offsetFromSurface = (highY - surface) * 12.0 * 128.0D / 256.0D / biomeHeightVariation;

					if (offsetFromSurface < 0.0D) {
						offsetFromSurface *= 4.0D;
					}

					double minDensity = minLimitRegion[heightMapIdx] / 512;
					double maxDensity = maxLimitRegion[heightMapIdx] / 512;
					double densitySlide = (mainNoiseRegion[heightMapIdx] / 10.0D + 1.0D) / 2.0D;
					double density = MathHelper.clampedLerp(minDensity, maxDensity, densitySlide) - offsetFromSurface;

					if (highY > 29) {
						double overrideWeight = (highY - 29) / 3.0F;
						density = density * (1.0D - overrideWeight) + -10.0D * overrideWeight;
					}

					heightMap[heightMapIdx] = density;
					heightMapIdx++;
				}
			}
		}
	}

	private static void replaceBiomeBlocks(Random rand, int x, int z, Storage3D chunk, int[] biomes) {
		depthBuffer = surfaceNoise.getRegion(depthBuffer, x * 16, z * 16, 16, 16, 0.0625, 0.0625, 1);

		for (int dz = 0; dz < 16; dz++) {
			for (int dx = 0; dx < 16; dx++) {
				Biomes.genTerrainBlocks(chunk, rand, x * 16 + dz, z * 16 + dx, depthBuffer[dx + dz * 16],
						biomes[dx + dz * 16]);
			}
		}
	}

	/**
	 * Sets a random seed used for structure layout, called individually by each
	 * structure generator
	 */
	public static void setMapGenSeedForChunk(Random rand, long worldSeed, int chunkX, int chunkZ) {
		rand.setSeed(worldSeed);
		long a = rand.nextLong();
		long b = rand.nextLong();
		rand.setSeed(chunkX * a ^ chunkZ * b ^ worldSeed);
	}

	/**
	 * Sets a random seed based on the inputs. Equivalent to the Minecraft
	 * method <tt>World.setRandomSeed(int, int, int)</tt>
	 */
	public static void setRandomSeed(Random rand, long worldSeed, int chunkX, int chunkZ, int uniquifier) {
		rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L + worldSeed + uniquifier);
	}

	/**
	 * Called at the beginning of each chunk population
	 */
	public static void setSeedForPopulation(Random rand, long worldSeed, int chunkX, int chunkZ) {
		rand.setSeed(worldSeed);
		long a = rand.nextLong() / 2 * 2 + 1;
		long b = rand.nextLong() / 2 * 2 + 1;
		rand.setSeed(chunkX * a + chunkZ * b ^ worldSeed);
	}

}
