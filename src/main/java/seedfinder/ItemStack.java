package seedfinder;

import java.util.ArrayList;
import java.util.List;

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

}
