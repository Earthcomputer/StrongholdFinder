package seedfinder.loot;

import java.util.Random;

@FunctionalInterface
public interface LootCondition {

	boolean test(Random rand, LootContext context);

	public static LootCondition randomChance(float chance) {
		return (rand, context) -> rand.nextFloat() < chance;
	}

}
