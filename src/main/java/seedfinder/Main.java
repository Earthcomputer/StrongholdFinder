package seedfinder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import seedfinder.biome.BiomeProvider;
import seedfinder.loot.LootTables;
import seedfinder.structure.MineshaftFinder;
import seedfinder.structure.StrongholdFinder;
import seedfinder.structure.StrongholdFinder.Stronghold;
import seedfinder.structure.StrongholdGen;
import seedfinder.structure.VillageFinder;
import seedfinder.task.GatherChestsTask;
import seedfinder.task.Task;
import seedfinder.util.BlockPos;
import seedfinder.util.ChunkPos;
import seedfinder.util.Storage3D;
import seedfinder.worldgen.WorldGen;

public class Main {

	private static volatile long seed;
	private static int eyesThreshold;
	private static int totalThreshold;

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("java -jar seed_finder.jar <start_seed> <eyes_threshold> [total_threshold]");
			System.out.println("java -jar seed_finder.jar seed <seed>");
			return;
		}

		if ("seed".equalsIgnoreCase(args[0])) {
			seed = Long.parseLong(args[1]);
			// so we print all strongholds
			eyesThreshold = 0;
			totalThreshold = 0;
			printSeedInfo(new Random(), new Storage3D(Blocks.AIR));
		} else {
			seed = Long.parseLong(args[0]);
			eyesThreshold = Integer.parseInt(args[1]);
			totalThreshold = args.length == 2 ? eyesThreshold : Integer.parseInt(args[2]);
			findSeeds();
		}
	}

	private static void printSeedInfo(Random random, Storage3D world) {
		// Initialize things to the seed
		WorldGen.setWorldSeed(random, seed);
		BiomeProvider.setWorldSeed(seed);
		MineshaftFinder.INSTANCE.reset();
		VillageFinder.INSTANCE.reset();
		StrongholdFinder.INSTANCE.reset();

		// Get stronghold positions
		StrongholdFinder.INSTANCE.findFirstLayerPositions(random, seed);

		for (ChunkPos strongholdPos : new HashSet<>(StrongholdFinder.INSTANCE.getStructurePositions())) {
			int eyes = StrongholdFinder.INSTANCE.getNumEyes(world, random, seed, strongholdPos, false);

			if (eyes >= eyesThreshold) {
				// Check for false positive
				eyes = StrongholdFinder.INSTANCE.getNumEyes(world, random, seed, strongholdPos, true);

				if (eyes >= eyesThreshold) {
					WorldGen.setMapGenSeedForChunk(random, seed, strongholdPos.getX(), strongholdPos.getZ());
					Stronghold stronghold = (Stronghold) StrongholdFinder.INSTANCE.getStructure(random, strongholdPos);

					// Find number of pearls in chests
					GatherChestsTask task = new GatherChestsTask();
					Task.setCurrentTask(task);

					stronghold.getComponents().stream().filter(it -> it instanceof StrongholdGen.ChestCorridor)
							.map(it -> (StrongholdGen.ChestCorridor) it).map(StrongholdGen.ChestCorridor::getChestPos)
							.forEach(pos -> WorldGen.createAndPopulatePosOverworld(world, random, seed, pos.getX(),
									pos.getZ()));

					Map<BlockPos, List<ItemStack>> chestContents = task.generateChests(LootTables.STRONGHOLD_CORRIDOR);
					int pearls = 0;
					Set<BlockPos> pearlChestPositions = new HashSet<>();
					for (Map.Entry<BlockPos, List<ItemStack>> chest : chestContents.entrySet()) {
						int pearlsInThisChest = chest.getValue().stream()
								.filter(it -> Items.ENDER_PEARL.equals(it.getItem())).mapToInt(ItemStack::getCount)
								.sum();
						if (pearlsInThisChest > 0) {
							pearls += pearlsInThisChest;
							pearlChestPositions.add(chest.getKey());
						}
					}
					if (eyes + pearls >= totalThreshold) {
						// Print info about stronghold
						System.out.println("-----------------------------");
						System.out.println("Seed: " + seed);
						System.out.println("Total: " + (eyes + pearls));
						System.out.println("Eyes: " + eyes);
						System.out.println("   In portal at:");
						System.out.println(
								"      " + ((Stronghold) StrongholdFinder.INSTANCE.getStructure(random, strongholdPos))
										.getPortalRoom().getPortalPos());
						System.out.println("Pearls: " + pearls);
						System.out.println("   In chest(s) at:");
						pearlChestPositions.forEach(pos -> System.out.println("      " + pos));
					}
				}
			}
		}
	}

	private static void findSeeds() {
		final long initialSeed = seed;
		// Allow user to stop seed finder at any time
		new Thread(() -> {
			Scanner scanner = new Scanner(System.in);
			while (!scanner.nextLine().equalsIgnoreCase("stop")) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			scanner.close();

			System.out.println("Current seed searching: " + seed);
			System.out.println("Number of seeds searched: " + (seed - initialSeed));
			System.out.println("Eyes threshold: " + eyesThreshold);
			System.out.println("Total threshold: " + totalThreshold);

			System.exit(0);
		}).start();

		Random random = new Random();
		Storage3D world = new Storage3D(Blocks.AIR);

		while (true) {
			printSeedInfo(random, world);
			seed++;
		}
	}

}
