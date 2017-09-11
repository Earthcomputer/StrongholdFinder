package seedfinder.structure;

import java.util.Random;

import seedfinder.util.ChunkPos;
import seedfinder.worldgen.WorldGen;

public abstract class ScatteredStructureFinder extends StructureFinder {

	private final int uniquifier;

	protected ScatteredStructureFinder(int uniquifier) {
		this.uniquifier = uniquifier;
	}

	@Override
	public void findStructurePositions(Random rand, long worldSeed, ChunkPos fromPos, ChunkPos toPos) {
		/*
		 * These types of structure are generated in 'sections', a 32x32 chunk
		 * square in the world. There is a maximum of one structure per section.
		 */

		int minChunkX = Math.min(fromPos.getX(), toPos.getX());
		int minChunkZ = Math.min(fromPos.getZ(), toPos.getZ());
		int maxChunkX = Math.max(fromPos.getX(), toPos.getX());
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

		int minSectionZ = minChunkZ;
		if (minSectionZ < 0) {
			minSectionZ -= sectionSize - 1;
		}
		minSectionZ /= sectionSize;

		int maxSectionX = maxChunkX;
		if (maxSectionX < 0) {
			maxSectionX -= sectionSize - 1;
		}
		maxSectionX /= sectionSize;

		int maxSectionZ = maxChunkZ;
		if (maxSectionZ < 0) {
			maxSectionZ -= sectionSize - 1;
		}
		maxSectionZ /= sectionSize;

		// Get the actual structure positions
		for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
			for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
				WorldGen.setRandomSeed(rand, worldSeed, sectionX, sectionZ, uniquifier);
				int chunkX = sectionX * sectionSize;
				int chunkZ = sectionZ * sectionSize;
				chunkX += rand.nextInt(sectionSize - 8);
				chunkZ += rand.nextInt(sectionSize - 8);

				if (isValidPosition(rand, chunkX, chunkZ)) {
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

	/**
	 * Tests the given chunk position for whether a structure could spawn there.
	 * For example, this method could test for a valid biome.
	 */
	protected abstract boolean isValidPosition(Random rand, int chunkX, int chunkZ);

}
