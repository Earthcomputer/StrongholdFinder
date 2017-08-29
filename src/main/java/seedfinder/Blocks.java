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
			LOG = 17,
			LEAVES = 18,
			SANDSTONE = 24,
			WEB = 30,
			WOOL = 35,
			DOUBLE_STONE_SLAB = 43,
			STONE_SLAB = 44,
			BOOKSHELF = 47,
			TORCH = 50,
			MOB_SPAWNER = 52,
			OAK_STAIRS = 53,
			CHEST = 54,
			CRAFTING_TABLE = 58,
			WHEAT = 59,
			FARMLAND = 60,
			FURNACE = 61,
			OAK_DOOR = 64,
			LADDER = 65,
			RAIL = 66,
			STONE_STAIRS = 67,
			IRON_DOOR = 71,
			WOODEN_PRESSURE_PLATE = 72,
			STONE_BUTTON = 77,
			SNOW_LAYER = 78,
			ICE = 79,
			SNOW = 80,
			OAK_FENCE = 85,
			STONEBRICK = 98,
			IRON_BARS = 101,
			GLASS_PANE = 102,
			STONE_BRICK_STAIRS = 109,
			MYCELIUM = 110,
			WATERLILY = 111,
			END_PORTAL = 119,
			END_PORTAL_FRAME = 120,
			SANDSTONE_STAIRS = 128,
			SPRUCE_STAIRS = 134,
			CARROTS = 141,
			POTATOES = 142,
			LEAVES2 = 161,
			LOG2 = 162,
			ACACIA_STAIRS = 163,
			HARDENED_CLAY = 172,
			RED_SANDSTONE = 179,
			SPRUCE_FENCE = 188,
			DARK_OAK_FENCE = 191,
			ACACIA_FENCE = 192,
			SPRUCE_DOOR = 193,
			ACACIA_DOOR = 196,
			BEETROOTS = 207,
			GRASS_PATH = 208;
	// @formatter:on

	private static final boolean[] OPAQUE_CUBE = new boolean[256];
	private static final int[] OPACITY = new int[256];
	private static final boolean[] BLOCKS_MOVEMENT = new boolean[256];
	static {
		OPAQUE_CUBE[AIR] = false;
		OPAQUE_CUBE[STONE] = true;
		OPAQUE_CUBE[GRASS] = true;
		OPAQUE_CUBE[DIRT] = true;
		OPAQUE_CUBE[COBBLESTONE] = true;
		OPAQUE_CUBE[PLANKS] = true;
		OPAQUE_CUBE[BEDROCK] = true;
		OPAQUE_CUBE[FLOWING_WATER] = false;
		OPAQUE_CUBE[WATER] = false;
		OPAQUE_CUBE[FLOWING_LAVA] = false;
		OPAQUE_CUBE[LAVA] = false;
		OPAQUE_CUBE[SAND] = true;
		OPAQUE_CUBE[GRAVEL] = true;
		OPAQUE_CUBE[LOG] = true;
		OPAQUE_CUBE[LEAVES] = false;
		OPAQUE_CUBE[SANDSTONE] = true;
		OPAQUE_CUBE[WEB] = false;
		OPAQUE_CUBE[WOOL] = true;
		OPAQUE_CUBE[DOUBLE_STONE_SLAB] = true;
		OPAQUE_CUBE[STONE_SLAB] = false;
		OPAQUE_CUBE[BOOKSHELF] = true;
		OPAQUE_CUBE[TORCH] = false;
		OPAQUE_CUBE[MOB_SPAWNER] = false;
		OPAQUE_CUBE[OAK_STAIRS] = false;
		OPAQUE_CUBE[CHEST] = false;
		OPAQUE_CUBE[CRAFTING_TABLE] = true;
		OPAQUE_CUBE[WHEAT] = false;
		OPAQUE_CUBE[FARMLAND] = false;
		OPAQUE_CUBE[FURNACE] = true;
		OPAQUE_CUBE[OAK_DOOR] = false;
		OPAQUE_CUBE[LADDER] = false;
		OPAQUE_CUBE[RAIL] = false;
		OPAQUE_CUBE[STONE_STAIRS] = false;
		OPAQUE_CUBE[IRON_DOOR] = false;
		OPAQUE_CUBE[WOODEN_PRESSURE_PLATE] = false;
		OPAQUE_CUBE[STONE_BUTTON] = false;
		OPAQUE_CUBE[SNOW_LAYER] = false;
		OPAQUE_CUBE[ICE] = false;
		OPAQUE_CUBE[SNOW] = true;
		OPAQUE_CUBE[OAK_FENCE] = false;
		OPAQUE_CUBE[STONEBRICK] = true;
		OPAQUE_CUBE[IRON_BARS] = false;
		OPAQUE_CUBE[GLASS_PANE] = false;
		OPAQUE_CUBE[STONE_BRICK_STAIRS] = false;
		OPAQUE_CUBE[MYCELIUM] = true;
		OPAQUE_CUBE[WATERLILY] = false;
		OPAQUE_CUBE[END_PORTAL] = false;
		OPAQUE_CUBE[END_PORTAL_FRAME] = false;
		OPAQUE_CUBE[SANDSTONE_STAIRS] = false;
		OPAQUE_CUBE[SPRUCE_STAIRS] = false;
		OPAQUE_CUBE[CARROTS] = false;
		OPAQUE_CUBE[POTATOES] = false;
		OPAQUE_CUBE[LEAVES2] = false;
		OPAQUE_CUBE[LOG2] = true;
		OPAQUE_CUBE[ACACIA_STAIRS] = false;
		OPAQUE_CUBE[HARDENED_CLAY] = true;
		OPAQUE_CUBE[RED_SANDSTONE] = true;
		OPAQUE_CUBE[SPRUCE_FENCE] = false;
		OPAQUE_CUBE[DARK_OAK_FENCE] = false;
		OPAQUE_CUBE[ACACIA_FENCE] = false;
		OPAQUE_CUBE[SPRUCE_DOOR] = false;
		OPAQUE_CUBE[ACACIA_DOOR] = false;
		OPAQUE_CUBE[BEETROOTS] = false;
		OPAQUE_CUBE[GRASS_PATH] = false;

		for (int block = 0; block < 256; block++) {
			OPACITY[block] = OPAQUE_CUBE[block] ? 255 : 0;
		}
		OPACITY[FLOWING_WATER] = 3;
		OPACITY[WATER] = 3;
		OPACITY[WEB] = 1;
		OPACITY[SNOW_LAYER] = 0;
		OPACITY[ICE] = 3;
		OPACITY[FARMLAND] = 255;
		OPACITY[GRASS_PATH] = 255;
		OPACITY[LEAVES] = 1;
		OPACITY[LEAVES2] = 1;
		OPACITY[STONE_SLAB] = 255;
		OPACITY[STONE_STAIRS] = 255;
		OPACITY[STONE_BRICK_STAIRS] = 255;
		OPACITY[OAK_STAIRS] = 255;
		OPACITY[SPRUCE_STAIRS] = 255;
		OPACITY[ACACIA_STAIRS] = 255;
		OPACITY[SANDSTONE_STAIRS] = 255;

		BLOCKS_MOVEMENT[AIR] = false;
		BLOCKS_MOVEMENT[STONE] = true;
		BLOCKS_MOVEMENT[GRASS] = true;
		BLOCKS_MOVEMENT[DIRT] = true;
		BLOCKS_MOVEMENT[COBBLESTONE] = true;
		BLOCKS_MOVEMENT[PLANKS] = true;
		BLOCKS_MOVEMENT[BEDROCK] = true;
		BLOCKS_MOVEMENT[FLOWING_WATER] = false;
		BLOCKS_MOVEMENT[WATER] = false;
		BLOCKS_MOVEMENT[FLOWING_LAVA] = false;
		BLOCKS_MOVEMENT[LAVA] = false;
		BLOCKS_MOVEMENT[SAND] = true;
		BLOCKS_MOVEMENT[GRAVEL] = true;
		BLOCKS_MOVEMENT[LOG] = true;
		BLOCKS_MOVEMENT[LEAVES] = true;
		BLOCKS_MOVEMENT[SANDSTONE] = true;
		BLOCKS_MOVEMENT[WEB] = false;
		BLOCKS_MOVEMENT[WOOL] = true;
		BLOCKS_MOVEMENT[DOUBLE_STONE_SLAB] = true;
		BLOCKS_MOVEMENT[STONE_SLAB] = true;
		BLOCKS_MOVEMENT[BOOKSHELF] = true;
		BLOCKS_MOVEMENT[TORCH] = false;
		BLOCKS_MOVEMENT[MOB_SPAWNER] = true;
		BLOCKS_MOVEMENT[OAK_STAIRS] = true;
		BLOCKS_MOVEMENT[CHEST] = true;
		BLOCKS_MOVEMENT[CRAFTING_TABLE] = true;
		BLOCKS_MOVEMENT[WHEAT] = false;
		BLOCKS_MOVEMENT[FARMLAND] = true;
		BLOCKS_MOVEMENT[FURNACE] = true;
		BLOCKS_MOVEMENT[OAK_DOOR] = true;
		BLOCKS_MOVEMENT[LADDER] = false;
		BLOCKS_MOVEMENT[RAIL] = false;
		BLOCKS_MOVEMENT[STONE_STAIRS] = true;
		BLOCKS_MOVEMENT[IRON_DOOR] = true;
		BLOCKS_MOVEMENT[WOODEN_PRESSURE_PLATE] = false;
		BLOCKS_MOVEMENT[STONE_BUTTON] = false;
		BLOCKS_MOVEMENT[SNOW_LAYER] = false;
		BLOCKS_MOVEMENT[ICE] = true;
		BLOCKS_MOVEMENT[SNOW] = true;
		BLOCKS_MOVEMENT[OAK_FENCE] = true;
		BLOCKS_MOVEMENT[STONEBRICK] = true;
		BLOCKS_MOVEMENT[IRON_BARS] = true;
		BLOCKS_MOVEMENT[GLASS_PANE] = true;
		BLOCKS_MOVEMENT[STONE_BRICK_STAIRS] = true;
		BLOCKS_MOVEMENT[MYCELIUM] = true;
		BLOCKS_MOVEMENT[WATERLILY] = false;
		BLOCKS_MOVEMENT[END_PORTAL] = false;
		BLOCKS_MOVEMENT[END_PORTAL_FRAME] = true;
		BLOCKS_MOVEMENT[SANDSTONE_STAIRS] = true;
		BLOCKS_MOVEMENT[SPRUCE_STAIRS] = true;
		BLOCKS_MOVEMENT[CARROTS] = false;
		BLOCKS_MOVEMENT[POTATOES] = false;
		BLOCKS_MOVEMENT[LEAVES2] = true;
		BLOCKS_MOVEMENT[LOG2] = true;
		BLOCKS_MOVEMENT[ACACIA_STAIRS] = true;
		BLOCKS_MOVEMENT[HARDENED_CLAY] = true;
		BLOCKS_MOVEMENT[RED_SANDSTONE] = true;
		BLOCKS_MOVEMENT[SPRUCE_FENCE] = true;
		BLOCKS_MOVEMENT[DARK_OAK_FENCE] = true;
		BLOCKS_MOVEMENT[ACACIA_FENCE] = true;
		BLOCKS_MOVEMENT[SPRUCE_DOOR] = true;
		BLOCKS_MOVEMENT[ACACIA_DOOR] = true;
		BLOCKS_MOVEMENT[BEETROOTS] = false;
		BLOCKS_MOVEMENT[GRASS_PATH] = true;
	}

	public static boolean isAir(int block) {
		return block == AIR;
	}

	public static boolean isLiquid(int block) {
		return block == WATER || block == LAVA || block == FLOWING_WATER || block == FLOWING_LAVA;
	}

	public static boolean isOpaqueCube(int block) {
		if (block < 0 || block >= 256) {
			return false;
		} else {
			return OPAQUE_CUBE[block];
		}
	}
	
	public static int getOpacity(int block) {
		if (block < 0 || block >= 256) {
			return 0;
		} else {
			return OPACITY[block];
		}
	}

	public static boolean blocksMovement(int block) {
		if (block < 0 || block >= 256) {
			return false;
		} else {
			return BLOCKS_MOVEMENT[block];
		}
	}

}
