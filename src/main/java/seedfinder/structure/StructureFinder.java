package seedfinder.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import seedfinder.util.ChunkPos;
import seedfinder.util.Storage3D;
import seedfinder.worldgen.WorldGen;

/**
 * Finds structures
 */
public abstract class StructureFinder {

	/**
	 * The chunk positions of found structures
	 */
	protected final Set<ChunkPos> structurePositions = new HashSet<>();
	/**
	 * The cached structures by chunk position
	 */
	protected final Map<ChunkPos, Structure> structures = new HashMap<>();

	/**
	 * Returns whether a structure is at the given chunk position. Only returns
	 * the correct result after the chunk has been covered by a call to
	 * {@link #findStructurePositions(ChunkPos, ChunkPos)}
	 */
	public boolean isStructureAt(ChunkPos pos) {
		return structurePositions.contains(pos);
	}

	/**
	 * Gets a set of structure positions found by
	 * {@link #findStructurePositions(Random, long, ChunkPos, ChunkPos)}
	 */
	public Set<ChunkPos> getStructurePositions() {
		return Collections.unmodifiableSet(structurePositions);
	}

	/**
	 * Removes the cached structure at the given position
	 */
	public void removeCachedStructure(ChunkPos pos) {
		structures.remove(pos);
	}

	/**
	 * Resets this structure finder for a new seed
	 */
	public void reset() {
		structurePositions.clear();
		structures.clear();
	}

	/**
	 * Creates a structure at the given position, or gets a cached structure.
	 * This method assumes that the given RNG has already been initialized
	 * according to the chunk position.
	 */
	public Structure getStructure(Random rand, ChunkPos pos) {
		Structure structure = structures.get(pos);
		if (structure == null) {
			structure = createStructure(rand, pos);
			structures.put(pos, structure);
		}
		return structure;
	}

	public void findStructurePositionsAffectingChunk(Random rand, long worldSeed, ChunkPos chunk) {
		findStructurePositions(rand, worldSeed, new ChunkPos(chunk.getX() - 8, chunk.getZ() - 8),
				new ChunkPos(chunk.getX() + 8, chunk.getZ() + 8));
	}

	/**
	 * Finds all structure positions in the rectangle with the given corners,
	 * and adds them to the {@link #structurePositions} set. The given RNG has
	 * not been initialized according to the world seed. Implementations of this
	 * method are encouraged to cache the results where possible.
	 */
	public void findStructurePositions(Random rand, long worldSeed, ChunkPos fromPos, ChunkPos toPos) {
		for (int x = Math.min(fromPos.getX(), toPos.getX()), xe = Math.max(fromPos.getX(),
				toPos.getX()); x <= xe; x++) {
			for (int z = Math.min(fromPos.getZ(), toPos.getZ()), ze = Math.max(fromPos.getZ(),
					toPos.getZ()); z <= ze; z++) {
				ChunkPos pos = new ChunkPos(x, z);

				WorldGen.setMapGenSeedForChunk(rand, worldSeed, x, z);
				rand.nextInt(); // Only God and Mojang know...

				if (isStructureAt(rand, worldSeed, pos)) {
					structurePositions.add(pos);
				}
			}
		}
	}

	/**
	 * Returns whether a structure can start at the given coordinates.
	 * Implementations of this method are allowed to assume that the given RNG
	 * has already been initialized according to the chunk position.
	 */
	public abstract boolean isStructureAt(Random rand, long worldSeed, ChunkPos pos);

	/**
	 * Creates a new structure at the given position. This method assumes that
	 * the given RNG has already been initialized according to the chunk
	 * position.
	 */
	protected abstract Structure createStructure(Random rand, ChunkPos pos);

	/**
	 * Populates the given chunk with this structure type. Assumes that the
	 * RNG's seed has already been set, and that
	 * {@link #findStructurePositions(Random, long, ChunkPos, ChunkPos)} has
	 * already been called.
	 */
	public void populate(Storage3D world, Random rand, long worldSeed, int chunkX, int chunkZ) {
		int popX = (chunkX << 4) + 8;
		int popZ = (chunkZ << 4) + 8;

		// We need a separate RNG here with separate seeds
		Random structureLayoutRand = new Random();
		structurePositions.forEach(structurePos -> {
			WorldGen.setMapGenSeedForChunk(structureLayoutRand, worldSeed, structurePos.getX(), structurePos.getZ());
			Structure structure = getStructure(structureLayoutRand, structurePos);

			if (structure.isValid() && structure.getBoundingBox().intersectsWith(popX, popZ, popX + 15, popZ + 15)) {
				structure.populate(world, rand, chunkX, chunkZ);
			}
		});
	}

}
