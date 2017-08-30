package seedfinder.loot;

import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;

public class LootTableEntry extends LootTablePoolEntry {

	private LootTable lootTable;

	private LootTableEntry(int weight, int quality, LootCondition[] conditions, LootTable lootTable) {
		super(weight, quality, conditions);
		this.lootTable = lootTable;
	}

	@Override
	public void generate(List<ItemStack> stacks, Random rand, LootContext context) {
		stacks.addAll(lootTable.generate(rand, context));
	}

	public static class Builder extends LootTablePoolEntry.Builder<LootTableEntry, Builder> {

		private LootTable lootTable;

		Builder(LootTablePool.Builder parent, LootTable lootTable) {
			super(parent);
			this.lootTable = lootTable;
		}

		@Override
		protected LootTableEntry create() {
			return new LootTableEntry(weight, quality, conditions.toArray(new LootCondition[conditions.size()]),
					lootTable);
		}

	}

}
