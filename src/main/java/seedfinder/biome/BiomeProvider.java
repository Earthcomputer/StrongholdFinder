package seedfinder.biome;

import java.util.Random;
import java.util.Set;

import seedfinder.BlockPos;
import seedfinder.IntCache;

public class BiomeProvider {

	private static GenLayer biomeGenerator = GenLayer.initializeBiomeGenerator();

	private BiomeProvider() {
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
		int[] aint = biomeGenerator.getValues(genStartX, genStartZ, genWidth, genHeight);

		BlockPos pos = null;
		int rarity = 0;

		for (int index = 0; index < genWidth * genHeight; ++index) {
			int posX = genStartX + index % genWidth << 2;
			int posZ = genStartZ + index / genWidth << 2;

			if (allowedBiomes.contains(aint[index]) && (pos == null || rand.nextInt(rarity + 1) == 0)) {
				pos = new BlockPos(posX, 0, posZ);
				rarity++;
			}
		}

		return pos;
	}

	public static void setWorldSeed(long seed) {
		biomeGenerator.initWorldSeed(seed);
	}

}
