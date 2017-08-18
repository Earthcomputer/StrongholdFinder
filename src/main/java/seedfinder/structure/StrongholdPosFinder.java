package seedfinder.structure;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import seedfinder.AABB;
import seedfinder.BlockPos;
import seedfinder.ChunkPos;
import seedfinder.Storage3D;
import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;
import seedfinder.worldgen.WorldGen;

public class StrongholdPosFinder {

	public static final int NUM_STRONGHOLDS = 128;

	private static final Set<Integer> ALLOWED_BIOMES = Biomes.allBiomesExcept(it -> Biomes.getBaseHeight(it) <= 0);

	private StrongholdPosFinder() {
	}

	/**
	 * Finds the starting positions of the first <tt>positions.length</tt>
	 * strongholds.
	 */
	public static void getStrongholdPositions(Random rand, long seed, ChunkPos[] positions) {
		rand.setSeed(seed);

		final double distance = 32;
		int spread = 3;
		int layer = 0;
		int strongholdsInLayer = 0;

		double strongholdAngle = rand.nextDouble() * Math.PI * 2;

		for (int i = 0; i < positions.length; i++) {
			// Get initial position
			double strongholdDistance = 4 * distance + distance * layer * 6
					+ (rand.nextDouble() - 0.5) * distance * 2.5;
			int chunkX = (int) Math.round(Math.cos(strongholdAngle) * strongholdDistance);
			int chunkZ = (int) Math.round(Math.sin(strongholdAngle) * strongholdDistance);

			// Correct position based on biomes
			BlockPos biomePos = BiomeProvider.findBiomePosition((chunkX << 4) + 8, (chunkZ << 4) + 8, 112,
					ALLOWED_BIOMES, rand);
			if (biomePos != null) {
				chunkX = biomePos.getX() >> 4;
				chunkZ = biomePos.getZ() >> 4;
			}

			positions[i] = new ChunkPos(chunkX, chunkZ);

			// Increment all the variables
			strongholdAngle += (Math.PI * 2) / spread;
			strongholdsInLayer++;

			if (strongholdsInLayer == spread) {
				layer++;
				strongholdsInLayer = 0;
				spread += 2 * spread / (layer + 1);
				spread = Math.min(spread, NUM_STRONGHOLDS - i);
				strongholdAngle += rand.nextDouble() * Math.PI * 2;
			}
		}
	}

	/**
	 * Gets the number of eyes of the stronghold at the given location
	 */
	public static int getNumEyes(Storage3D world, Random rand, long seed, ChunkPos location) {
		Stronghold stronghold = getStronghold(rand, seed, location);
		generatePortalChunks(world, rand, seed, stronghold);
		return StrongholdGen.getNumEyes();
	}

	/**
	 * Gets the {@link Stronghold} that would generate at the given location
	 */
	public static Stronghold getStronghold(Random rand, long seed, ChunkPos location) {
		WorldGen.setMapGenSeedForChunk(rand, seed, location.getX(), location.getZ());
		rand.nextInt(); // why? idk, but we must conform to Minecraft...

		// Create different configurations of stronghold until one has a portal
		// room
		Stronghold stronghold;
		do {
			stronghold = new Stronghold(rand, location);
		} while (stronghold.getComponents().isEmpty() || stronghold.getPortalRoom() == null);
		return stronghold;
	}

	// Generates chunks around the end portal to get the likely number of eyes
	private static void generatePortalChunks(Storage3D world, Random rand, long worldSeed, Stronghold stronghold) {
		StrongholdGen.resetNumEyes();

		BlockPos minPortalPos = stronghold.getPortalRoom().getPortalPos();
		BlockPos maxPortalPos = minPortalPos.add(4, 0, 4);
		int minChunkX = (minPortalPos.getX() - 8) >> 4;
		int minChunkZ = (minPortalPos.getZ() - 8) >> 4;
		int maxChunkX = (maxPortalPos.getX() - 8) >> 4;
		int maxChunkZ = (maxPortalPos.getZ() - 8) >> 4;
		int minX = minChunkX << 4;
		int minZ = minChunkZ << 4;

		// A 3x3 chunk area is the maximum number of chunks that might be
		// generated
		world.eraseAndAllocate(minX, 0, minZ, minX + 47, 255, minZ + 47);

		// Generate the chunks
		for (int chunkX = minChunkX; chunkX <= maxChunkX + 1; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ + 1; chunkZ++) {
				WorldGen.createOverworld(rand, worldSeed, chunkX, chunkZ, world);
			}
		}

		// Populate the chunks (all we do is the stronghold)
		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				// TODO: perhaps simulate mineshafts and villages if an
				// "accurate" flag
				// is true?
				WorldGen.setSeedForPopulation(rand, worldSeed, chunkX, chunkZ);

				AABB popBB = new AABB((chunkX << 4) + 8, 1, (chunkZ << 4) + 8, (chunkX << 4) + 8 + 15, 512,
						(chunkZ << 4) + 8 + 15);

				// Generate the stronghold components that are in the chunk
				Iterator<Component> itr = stronghold.getComponents().iterator();
				while (itr.hasNext()) {
					Component component = itr.next();
					if (component.getBoundingBox().intersectsWith(popBB)
							&& !component.placeInWorld(world, rand, popBB)) {
						itr.remove();
					}
				}
			}
		}
	}

	/**
	 * Represents a configuration of stronghold rooms
	 */
	public static class Stronghold extends Structure {

		public Stronghold(Random rand, ChunkPos location) {
			StrongholdGen.prepareStructurePieces();

			StrongholdGen.StartingStairs stairs2 = new StrongholdGen.StartingStairs(0, rand, (location.getX() << 4) + 2,
					(location.getZ() << 4) + 2);
			getComponents().add(stairs2);

			stairs2.addMoreComponents(stairs2, getComponents(), rand);

			// Recursively add more rooms until there are none left to add
			List<StrongholdGen.StrongholdComponent> pendingChildren = stairs2.pendingChildren;
			while (!pendingChildren.isEmpty()) {
				int index = rand.nextInt(pendingChildren.size());
				StrongholdGen.StrongholdComponent nextComponent = pendingChildren.remove(index);
				nextComponent.addMoreComponents(stairs2, getComponents(), rand);
			}

			// Update the main outer bounding box
			updateBoundingBox();

			// Shift the entire structure vertically
			adjustVertical(rand);
		}

		public StrongholdGen.PortalRoom getPortalRoom() {
			return ((StrongholdGen.StartingStairs) getComponents().get(0)).portalRoom;
		}

	}

}
