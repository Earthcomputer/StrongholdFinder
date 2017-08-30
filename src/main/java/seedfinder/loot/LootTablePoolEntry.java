package seedfinder.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;
import seedfinder.MathHelper;

public abstract class LootTablePoolEntry {

	private int weight;
	private int quality;
	private LootCondition[] conditions;

	protected LootTablePoolEntry(int weight, int quality, LootCondition[] conditions) {
		this.weight = weight;
		this.quality = quality;
		this.conditions = conditions;
	}

	public boolean testAllConditions(Random rand, LootContext context) {
		for (LootCondition condition : conditions) {
			if (!condition.test(rand, context)) {
				return false;
			}
		}
		return true;
	}

	public int getWeight(float luck) {
		return Math.max(MathHelper.floor(weight + quality * luck), 0);
	}

	public abstract void generate(List<ItemStack> stacks, Random rand, LootContext context);

	@SuppressWarnings("unchecked")
	public static abstract class Builder<ETRY extends LootTablePoolEntry, BDR extends Builder<ETRY, BDR>> {
		private LootTablePool.Builder parent;
		protected int weight;
		protected int quality;
		protected List<LootCondition> conditions = new ArrayList<>();

		protected Builder(LootTablePool.Builder parent) {
			this.parent = parent;
		}

		public BDR setWeight(int weight) {
			this.weight = weight;
			return (BDR) this;
		}

		public BDR setQuality(int quality) {
			this.quality = quality;
			return (BDR) this;
		}

		public BDR addCondition(LootCondition condition) {
			conditions.add(condition);
			return (BDR) this;
		}

		protected abstract ETRY create();

		public LootTablePool.Builder endEntry() {
			parent.entries.add(create());
			return parent;
		}
	}

}
