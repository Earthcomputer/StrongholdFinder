package seedfinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedfinder.Enchantments.EnchantmentInstance;

public class ItemStack {

	private final String item;
	private int count = 1;
	private int damage = 0;
	private List<EnchantmentInstance> enchantments = new ArrayList<>();

	public ItemStack(String item) {
		this.item = item;
	}

	public String getItem() {
		return item;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public List<EnchantmentInstance> getEnchantments() {
		return enchantments;
	}

	public void addEnchantment(int ench, int lvl) {
		enchantments.add(new EnchantmentInstance(ench, lvl));
	}

	public boolean canMerge(ItemStack other) {
		return item.equals(other.item) && damage == other.damage && enchantments.isEmpty()
				&& other.enchantments.isEmpty();
	}

	public ItemStack copy() {
		ItemStack other = new ItemStack(item);
		other.count = count;
		other.damage = damage;
		other.enchantments = new ArrayList<>(enchantments);
		return other;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(count).append("x ").append(item);
		if (damage != 0) {
			str.append(" @").append(damage);
		}
		if (!enchantments.isEmpty()) {
			str.append(" ").append(enchantments);
		}
		return str.toString();
	}

	public static List<ItemStack> mergeAll(Collection<? extends Collection<ItemStack>> stacks) {
		List<ItemStack> allStacks = new ArrayList<>();
		stacks.forEach(allStacks::addAll);
		return merge(allStacks);
	}

	public static List<ItemStack> merge(Collection<ItemStack> stacks) {
		Set<String> existingItems = new HashSet<>();
		List<ItemStack> merged = new ArrayList<>();

		stacks.forEach(stack -> {
			String item = stack.getItem();

			if (existingItems.contains(item)) {
				for (ItemStack existingStack : merged) {
					if (stack.canMerge(existingStack)) {
						existingStack.count += stack.count;
						return;
					}
				}
			}

			merged.add(stack.copy());
			existingItems.add(item);
		});

		return merged;
	}

}
