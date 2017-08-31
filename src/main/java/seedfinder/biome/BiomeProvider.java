package seedfinder.biome;

import java.util.Random;
import java.util.Set;

import seedfinder.util.BlockPos;
import seedfinder.util.IntCache;

public class BiomeProvider {

	private static GenLayer biomeGenerator = GenLayer.initializeBiomeGenerator();
	private static GenLayer finalBiomeGenerator = new GenLayerVoronoiZoom(10, biomeGenerator);

	private BiomeProvider() {
	}

	public static int[] getBiomes(int[] biomes, int x, int z, int width, int height) {
		IntCache.free();

		if (biomes == null || biomes.length < width * height) {
			biomes = new int[width * height];
		}

		int[] values = finalBiomeGenerator.getValues(x, z, width, height);
		System.arraycopy(values, 0, biomes, 0, width * height);

		return biomes;
	}

	public static int[] getBiomesForGeneration(int[] biomes, int x, int z, int width, int height) {
		IntCache.free();

		if (biomes == null || biomes.length < width * height) {
			biomes = new int[width * height];
		}

		int[] values = biomeGenerator.getValues(x, z, width, height);
		System.arraycopy(values, 0, biomes, 0, width * height);

		return biomes;
	}

	public static BlockPos findBiomePosition(int x, int z, int range, Set<Integer> allowedBiomes, Random rand) {
		IntCache.free();

		int genStartX = x - range >> 2;
		int genStartZ = z - range >> 2;
		int getEndX = x + range >> 2;
		int genEndZ = z + range >> 2;
		int genWidth = getEndX - genStartX + 1;
		int genHeight = genEndZ - genStartZ + 1;
		int[] values = biomeGenerator.getValues(genStartX, genStartZ, genWidth, genHeight);

		BlockPos pos = null;
		int rarity = 0;

		for (int index = 0; index < genWidth * genHeight; index++) {
			int posX = genStartX + index % genWidth << 2;
			int posZ = genStartZ + index / genWidth << 2;

			if (allowedBiomes.contains(values[index]) && (pos == null || rand.nextInt(rarity + 1) == 0)) {
				pos = new BlockPos(posX, 0, posZ);
				rarity++;
			}
		}

		return pos;
	}

	public static boolean areBiomesViable(int x, int z, int radius, Set<Integer> allowed) {
		IntCache.free();

		int genStartX = x - radius >> 2;
		int genStartZ = z - radius >> 2;
		int genEndX = x + radius >> 2;
		int genEndZ = z + radius >> 2;
		int genWidth = genEndX - genStartX + 1;
		int genHeight = genEndZ - genStartZ + 1;
		int[] values = biomeGenerator.getValues(genStartX, genStartZ, genWidth, genHeight);

		for (int i = 0; i < genWidth * genHeight; i++) {
			if (!allowed.contains(values[i])) {
				return false;
			}
		}

		return true;
	}

	public static void setWorldSeed(long seed) {
		biomeGenerator.initWorldSeed(seed);
		finalBiomeGenerator.initWorldSeed(seed);
	}

}
