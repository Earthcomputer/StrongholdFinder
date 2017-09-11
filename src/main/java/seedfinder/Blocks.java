package seedfinder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

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
			COAL_ORE = 16,
			LOG = 17,
			LEAVES = 18,
			DISPENSER = 23,
			SANDSTONE = 24,
			BED = 26,
			STICKY_PISTON = 29,
			WEB = 30,
			WOOL = 35,
			DOUBLE_STONE_SLAB = 43,
			STONE_SLAB = 44,
			TNT = 46,
			BOOKSHELF = 47,
			MOSSY_COBBLESTONE = 48,
			OBSIDIAN = 49,
			TORCH = 50,
			MOB_SPAWNER = 52,
			OAK_STAIRS = 53,
			CHEST = 54,
			REDSTONE_WIRE = 55,
			CRAFTING_TABLE = 58,
			WHEAT = 59,
			FARMLAND = 60,
			FURNACE = 61,
			OAK_DOOR = 64,
			LADDER = 65,
			RAIL = 66,
			STONE_STAIRS = 67,
			WALL_SIGN = 68,
			LEVER = 69,
			STONE_PRESSURE_PLATE = 70,
			IRON_DOOR = 71,
			WOODEN_PRESSURE_PLATE = 72,
			REDSTONE_TORCH = 76,
			STONE_BUTTON = 77,
			SNOW_LAYER = 78,
			ICE = 79,
			SNOW = 80,
			OAK_FENCE = 85,
			UNPOWERED_REPEATER = 93,
			STAINED_GLASS = 95,
			TRAPDOOR = 96,
			MONSTER_EGG = 97,
			STONEBRICK = 98,
			IRON_BARS = 101,
			GLASS_PANE = 102,
			VINE = 105,
			STONE_BRICK_STAIRS = 109,
			MYCELIUM = 110,
			WATERLILY = 111,
			BREWING_STAND = 117,
			CAULDRON = 118,
			END_PORTAL = 119,
			END_PORTAL_FRAME = 120,
			WOODEN_SLAB = 126,
			SANDSTONE_STAIRS = 128,
			ENDER_CHEST = 130,
			TRIPWIRE_HOOK = 131,
			TRIPWIRE = 132,
			SPRUCE_STAIRS = 134,
			FLOWER_POT = 140,
			CARROTS = 141,
			POTATOES = 142,
			SKULL = 144,
			LEAVES2 = 161,
			LOG2 = 162,
			ACACIA_STAIRS = 163,
			CARPET = 171,
			HARDENED_CLAY = 172,
			WALL_BANNER = 177,
			RED_SANDSTONE = 179,
			SPRUCE_FENCE = 188,
			DARK_OAK_FENCE = 191,
			ACACIA_FENCE = 192,
			SPRUCE_DOOR = 193,
			ACACIA_DOOR = 196,
			END_ROD = 198,
			PURPUR_BLOCK = 201,
			PURPUR_PILLAR = 202,
			PURPUR_STAIRS = 203,
			PURPUR_SLAB = 205,
			END_BRICKS = 206,
			BEETROOTS = 207,
			GRASS_PATH = 208,
			BONE_BLOCK = 216,
			STRUCTURE_VOID = 217,
			STRUCTURE_BLOCK = 255;
	// @formatter:on

	private static final String[] NAMES = new String[256];
	private static final Map<String, Integer> BY_NAME = new HashMap<>();
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
		OPAQUE_CUBE[COAL_ORE] = true;
		OPAQUE_CUBE[LOG] = true;
		OPAQUE_CUBE[LEAVES] = false;
		OPAQUE_CUBE[DISPENSER] = true;
		OPAQUE_CUBE[SANDSTONE] = true;
		OPAQUE_CUBE[BED] = false;
		OPAQUE_CUBE[STICKY_PISTON] = false;
		OPAQUE_CUBE[WEB] = false;
		OPAQUE_CUBE[WOOL] = true;
		OPAQUE_CUBE[DOUBLE_STONE_SLAB] = true;
		OPAQUE_CUBE[STONE_SLAB] = false;
		OPAQUE_CUBE[TNT] = true;
		OPAQUE_CUBE[BOOKSHELF] = true;
		OPAQUE_CUBE[MOSSY_COBBLESTONE] = true;
		OPAQUE_CUBE[OBSIDIAN] = true;
		OPAQUE_CUBE[TORCH] = false;
		OPAQUE_CUBE[MOB_SPAWNER] = false;
		OPAQUE_CUBE[OAK_STAIRS] = false;
		OPAQUE_CUBE[CHEST] = false;
		OPAQUE_CUBE[REDSTONE_WIRE] = false;
		OPAQUE_CUBE[CRAFTING_TABLE] = true;
		OPAQUE_CUBE[WHEAT] = false;
		OPAQUE_CUBE[FARMLAND] = false;
		OPAQUE_CUBE[FURNACE] = true;
		OPAQUE_CUBE[OAK_DOOR] = false;
		OPAQUE_CUBE[LADDER] = false;
		OPAQUE_CUBE[RAIL] = false;
		OPAQUE_CUBE[STONE_STAIRS] = false;
		OPAQUE_CUBE[WALL_SIGN] = false;
		OPAQUE_CUBE[LEVER] = false;
		OPAQUE_CUBE[STONE_PRESSURE_PLATE] = false;
		OPAQUE_CUBE[IRON_DOOR] = false;
		OPAQUE_CUBE[WOODEN_PRESSURE_PLATE] = false;
		OPAQUE_CUBE[REDSTONE_TORCH] = false;
		OPAQUE_CUBE[STONE_BUTTON] = false;
		OPAQUE_CUBE[SNOW_LAYER] = false;
		OPAQUE_CUBE[ICE] = false;
		OPAQUE_CUBE[SNOW] = true;
		OPAQUE_CUBE[OAK_FENCE] = false;
		OPAQUE_CUBE[UNPOWERED_REPEATER] = false;
		OPAQUE_CUBE[STAINED_GLASS] = false;
		OPAQUE_CUBE[TRAPDOOR] = false;
		OPAQUE_CUBE[MONSTER_EGG] = true;
		OPAQUE_CUBE[STONEBRICK] = true;
		OPAQUE_CUBE[IRON_BARS] = false;
		OPAQUE_CUBE[GLASS_PANE] = false;
		OPAQUE_CUBE[VINE] = false;
		OPAQUE_CUBE[STONE_BRICK_STAIRS] = false;
		OPAQUE_CUBE[MYCELIUM] = true;
		OPAQUE_CUBE[WATERLILY] = false;
		OPAQUE_CUBE[BREWING_STAND] = false;
		OPAQUE_CUBE[CAULDRON] = false;
		OPAQUE_CUBE[END_PORTAL] = false;
		OPAQUE_CUBE[END_PORTAL_FRAME] = false;
		OPAQUE_CUBE[WOODEN_SLAB] = false;
		OPAQUE_CUBE[SANDSTONE_STAIRS] = false;
		OPAQUE_CUBE[ENDER_CHEST] = false;
		OPAQUE_CUBE[TRIPWIRE_HOOK] = false;
		OPAQUE_CUBE[TRIPWIRE] = false;
		OPAQUE_CUBE[SPRUCE_STAIRS] = false;
		OPAQUE_CUBE[FLOWER_POT] = false;
		OPAQUE_CUBE[CARROTS] = false;
		OPAQUE_CUBE[POTATOES] = false;
		OPAQUE_CUBE[SKULL] = false;
		OPAQUE_CUBE[LEAVES2] = false;
		OPAQUE_CUBE[LOG2] = true;
		OPAQUE_CUBE[ACACIA_STAIRS] = false;
		OPAQUE_CUBE[CARPET] = false;
		OPAQUE_CUBE[HARDENED_CLAY] = true;
		OPAQUE_CUBE[WALL_BANNER] = false;
		OPAQUE_CUBE[RED_SANDSTONE] = true;
		OPAQUE_CUBE[SPRUCE_FENCE] = false;
		OPAQUE_CUBE[DARK_OAK_FENCE] = false;
		OPAQUE_CUBE[ACACIA_FENCE] = false;
		OPAQUE_CUBE[SPRUCE_DOOR] = false;
		OPAQUE_CUBE[ACACIA_DOOR] = false;
		OPAQUE_CUBE[END_ROD] = false;
		OPAQUE_CUBE[PURPUR_BLOCK] = true;
		OPAQUE_CUBE[PURPUR_PILLAR] = true;
		OPAQUE_CUBE[PURPUR_STAIRS] = false;
		OPAQUE_CUBE[PURPUR_SLAB] = false;
		OPAQUE_CUBE[END_BRICKS] = true;
		OPAQUE_CUBE[BEETROOTS] = false;
		OPAQUE_CUBE[GRASS_PATH] = false;
		OPAQUE_CUBE[BONE_BLOCK] = true;
		OPAQUE_CUBE[STRUCTURE_VOID] = false;
		OPAQUE_CUBE[STRUCTURE_BLOCK] = true;

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
		OPACITY[WOODEN_SLAB] = 255;
		OPACITY[SPRUCE_STAIRS] = 255;
		OPACITY[ACACIA_STAIRS] = 255;
		OPACITY[SANDSTONE_STAIRS] = 255;
		OPACITY[PURPUR_STAIRS] = 255;
		OPACITY[PURPUR_SLAB] = 255;

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
		BLOCKS_MOVEMENT[COAL_ORE] = true;
		BLOCKS_MOVEMENT[LOG] = true;
		BLOCKS_MOVEMENT[LEAVES] = true;
		BLOCKS_MOVEMENT[DISPENSER] = true;
		BLOCKS_MOVEMENT[SANDSTONE] = true;
		BLOCKS_MOVEMENT[BED] = true;
		BLOCKS_MOVEMENT[STICKY_PISTON] = true;
		BLOCKS_MOVEMENT[WEB] = false;
		BLOCKS_MOVEMENT[WOOL] = true;
		BLOCKS_MOVEMENT[DOUBLE_STONE_SLAB] = true;
		BLOCKS_MOVEMENT[STONE_SLAB] = true;
		BLOCKS_MOVEMENT[TNT] = true;
		BLOCKS_MOVEMENT[BOOKSHELF] = true;
		BLOCKS_MOVEMENT[MOSSY_COBBLESTONE] = true;
		BLOCKS_MOVEMENT[OBSIDIAN] = true;
		BLOCKS_MOVEMENT[TORCH] = false;
		BLOCKS_MOVEMENT[MOB_SPAWNER] = true;
		BLOCKS_MOVEMENT[OAK_STAIRS] = true;
		BLOCKS_MOVEMENT[CHEST] = true;
		BLOCKS_MOVEMENT[REDSTONE_WIRE] = false;
		BLOCKS_MOVEMENT[CRAFTING_TABLE] = true;
		BLOCKS_MOVEMENT[WHEAT] = false;
		BLOCKS_MOVEMENT[FARMLAND] = true;
		BLOCKS_MOVEMENT[FURNACE] = true;
		BLOCKS_MOVEMENT[OAK_DOOR] = true;
		BLOCKS_MOVEMENT[LADDER] = false;
		BLOCKS_MOVEMENT[RAIL] = false;
		BLOCKS_MOVEMENT[STONE_STAIRS] = true;
		BLOCKS_MOVEMENT[WALL_SIGN] = true;
		BLOCKS_MOVEMENT[LEVER] = false;
		BLOCKS_MOVEMENT[STONE_PRESSURE_PLATE] = false;
		BLOCKS_MOVEMENT[IRON_DOOR] = true;
		BLOCKS_MOVEMENT[WOODEN_PRESSURE_PLATE] = false;
		BLOCKS_MOVEMENT[REDSTONE_TORCH] = false;
		BLOCKS_MOVEMENT[STONE_BUTTON] = false;
		BLOCKS_MOVEMENT[SNOW_LAYER] = false;
		BLOCKS_MOVEMENT[ICE] = true;
		BLOCKS_MOVEMENT[SNOW] = true;
		BLOCKS_MOVEMENT[OAK_FENCE] = true;
		BLOCKS_MOVEMENT[UNPOWERED_REPEATER] = false;
		BLOCKS_MOVEMENT[STAINED_GLASS] = true;
		BLOCKS_MOVEMENT[TRAPDOOR] = true;
		BLOCKS_MOVEMENT[MONSTER_EGG] = true;
		BLOCKS_MOVEMENT[STONEBRICK] = true;
		BLOCKS_MOVEMENT[IRON_BARS] = true;
		BLOCKS_MOVEMENT[GLASS_PANE] = true;
		BLOCKS_MOVEMENT[VINE] = false;
		BLOCKS_MOVEMENT[STONE_BRICK_STAIRS] = true;
		BLOCKS_MOVEMENT[MYCELIUM] = true;
		BLOCKS_MOVEMENT[WATERLILY] = false;
		BLOCKS_MOVEMENT[BREWING_STAND] = true;
		BLOCKS_MOVEMENT[CAULDRON] = true;
		BLOCKS_MOVEMENT[END_PORTAL] = false;
		BLOCKS_MOVEMENT[END_PORTAL_FRAME] = true;
		BLOCKS_MOVEMENT[WOODEN_SLAB] = true;
		BLOCKS_MOVEMENT[SANDSTONE_STAIRS] = true;
		BLOCKS_MOVEMENT[ENDER_CHEST] = true;
		BLOCKS_MOVEMENT[TRIPWIRE_HOOK] = false;
		BLOCKS_MOVEMENT[TRIPWIRE] = false;
		BLOCKS_MOVEMENT[SPRUCE_STAIRS] = true;
		BLOCKS_MOVEMENT[FLOWER_POT] = false;
		BLOCKS_MOVEMENT[CARROTS] = false;
		BLOCKS_MOVEMENT[POTATOES] = false;
		BLOCKS_MOVEMENT[SKULL] = false;
		BLOCKS_MOVEMENT[LEAVES2] = true;
		BLOCKS_MOVEMENT[LOG2] = true;
		BLOCKS_MOVEMENT[ACACIA_STAIRS] = true;
		BLOCKS_MOVEMENT[CARPET] = false;
		BLOCKS_MOVEMENT[HARDENED_CLAY] = true;
		BLOCKS_MOVEMENT[WALL_BANNER] = true;
		BLOCKS_MOVEMENT[RED_SANDSTONE] = true;
		BLOCKS_MOVEMENT[SPRUCE_FENCE] = true;
		BLOCKS_MOVEMENT[DARK_OAK_FENCE] = true;
		BLOCKS_MOVEMENT[ACACIA_FENCE] = true;
		BLOCKS_MOVEMENT[SPRUCE_DOOR] = true;
		BLOCKS_MOVEMENT[ACACIA_DOOR] = true;
		BLOCKS_MOVEMENT[END_ROD] = false;
		BLOCKS_MOVEMENT[PURPUR_BLOCK] = true;
		BLOCKS_MOVEMENT[PURPUR_PILLAR] = true;
		BLOCKS_MOVEMENT[PURPUR_STAIRS] = true;
		BLOCKS_MOVEMENT[PURPUR_SLAB] = true;
		BLOCKS_MOVEMENT[END_BRICKS] = true;
		BLOCKS_MOVEMENT[BEETROOTS] = false;
		BLOCKS_MOVEMENT[GRASS_PATH] = true;
		BLOCKS_MOVEMENT[BONE_BLOCK] = true;
		BLOCKS_MOVEMENT[STRUCTURE_VOID] = false;
		BLOCKS_MOVEMENT[STRUCTURE_BLOCK] = true;

		for (Field field : Blocks.class.getDeclaredFields()) {
			if (field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
				if (field.getType() == int.class) {
					String name = field.getName().toLowerCase();
					int id;
					try {
						id = field.getInt(null);
					} catch (Exception e) {
						throw new Error(e);
					}

					NAMES[id] = name;
					BY_NAME.put(name, id);
				}
			}
		}
	}

	public static boolean isAir(int block) {
		return block == AIR;
	}

	public static boolean isLiquid(int block) {
		return block == WATER || block == LAVA || block == FLOWING_WATER || block == FLOWING_LAVA;
	}

	public static String getName(int block) {
		if (block < 0 || block >= 256) {
			return null;
		} else {
			return NAMES[block];
		}
	}

	public static OptionalInt getByName(String name) {
		if (name.startsWith("minecraft:")) {
			name = name.substring("minecraft:".length());
		}
		Integer id = BY_NAME.get(name);
		return id == null ? OptionalInt.empty() : OptionalInt.of(id);
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
