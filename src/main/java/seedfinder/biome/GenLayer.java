package seedfinder.biome;

public abstract class GenLayer {

	/*
	 * GenLayer uses the LCG values of MMIX; the modulus, 2^64, is implied by
	 * using the long data type
	 */
	private static final long MULTIPLIER = 6364136223846793005L;
	private static final long INCREMENT = 1442695040888963407L;

	private long worldSeed;
	protected GenLayer parent;
	private long chunkSeed;
	protected long uniquifier;

	public GenLayer(long uniquifier) {
		this.uniquifier = uniquifier;
		this.uniquifier *= this.uniquifier * MULTIPLIER + INCREMENT;
		this.uniquifier += uniquifier;
		this.uniquifier *= this.uniquifier * MULTIPLIER + INCREMENT;
		this.uniquifier += uniquifier;
		this.uniquifier *= this.uniquifier * MULTIPLIER + INCREMENT;
		this.uniquifier += uniquifier;
	}

	public static GenLayer initializeBiomeGenerator() {
		GenLayer layer = new GenLayerIsland(1);
		layer = new GenLayerFuzzyZoom(2000, layer);
		layer = new GenLayerAddIsland(1, layer);
		layer = new GenLayerZoom(2001, layer);
		layer = new GenLayerAddIsland(2, layer);
		layer = new GenLayerAddIsland(50, layer);
		layer = new GenLayerAddIsland(70, layer);
		layer = new GenLayerRemoveTooMuchOcean(2, layer);
		layer = new GenLayerAddSnow(2, layer);
		layer = new GenLayerAddIsland(3, layer);
		layer = new GenLayerCoolWarmEdge(2, layer);
		layer = new GenLayerHeatIceEdge(2, layer);
		layer = new GenLayerSpecialEdge(3, layer);
		layer = new GenLayerZoom(2002, layer);
		layer = new GenLayerZoom(2003, layer);
		layer = new GenLayerAddIsland(4, layer);
		layer = new GenLayerAddMushroomIsland(5, layer);
		layer = new GenLayerDeepOcean(4, layer);
		layer = GenLayerZoom.magnify(1000, layer, 0);

		GenLayer randomValuesLayer = GenLayerZoom.magnify(1000, layer, 0);
		randomValuesLayer = new GenLayerRandomValues(100, randomValuesLayer);

		GenLayer biomeLayer = new GenLayerBiome(200, layer);
		biomeLayer = GenLayerZoom.magnify(1000, biomeLayer, 2);
		biomeLayer = new GenLayerBiomeEdge(1000, biomeLayer);
		GenLayer tmp = GenLayerZoom.magnify(1000, randomValuesLayer, 2);
		biomeLayer = new GenLayerHills(1000, biomeLayer, tmp);

		GenLayer riverLayer = GenLayerZoom.magnify(1000, randomValuesLayer, 2);
		riverLayer = GenLayerZoom.magnify(1000, riverLayer, 4);
		riverLayer = new GenLayerRiver(1, riverLayer);
		riverLayer = new GenLayerSmooth(1000, riverLayer);

		biomeLayer = new GenLayerAddMutatedPlains(1001, biomeLayer);
		biomeLayer = new GenLayerZoom(1000, biomeLayer);
		biomeLayer = new GenLayerAddIsland(3, biomeLayer);
		biomeLayer = new GenLayerZoom(1001, biomeLayer);
		biomeLayer = new GenLayerShore(1000, biomeLayer);
		biomeLayer = new GenLayerZoom(1002, biomeLayer);
		biomeLayer = new GenLayerZoom(1003, biomeLayer);
		biomeLayer = new GenLayerSmooth(1000, biomeLayer);
		biomeLayer = new GenLayerRiverMix(100, biomeLayer, riverLayer);

		return biomeLayer;
	}

	public void initWorldSeed(long seed) {
		worldSeed = seed;

		if (parent != null) {
			parent.initWorldSeed(seed);
		}

		worldSeed *= worldSeed * MULTIPLIER + INCREMENT;
		worldSeed += uniquifier;
		worldSeed *= worldSeed * MULTIPLIER + INCREMENT;
		worldSeed += uniquifier;
		worldSeed *= worldSeed * MULTIPLIER + INCREMENT;
		worldSeed += uniquifier;
	}

	public void initChunkSeed(long x, long z) {
		chunkSeed = worldSeed;
		chunkSeed *= chunkSeed * MULTIPLIER + INCREMENT;
		chunkSeed += x;
		chunkSeed *= chunkSeed * MULTIPLIER + INCREMENT;
		chunkSeed += z;
		chunkSeed *= chunkSeed * MULTIPLIER + INCREMENT;
		chunkSeed += x;
		chunkSeed *= chunkSeed * MULTIPLIER + INCREMENT;
		chunkSeed += z;
	}

	protected int nextInt(int max) {
		int num = (int) ((chunkSeed >> 24) % max);
		if (num < 0) {
			num += max;
		}

		chunkSeed *= chunkSeed * MULTIPLIER + INCREMENT;
		chunkSeed += worldSeed;

		return num;
	}

	protected int choose(int... values) {
		return values[nextInt(values.length)];
	}

	protected int modeOrRandom(int a, int b, int c, int d) {
		// a = b = c
		if (a == b && a == c) {
			return a;
		}
		// a = b = d
		if (a == b && a == d) {
			return a;
		}
		// a = c = d
		if (a == c && a == d) {
			return a;
		}
		// a = b
		if (a == b && c != d) {
			return a;
		}
		// a = c
		if (a == c && b != d) {
			return a;
		}
		// a = d
		if (a == d && b != c) {
			return a;
		}
		// b = c
		if (b == c && a != d) {
			return b;
		}
		// b = d
		if (b == d && a != c) {
			return b;
		}
		// c = d
		if (c == d && a != b) {
			return c;
		}
		return choose(a, b, c, d);
	}

	public abstract int[] getValues(int x, int z, int width, int height);

}
