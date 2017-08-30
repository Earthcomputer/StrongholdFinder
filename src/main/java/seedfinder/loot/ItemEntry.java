package seedfinder.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;

public class ItemEntry extends LootTablePoolEntry {

	private String item;
	private LootFunction[] functions;

	ItemEntry(int weight, int quality, LootCondition[] conditions, String item, LootFunction[] functions) {
		super(weight, quality, conditions);
		this.item = item;
		this.functions = functions;
	}

	@Override
	public void generate(List<ItemStack> stacks, Random rand, LootContext context) {
		ItemStack stack = new ItemStack(item);

		for (LootFunction function : functions) {
			if (function.testAllConditions(rand, context)) {
				stack = function.applyFunction(stack, rand, context);
			}
		}

		if (stack.getCount() >= 0) {
			stacks.add(stack);
		}
	}

	public static class Builder extends LootTablePoolEntry.Builder<ItemEntry, Builder> {

		private String item;
		List<LootFunction> functions = new ArrayList<>();

		Builder(LootTablePool.Builder parent, String item) {
			super(parent);
			this.item = item;
		}

		public Builder addFunction(ILootFunction function) {
			return startFunction(function).endFunction();
		}

		public LootFunction.Builder startFunction(ILootFunction function) {
			return new LootFunction.Builder(this, function);
		}

		@Override
		protected ItemEntry create() {
			return new ItemEntry(weight, quality, conditions.toArray(new LootCondition[conditions.size()]), item,
					functions.toArray(new LootFunction[functions.size()]));
		}

	}

}
