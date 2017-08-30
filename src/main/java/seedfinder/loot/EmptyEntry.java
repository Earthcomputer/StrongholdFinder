package seedfinder.loot;

import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;

public class EmptyEntry extends LootTablePoolEntry {

	private EmptyEntry(int weight, int quality, LootCondition[] conditions) {
		super(weight, quality, conditions);
	}

	@Override
	public void generate(List<ItemStack> stacks, Random rand, LootContext context) {
	}

	public static class Builder extends LootTablePoolEntry.Builder<EmptyEntry, Builder> {

		Builder(seedfinder.loot.LootTablePool.Builder parent) {
			super(parent);
		}

		@Override
		protected EmptyEntry create() {
			return new EmptyEntry(weight, quality, conditions.toArray(new LootCondition[conditions.size()]));
		}

	}

}
