package seedfinder.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.ItemStack;

public class LootFunction {

	private ILootFunction function;
	private LootCondition[] conditions;

	private LootFunction(ILootFunction function, LootCondition[] conditions) {
		this.function = function;
		this.conditions = conditions;
	}

	public ItemStack applyFunction(ItemStack stack, Random rand, LootContext context) {
		return function.apply(stack, rand, context);
	}

	public boolean testAllConditions(Random rand, LootContext context) {
		for (LootCondition condition : conditions) {
			if (!condition.test(rand, context)) {
				return false;
			}
		}
		return true;
	}

	public static class Builder {
		private ItemEntry.Builder parent;
		private ILootFunction function;
		private List<LootCondition> conditions = new ArrayList<>();

		Builder(ItemEntry.Builder parent, ILootFunction function) {
			this.parent = parent;
			this.function = function;
		}

		public Builder addCondition(LootCondition condition) {
			conditions.add(condition);
			return this;
		}

		public ItemEntry.Builder endFunction() {
			LootFunction function = new LootFunction(this.function,
					conditions.toArray(new LootCondition[conditions.size()]));
			parent.functions.add(function);
			return parent;
		}
	}

}
