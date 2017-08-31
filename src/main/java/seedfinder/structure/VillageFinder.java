package seedfinder.structure;

import java.util.List;
import java.util.Random;
import java.util.Set;

import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;
import seedfinder.util.ChunkPos;
import seedfinder.worldgen.WorldGen;

public class VillageFinder extends StructureFinder {

	public static final VillageFinder INSTANCE = new VillageFinder();

	private static final Set<Integer> ALLOWED_BIOMES = Biomes.setOf(Biomes.PLAINS, Biomes.DESERT, Biomes.SAVANNA,
			Biomes.TAIGA);

	private VillageFinder() {
	}

	@Override
	public void findStructurePositions(Random rand, long worldSeed, ChunkPos fromPos, ChunkPos toPos) {
		/*
		 * Villages are generated in 'sections', a 32x32 chunk square in the
		 * world. There is a maximum of 1 village per section.
		 */

		int minChunkX = Math.min(fromPos.getX(), toPos.getX());
		int maxChunkX = Math.max(fromPos.getX(), toPos.getX());
		int minChunkZ = Math.min(fromPos.getZ(), toPos.getZ());
		int maxChunkZ = Math.max(fromPos.getZ(), toPos.getZ());

		final int sectionSize = 32;

		// Calculate the section positions. If negative, we have to subtract
		// some more because we don't want the division being truncated to 0 and
		// overlapping another section.
		int minSectionX = minChunkX;
		if (minSectionX < 0) {
			minSectionX -= sectionSize - 1;
		}
		minSectionX /= sectionSize;

		int maxSectionX = maxChunkX;
		if (maxSectionX < 0) {
			maxSectionX -= sectionSize - 1;
		}
		maxSectionX /= sectionSize;

		int minSectionZ = minChunkZ;
		if (minSectionZ < 0) {
			minSectionZ -= sectionSize - 1;
		}
		minSectionZ /= sectionSize;

		int maxSectionZ = maxChunkZ;
		if (maxSectionZ < 0) {
			maxSectionZ -= sectionSize - 1;
		}
		maxSectionZ /= sectionSize;

		// Get the actual village positions
		for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
			for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
				WorldGen.setRandomSeed(rand, worldSeed, sectionX, sectionZ, 10387312);
				int chunkX = sectionX * sectionSize;
				int chunkZ = sectionZ * sectionSize;
				chunkX += rand.nextInt(sectionSize - 8);
				chunkZ += rand.nextInt(sectionSize - 8);

				if (BiomeProvider.areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 0, ALLOWED_BIOMES)) {
					structurePositions.add(new ChunkPos(chunkX, chunkZ));
				}
			}
		}
	}

	@Override
	public boolean isStructureAt(Random rand, long worldSeed, ChunkPos pos) {
		findStructurePositions(rand, worldSeed, pos, pos);
		return structurePositions.contains(pos);
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
