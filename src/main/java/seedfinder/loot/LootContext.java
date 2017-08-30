package seedfinder.loot;

import java.util.HashSet;
import java.util.Set;

public class LootContext {

	private Set<LootTable> lootTables = new HashSet<>();
	
	public boolean addLootTable(LootTable table) {
		return lootTables.add(table);
	}
	
	public void removeLootTable(LootTable table) {
		lootTables.remove(table);
	}
	
	public float getLuck() {
		return 0;
	}
	
}
