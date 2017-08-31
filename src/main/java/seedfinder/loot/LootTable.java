package seedfinder.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;

public class LootTable {

	private LootTablePool[] pools;

	private LootTable(LootTablePool[] pools) {
		this.pools = pools;
	}

	public List<ItemStack> generate(Random rand, LootContext context) {
		List<ItemStack> stacks = new ArrayList<>();

		if (!context.addLootTable(this)) {
			return stacks;
		}

		for (LootTablePool pool : pools) {
			pool.generate(stacks, rand, context);
		}

		context.removeLootTable(this);

		return stacks;
	}

	public static class Builder {
		List<LootTablePool> pools = new ArrayList<>();

		private Builder() {
		}

		public static Builder create() {
			return new Builder();
		}

		public LootTablePool.Builder startPool() {
			return new LootTablePool.Builder(this);
		}

		public LootTable build() {
			return new LootTable(pools.toArray(new LootTablePool[pools.size()]));
		}
	}

}
