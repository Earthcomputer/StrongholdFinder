package seedfinder.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.Enchantments;
import seedfinder.ItemStack;
import seedfinder.Items;
import seedfinder.util.MathHelper;

@FunctionalInterface
public interface ILootFunction {

	ItemStack apply(ItemStack stack, Random rand, LootContext context);

	public static ILootFunction setCount(int count) {
		return setCount(new RandomValueRange(count));
	}

	public static ILootFunction setCount(int min, int max) {
		return setCount(new RandomValueRange(min, max));
	}

	public static ILootFunction setCount(RandomValueRange count) {
		return (stack, rand, context) -> {
			stack.setCount(count.nextInt(rand));
			return stack;
		};
	}

	public static ILootFunction setDamage(int damage) {
		return setDamage(new RandomValueRange(damage));
	}

	public static ILootFunction setDamage(int min, int max) {
		return setDamage(new RandomValueRange(min, max));
	}

	public static ILootFunction setDamage(RandomValueRange damage) {
		return (stack, rand, context) -> {
			stack.setDamage(damage.nextInt(rand));
			return stack;
		};
	}

	public static ILootFunction enchantWithLevels(int levels, boolean treasure) {
		return enchantWithLevels(new RandomValueRange(levels), treasure);
	}

	public static ILootFunction enchantWithLevels(int minLevels, int maxLevels, boolean treasure) {
		return enchantWithLevels(new RandomValueRange(minLevels, maxLevels), treasure);
	}

	public static ILootFunction enchantWithLevels(RandomValueRange levels, boolean treasure) {
		return (stack, rand, context) -> Enchantments.addRandomEnchantment(rand, stack, levels.nextInt(rand), treasure);
	}

	public static ILootFunction enchantRandomly(int... enchantments) {
		boolean checkApplicable = enchantments.length == 0;
		if (enchantments.length == 0) {
			enchantments = Enchantments.ALL_ENCHANTMENTS;
		}
		final int[] enchantments_f = enchantments;

		return (stack, rand, context) -> {
			int enchantment;
			// choose enchantment
			if (checkApplicable) {
				List<Integer> allowedEnchantments = new ArrayList<>(enchantments_f.length);
				for (int ench : enchantments_f) {
					if (Enchantments.canApply(ench, stack, false)) {
						allowedEnchantments.add(ench);
					}
				}
				enchantment = allowedEnchantments.get(rand.nextInt(allowedEnchantments.size()));
			} else {
				enchantment = enchantments_f[rand.nextInt(enchantments_f.length)];
			}

			// replace book with enchanted book
			if (Items.BOOK.equals(stack.getItem())) {
				stack = new ItemStack(Items.ENCHANTED_BOOK);
			}

			// apply enchantment to stack
			stack.addEnchantment(enchantment, MathHelper.randomRange(rand, 1, Enchantments.getMaxLevel(enchantment)));
			return stack;
		};
	}

}
