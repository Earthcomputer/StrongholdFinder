package seedfinder.structure;

import java.util.List;
import java.util.Random;
import java.util.Set;

import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;
import seedfinder.util.ChunkPos;

public class VillageFinder extends ScatteredStructureFinder {

	public static final VillageFinder INSTANCE = new VillageFinder();

	private static final Set<Integer> ALLOWED_BIOMES = Biomes.setOf(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA,
			Biomes.TAIGA);

	private VillageFinder() {
		super(10387312);
	}

	@Override
	protected boolean isValidPosition(Random rand, int chunkX, int chunkZ) {
		return BiomeProvider.areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 0, ALLOWED_BIOMES);
	}

	@Override
	protected Village createStructure(Random rand, ChunkPos pos) {
		rand.nextInt(); // weird, but necessary...

		return new Village(rand, pos.getX(), pos.getZ(), 0);
	}

	public static class Village extends Structure {

		private boolean valid;

		public Village(Random rand, int x, int z, int size) {
			List<VillageGen.PoolEntry> weights = VillageGen.createPoolEntries(rand, size);
			VillageGen.Start start = new VillageGen.Start(rand, (x << 4) + 2, (z << 4) + 2, weights, size);
			getComponents().add(start);
			start.addMoreComponents(start, getComponents(), rand);
			List<Component> pendingRoads = start.pendingRoads;
			List<Component> pendingHouses = start.pendingHouses;

			while (!pendingRoads.isEmpty() || !pendingHouses.isEmpty()) {
				if (pendingRoads.isEmpty()) {
					int index = rand.nextInt(pendingHouses.size());
					Component component = pendingHouses.remove(index);
					component.addMoreComponents(start, getComponents(), rand);
				} else {
					int index = rand.nextInt(pendingRoads.size());
					Component component = pendingRoads.remove(index);
					component.addMoreComponents(start, getComponents(), rand);
				}
			}

			updateBoundingBox();

			int houseCount = (int) getComponents().stream().filter(it -> !(it instanceof VillageGen.Road)).count();

			valid = houseCount > 2;
		}

		@Override
		public boolean isValid() {
			return valid;
		}

	}

}
