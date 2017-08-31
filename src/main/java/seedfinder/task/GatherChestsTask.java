package seedfinder.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import seedfinder.ItemStack;
import seedfinder.loot.LootContext;
import seedfinder.loot.LootTable;
import seedfinder.util.BlockPos;

public class GatherChestsTask extends Task {

	private Map<BlockPos, Chest> chests = new HashMap<>();

	public GatherChestsTask() {
		super(Type.GATHER_CHESTS);
	}

	public void addChest(int x, int y, int z, LootTable lootTable, long seed) {
		BlockPos pos = new BlockPos(x, y, z);
		chests.put(pos, new Chest(pos, lootTable, seed));
	}

	public Map<BlockPos, List<ItemStack>> generateChests() {
		return generateChests(it -> true);
	}

	public Map<BlockPos, List<ItemStack>> generateChests(LootTable lootTable) {
		return generateChests(it -> it.lootTable == lootTable);
	}

	private Map<BlockPos, List<ItemStack>> generateChests(Predicate<Chest> predicate) {
		Map<BlockPos, List<ItemStack>> chests = new HashMap<>();
		Random rand = new Random();
		LootContext context = new LootContext();
		this.chests.values().stream().filter(predicate).forEach(it -> {
			rand.setSeed(it.seed);
			chests.put(it.pos, it.lootTable.generate(rand, context));
		});
		return chests;
	}

	private static class Chest {
		private BlockPos pos;
		private LootTable lootTable;
		private long seed;

		public Chest(BlockPos pos, LootTable lootTable, long seed) {
			this.pos = pos;
			this.lootTable = lootTable;
			this.seed = seed;
		}
	}

}
