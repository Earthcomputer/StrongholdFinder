package seedfinder;

public class Blocks {

	private Blocks() {
	}

	// @formatter:off
	public static final int
			AIR = 0,
			STONE = 1,
			GRASS = 2,
			DIRT = 3,
			COBBLESTONE = 4,
			PLANKS = 5,
			BEDROCK = 7,
			FLOWING_WATER = 8,
			WATER = 9,
			FLOWING_LAVA = 10,
			LAVA = 11,
			SAND = 12,
			GRAVEL = 13,
			SANDSTONE = 24,
			DOUBLE_STONE_SLAB = 43,
			STONE_SLAB = 44,
			BOOKSHELF = 47,
			TORCH = 50,
			MOB_SPAWNER = 52,
			CHEST = 54,
			OAK_DOOR = 64,
			LADDER = 65,
			STONE_STAIRS = 67,
			IRON_DOOR = 71,
			STONE_BUTTON = 77,
			SNOW_LAYER = 78,
			ICE = 79,
			SNOW = 80,
			OAK_FENCE = 85,
			STONEBRICK = 98,
			IRON_BARS = 101,
			STONE_BRICK_STAIRS = 109,
			MYCELIUM = 110,
			WATERLILY = 111,
			END_PORTAL = 119,
			END_PORTAL_FRAME = 120,
			HARDENED_CLAY = 172;
	// @formatter:on

	public static boolean isAir(int block) {
		return block == AIR;
	}

	public static boolean isLiquid(int block) {
		return block == WATER || block == LAVA || block == FLOWING_WATER || block == FLOWING_LAVA;
	}

}
