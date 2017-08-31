package seedfinder.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;
import seedfinder.util.MathHelper;

public class LootTablePool {

	private LootTablePoolEntry[] entries;
	private LootCondition[] conditions;
	private RandomValueRange rolls;
	private RandomValueRange bonusRolls;

	private LootTablePool(LootTablePoolEntry[] entries, LootCondition[] conditions, RandomValueRange rolls,
			RandomValueRange bonusRolls) {
		this.entries = entries;
		this.conditions = conditions;
		this.rolls = rolls;
		this.bonusRolls = bonusRolls;
	}

	public void generate(List<ItemStack> stacks, Random rand, LootContext context) {
		for (LootCondition condition : conditions) {
			if (!condition.test(rand, context)) {
				return;
			}
		}

		int rolls = this.rolls.nextInt(rand) + MathHelper.floor(bonusRolls.nextFloat(rand) * context.getLuck());
		for (int i = 0; i < rolls; i++) {
			List<LootTablePoolEntry> validEntries = new ArrayList<>();

			for (LootTablePoolEntry entry : entries) {
				if (entry.testAllConditions(rand, context)) {
					int weight = entry.getWeight(context.getLuck());
					if (weight > 0) {
						validEntries.add(entry);
					}
				}
			}

			LootTablePoolEntry chosenEntry = MathHelper.weightedRandom(rand, validEntries,
					it -> it.getWeight(context.getLuck()));
			if (chosenEntry != null) {
				chosenEntry.generate(stacks, rand, context);
			}
		}
	}

	public static class Builder {
		private LootTable.Builder parent;
		List<LootTablePoolEntry> entries = new ArrayList<>();
		private List<LootCondition> conditions = new ArrayList<>();
		private RandomValueRange rolls;
		private RandomValueRange bonusRolls = new RandomValueRange(0);

		Builder(LootTable.Builder parent) {
			this.parent = parent;
		}

		public ItemEntry.Builder startItemEntry(String item) {
			return new ItemEntry.Builder(this, item);
		}

		public LootTableEntry.Builder startLootTableEntry(LootTable lootTable) {
			return new LootTableEntry.Builder(this, lootTable);
		}

		public EmptyEntry.Builder startEmptyEntry() {
			return new EmptyEntry.Builder(this);
		}

		public Builder addCondition(LootCondition condition) {
			conditions.add(condition);
			return this;
		}

		public Builder setRolls(int rolls) {
			this.rolls = new RandomValueRange(rolls);
			return this;
		}

		public Builder setRolls(int min, int max) {
			this.rolls = new RandomValueRange(min, max);
			return this;
		}

		public Builder setBonusRolls(int bonusRolls) {
			this.bonusRolls = new RandomValueRange(bonusRolls);
			return this;
		}

		public Builder setBonusRolls(int min, int max) {
			this.bonusRolls = new RandomValueRange(min, max);
			return this;
		}

		public LootTable.Builder endPool() {
			LootTablePool pool = new LootTablePool(entries.toArray(new LootTablePoolEntry[entries.size()]),
					conditions.toArray(new LootCondition[conditions.size()]), rolls, bonusRolls);
			parent.pools.add(pool);
			return parent;
		}
	}

}
