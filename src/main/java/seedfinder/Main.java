package seedfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import seedfinder.biome.BiomeProvider;
import seedfinder.loot.LootTables;
import seedfinder.structure.MineshaftFinder;
import seedfinder.structure.NBTStructures;
import seedfinder.structure.StrongholdFinder;
import seedfinder.structure.StrongholdFinder.Stronghold;
import seedfinder.structure.StrongholdGen;
import seedfinder.structure.TempleFinder;
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

	private static void printUsage() {
		System.out.println("java -jar seed_finder.jar <start_seed> <eyes_threshold> [total_threshold]");
		System.out.println("java -jar seed_finder.jar seed <seed>");
		System.out.println("java -jar seed_finder.jar legacy <old_output_file> <eyes_threshold>");
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			printUsage();
			return;
		}

		try {
			NBTStructures.load();
		} catch (IOException e) {
			System.err.println("Failed to load NBT structures");
			e.printStackTrace();
			return;
		}

		if ("seed".equalsIgnoreCase(args[0])) {
			seed = Long.parseLong(args[1]);
			// so we print all strongholds
			eyesThreshold = 0;
			totalThreshold = 0;
			printSeedInfo(new Random(), new Storage3D(Blocks.AIR));
		} else if ("legacy".equalsIgnoreCase(args[0])) {
			if (args.length < 3) {
				printUsage();
				return;
			}
			eyesThreshold = Integer.parseInt(args[2]);
			totalThreshold = eyesThreshold;
			try {
				translateLegacy(new File(args[1]));
			} catch (IOException e) {
				System.err.println("An I/O error occurred when reading from that file");
				e.printStackTrace();
			}
		} else {
			seed = Long.parseLong(args[0]);
			eyesThreshold = Integer.parseInt(args[1]);
			totalThreshold = args.length == 2 ? eyesThreshold : Integer.parseInt(args[2]);
			findSeeds();
		}
	}

	private static void translateLegacy(File file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			Random rand = new Random();
			Storage3D world = new Storage3D(Blocks.AIR);
			String line;

			while ((line = reader.readLine()) != null) {
				// A "------------------" separator indicated the start of a
				// seed
				if (!"------------------".equals(line)) {
					continue;
				}

				if ((line = reader.readLine()) == null) {
					break;
				}
				if (!line.startsWith("Seed: ")) {
					System.err.println("Parse error:");
					System.err.println("  Expected \"Seed: <number>\" after divider");
					continue;
				}
				line = line.substring(6);
				try {
					seed = Long.parseLong(line);
				} catch (NumberFormatException e) {
					System.err.println("Parse error:");
					System.err.println("  Expected \"Seed: <number>\" after divider");
					continue;
				}

				printSeedInfo(rand, world);
			}
		}
	}

	private static void printSeedInfo(Random random, Storage3D world) {
		// Initialize things to the seed
		WorldGen.setWorldSeed(random, seed);
		BiomeProvider.setWorldSeed(seed);
		MineshaftFinder.INSTANCE.reset();
		VillageFinder.INSTANCE.reset();
		StrongholdFinder.INSTANCE.reset();
		TempleFinder.INSTANCE.reset();

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
