package seedfinder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Enchantments {

	// @formatter:off
	public static final int
			PROTECTION = 0,
			FIRE_PROTECTION = 1,
			FEATHER_FALLING = 2,
			BLAST_PROTECTION = 3,
			PROJECTILE_PROTECTION = 4,
			RESPIRATION = 5,
			AQUA_AFFINITY = 6,
			THORNS = 7,
			DEPTH_STRIDER = 8,
			FROST_WALKER = 9,
			BINDING_CURSE = 10,
			SHARPNESS = 16,
			SMITE = 17,
			BANE_OF_ARTHROPODS = 18,
			KNOCKBACK = 19,
			FIRE_ASPECT = 20,
			LOOTING = 21,
			SWEEPING = 22,
			EFFICIENCY = 32,
			SILK_TOUCH = 33,
			UNBREAKING = 34,
			FORTUNE = 35,
			POWER = 48,
			PUNCH = 49,
			FLAME = 50,
			INFINITY = 51,
			LUCK_OF_THE_SEA = 61,
			LURE = 62,
			MENDING = 70,
			VANISHING_CURSE = 71;
	// @formatter:on

	public static final String[] ENCHANTMENT_NAMES = new String[256];

	// @formatter:off
	public static final int[] ALL_ENCHANTMENTS = {
			// armor
			 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10,
			// sword
			16, 17, 18, 19, 20, 21, 22,
			// tools
			32, 33, 34, 35,
			// bow                                          // fishing rod
			48, 49, 50, 51,                                     61, 62,
			                      // other
			                        70, 71
	};
	// @formatter:on

	private static final Set<Set<Integer>> INCOMPATIBLE_GROUPS = new HashSet<>();

	static {
		Set<Integer> set;

		set = new HashSet<>();
		set.add(INFINITY);
		set.add(MENDING);
		INCOMPATIBLE_GROUPS.add(set);

		set = new HashSet<>();
		set.add(SHARPNESS);
		set.add(SMITE);
		set.add(BANE_OF_ARTHROPODS);
		INCOMPATIBLE_GROUPS.add(set);

		set = new HashSet<>();
		set.add(DEPTH_STRIDER);
		set.add(FROST_WALKER);
		INCOMPATIBLE_GROUPS.add(set);

		set = new HashSet<>();
		set.add(PROTECTION);
		set.add(FIRE_PROTECTION);
		set.add(BLAST_PROTECTION);
		set.add(PROJECTILE_PROTECTION);
		INCOMPATIBLE_GROUPS.add(set);

		set = new HashSet<>();
		set.add(SILK_TOUCH);
		set.add(LOOTING);
		INCOMPATIBLE_GROUPS.add(set);

		set = new HashSet<>();
		set.add(SILK_TOUCH);
		set.add(FORTUNE);
		INCOMPATIBLE_GROUPS.add(set);

		set = new HashSet<>();
		set.add(SILK_TOUCH);
		set.add(LUCK_OF_THE_SEA);
		INCOMPATIBLE_GROUPS.add(set);

		for (Field field : Enchantments.class.getDeclaredFields()) {
			if (field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) {
				if (field.getType() == int.class) {
					String enchantmentName = field.getName().toLowerCase();
					int enchantmentId;
					try {
						enchantmentId = field.getInt(null);
					} catch (Exception e) {
						throw new Error(e);
					}
					ENCHANTMENT_NAMES[enchantmentId] = enchantmentName;
				}
			}
		}
	}

	public static String getName(int enchantment) {
		if (enchantment < 0 || enchantment >= 256) {
			return null;
		} else {
			return ENCHANTMENT_NAMES[enchantment];
		}
	}

	public static boolean canApply(int enchantment, ItemStack stack, boolean primary) {
		String item = stack.getItem();

		if (Items.BOOK.equals(item)) {
			return true;
		}

		switch (enchantment) {
		case PROTECTION:
		case FIRE_PROTECTION:
		case BLAST_PROTECTION:
		case PROJECTILE_PROTECTION:
			return Items.isArmor(item);
		case THORNS:
			return primary ? Items.isChestplate(item) : Items.isArmor(item);
		case FEATHER_FALLING:
		case DEPTH_STRIDER:
		case FROST_WALKER:
			return Items.isBoots(item);
		case RESPIRATION:
		case AQUA_AFFINITY:
			return Items.isHelmet(item);
		case BINDING_CURSE:
			return Items.isArmor(item) || Items.PUMPKIN.equals(item) || Items.ELYTRA.equals(item)
					|| Items.SKULL.equals(item);
		case SHARPNESS:
		case SMITE:
		case BANE_OF_ARTHROPODS:
			return Items.isSword(item) || !primary && Items.isAxe(item);
		case KNOCKBACK:
		case FIRE_ASPECT:
		case LOOTING:
		case SWEEPING:
			return Items.isSword(item);
		case EFFICIENCY:
			return Items.isTool(item) || !primary && Items.SHEARS.equals(item);
		case SILK_TOUCH:
		case FORTUNE:
			return Items.isTool(item);
		case POWER:
		case PUNCH:
		case FLAME:
		case INFINITY:
			return Items.BOW.equals(item);
		case LUCK_OF_THE_SEA:
		case LURE:
			return Items.FISHING_ROD.equals(item);
		case UNBREAKING:
		case MENDING:
			return Items.hasDurability(item);
		case VANISHING_CURSE:
			return Items.hasDurability(item) || Items.PUMPKIN.equals(item) || Items.SKULL.equals(item);
		default:
			throw new IllegalArgumentException("Unknown enchantment: " + enchantment);
		}
	}

	public static boolean isTreasure(int enchantment) {
		return enchantment == FROST_WALKER || enchantment == MENDING || enchantment == BINDING_CURSE
				|| enchantment == VANISHING_CURSE;
	}

	public static int getMaxLevel(int enchantment) {
		switch (enchantment) {
		case SHARPNESS:
		case SMITE:
		case BANE_OF_ARTHROPODS:
		case EFFICIENCY:
		case POWER:
			return 5;
		case PROTECTION:
		case FIRE_PROTECTION:
		case BLAST_PROTECTION:
		case PROJECTILE_PROTECTION:
		case FEATHER_FALLING:
			return 4;
		case THORNS:
		case DEPTH_STRIDER:
		case RESPIRATION:
		case LOOTING:
		case SWEEPING:
		case FORTUNE:
		case LUCK_OF_THE_SEA:
		case LURE:
		case UNBREAKING:
			return 3;
		case FROST_WALKER:
		case KNOCKBACK:
		case FIRE_ASPECT:
		case PUNCH:
			return 2;
		case AQUA_AFFINITY:
		case BINDING_CURSE:
		case SILK_TOUCH:
		case FLAME:
		case INFINITY:
		case MENDING:
		case VANISHING_CURSE:
			return 1;
		default:
			throw new IllegalArgumentException("Unknown enchantment: " + enchantment);
		}
	}

	public static int getMinEnchantability(int enchantment, int level) {
		switch (enchantment) {
		case PROTECTION:
			return 1 + (level - 1) * 11;
		case FIRE_PROTECTION:
			return 10 + (level - 1) * 8;
		case FEATHER_FALLING:
			return 5 + (level - 1) * 6;
		case BLAST_PROTECTION:
			return 5 + (level - 1) * 8;
		case PROJECTILE_PROTECTION:
			return 3 + (level - 1) * 6;
		case RESPIRATION:
			return level * 10;
		case AQUA_AFFINITY:
			return 1;
		case THORNS:
			return 10 + (level - 1) * 20;
		case DEPTH_STRIDER:
			return level * 10;
		case FROST_WALKER:
			return level * 10;
		case BINDING_CURSE:
			return 25;
		case SHARPNESS:
			return 1 + (level - 1) * 11;
		case SMITE:
			return 5 + (level - 1) * 8;
		case BANE_OF_ARTHROPODS:
			return 5 + (level - 1) * 8;
		case KNOCKBACK:
			return 5 + (level - 1) * 20;
		case FIRE_ASPECT:
			return 10 + (level - 1) * 20;
		case LOOTING:
			return 15 + (level - 1) * 9;
		case SWEEPING:
			return 5 + (level - 1) * 9;
		case EFFICIENCY:
			return 1 + (level - 1) * 10;
		case SILK_TOUCH:
			return 15;
		case UNBREAKING:
			return 5 + (level - 1) * 8;
		case FORTUNE:
			return 15 + (level - 1) * 9;
		case POWER:
			return 1 + (level - 1) * 10;
		case PUNCH:
			return 12 + (level - 1) * 20;
		case FLAME:
			return 20;
		case INFINITY:
			return 20;
		case LUCK_OF_THE_SEA:
			return 15 + (level - 1) * 9;
		case LURE:
			return 15 + (level - 1) * 9;
		case MENDING:
			return 25;
		case VANISHING_CURSE:
			return 25;
		default:
			throw new IllegalArgumentException("Unknown enchantment: " + enchantment);
		}
	}

	public static int getMaxEnchantability(int enchantment, int level) {
		switch (enchantment) {
		case PROTECTION:
			return 1 + level * 11;
		case FIRE_PROTECTION:
			return 10 + level * 8;
		case FEATHER_FALLING:
			return 5 + level * 6;
		case BLAST_PROTECTION:
			return 5 + level * 8;
		case PROJECTILE_PROTECTION:
			return 3 + level * 6;
		case RESPIRATION:
			return 30 + level * 10;
		case AQUA_AFFINITY:
			return 41;
		case THORNS:
			return 40 + level * 20;
		case DEPTH_STRIDER:
			return 15 + level * 10;
		case FROST_WALKER:
			return 15 + level * 10;
		case BINDING_CURSE:
			return 50;
		case SHARPNESS:
			return 21 + (level - 1) * 11;
		case SMITE:
			return 25 + (level - 1) * 8;
		case BANE_OF_ARTHROPODS:
			return 25 + (level - 1) * 8;
		case KNOCKBACK:
			return 55 + (level - 1) * 20;
		case FIRE_ASPECT:
			return 40 + level * 20;
		case LOOTING:
			return 65 + (level - 1) * 9;
		case SWEEPING:
			return 20 + (level - 1) * 9;
		case EFFICIENCY:
			return 50 + level * 10;
		case SILK_TOUCH:
			return 65;
		case UNBREAKING:
			return 55 + (level - 1) * 8;
		case FORTUNE:
			return 65 + (level - 1) * 9;
		case POWER:
			return 16 + (level - 1) * 10;
		case PUNCH:
			return 37 + (level - 1) * 20;
		case FLAME:
			return 50;
		case INFINITY:
			return 50;
		case LUCK_OF_THE_SEA:
			return 65 + (level - 1) * 9;
		case LURE:
			return 65 + (level - 1) * 9;
		case MENDING:
			return 75;
		case VANISHING_CURSE:
			return 50;
		default:
			throw new IllegalArgumentException("Unknown enchantment: " + enchantment);
		}
	}

	public static int getWeight(int enchantment) {
		switch (enchantment) {
		case PROTECTION:
		case SHARPNESS:
		case EFFICIENCY:
		case POWER:
			return 10;
		case FIRE_PROTECTION:
		case FEATHER_FALLING:
		case PROJECTILE_PROTECTION:
		case SMITE:
		case BANE_OF_ARTHROPODS:
		case KNOCKBACK:
		case UNBREAKING:
			return 5;
		case BLAST_PROTECTION:
		case RESPIRATION:
		case AQUA_AFFINITY:
		case DEPTH_STRIDER:
		case FROST_WALKER:
		case FIRE_ASPECT:
		case LOOTING:
		case SWEEPING:
		case FORTUNE:
		case PUNCH:
		case FLAME:
		case LUCK_OF_THE_SEA:
		case LURE:
		case MENDING:
			return 2;
		case THORNS:
		case BINDING_CURSE:
		case SILK_TOUCH:
		case INFINITY:
		case VANISHING_CURSE:
			return 1;
		default:
			throw new IllegalArgumentException("Unknown enchantment: " + enchantment);
		}
	}

	public static boolean areCompatible(int enchA, int enchB) {
		// Can't have same enchantment twice
		if (enchA == enchB) {
			return false;
		}

		return INCOMPATIBLE_GROUPS.stream().noneMatch(group -> group.contains(enchA) && group.contains(enchB));
	}

	public static ItemStack addRandomEnchantment(Random rand, ItemStack stack, int level, boolean treasure) {
		String item = stack.getItem();
		int enchantability = Items.getEnchantability(item);
		List<EnchantmentInstance> enchantments = new ArrayList<>();

		if (enchantability > 0) {
			// Modify the enchantment level randomly and according to
			// enchantability
			level = level + 1 + rand.nextInt(enchantability / 4 + 1) + rand.nextInt(enchantability / 4 + 1);
			float percentChange = (rand.nextFloat() + rand.nextFloat() - 1) * 0.15f;
			level += Math.round(level * percentChange);
			if (level < 1) {
				level = 1;
			}

			// Get a list of allowed enchantments with their max allowed levels
			List<EnchantmentInstance> allowedEnchantments = new ArrayList<>();
			for (int enchantment : ALL_ENCHANTMENTS) {
				if ((treasure || !isTreasure(enchantment)) && canApply(enchantment, stack, true)) {
					for (int enchLvl = getMaxLevel(enchantment); enchLvl >= 1; enchLvl--) {
						if (level >= getMinEnchantability(enchantment, enchLvl)
								&& level <= getMaxEnchantability(enchantment, enchLvl)) {
							allowedEnchantments.add(new EnchantmentInstance(enchantment, enchLvl));
							break;
						}
					}
				}
			}

			if (!allowedEnchantments.isEmpty()) {
				// Get first enchantment
				EnchantmentInstance enchantmentInstance = MathHelper.weightedRandom(rand, allowedEnchantments,
						it -> getWeight(it.enchantment));
				enchantments.add(enchantmentInstance);

				// Get optional extra enchantments
				while (rand.nextInt(50) <= level) {
					// Remove incompatible enchantments from allowed list with
					// last enchantment
					int enchantment = enchantmentInstance.enchantment;
					allowedEnchantments.removeIf(it -> !areCompatible(it.enchantment, enchantment));

					if (allowedEnchantments.isEmpty()) {
						// no enchantments left
						break;
					}

					// Get extra enchantment
					enchantmentInstance = MathHelper.weightedRandom(rand, allowedEnchantments,
							it -> getWeight(it.enchantment));
					enchantments.add(enchantmentInstance);

					// Make it less likely for another enchantment to happen
					level /= 2;
				}
			}
		}

		// Turn books into enchanted books
		if (Items.BOOK.equals(item)) {
			stack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		// Apply enchantments to stack
		stack.getEnchantments().addAll(enchantments);

		return stack;
	}

	public static class EnchantmentInstance {
		public final int enchantment;
		public final int level;

		public EnchantmentInstance(int enchantment, int level) {
			this.enchantment = enchantment;
			this.level = level;
		}

		@Override
		public int hashCode() {
			return enchantment + 31 * level;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof EnchantmentInstance && equals((EnchantmentInstance) other);
		}

		public boolean equals(EnchantmentInstance other) {
			return enchantment == other.enchantment && level == other.level;
		}

		@Override
		public String toString() {
			String enchName = getName(enchantment);
			if (level == 1 && getMaxLevel(enchantment) == 1) {
				return enchName;
			}
			String lvlName;
			switch (level) {
			case 1:
				lvlName = "I";
				break;
			case 2:
				lvlName = "II";
				break;
			case 3:
				lvlName = "III";
				break;
			case 4:
				lvlName = "IV";
				break;
			case 5:
				lvlName = "V";
				break;
			default:
				lvlName = String.valueOf(level);
				break;
			}
			return enchName + " " + lvlName;
		}
	}

}
