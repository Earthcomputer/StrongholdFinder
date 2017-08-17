package seedfinder;

import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

import seedfinder.biome.BiomeProvider;
import seedfinder.stronghold.StrongholdGen;
import seedfinder.stronghold.StrongholdPosFinder;
import seedfinder.stronghold.StrongholdPosFinder.Stronghold;
import seedfinder.worldgen.WorldGen;

public class Main {

	private static long seed;
	private static int eyesThreshold;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("java -jar seed_finder.jar <start_seed> <eyes_threshold>");
			System.out.println("java -jar seed_finder.jar seed <seed>");
			return;
		}

		if ("seed".equalsIgnoreCase(args[0])) {
			seed = Long.parseLong(args[1]);
			eyesThreshold = 0; // so we print all strongholds
			printSeedInfo(new Random(), new Storage3D(Blocks.AIR), new ChunkPos[3]);
		} else {
			seed = Long.parseLong(args[0]);
			eyesThreshold = Integer.parseInt(args[1]);
			findSeeds();
		}
	}

	private static void printSeedInfo(Random random, Storage3D world, ChunkPos[] strongholdPositions) {
		// Initialize things to the seed
		WorldGen.setWorldSeed(random, seed);
		BiomeProvider.setWorldSeed(seed);

		// Get stronghold positions
		StrongholdPosFinder.getStrongholdPositions(random, seed, strongholdPositions);

		for (ChunkPos strongholdPos : strongholdPositions) {
			int eyes = StrongholdPosFinder.getNumEyes(world, random, seed, strongholdPos);

			if (eyes >= eyesThreshold) {
				Stronghold stronghold = StrongholdPosFinder.getStronghold(random, seed, strongholdPos);

				// Output things about the stronghold
				System.out.println("------------------");
				System.out.println("Seed: " + seed);
				System.out.println("Stronghold: " + strongholdPos);
				System.out.println("Portal: " + stronghold.getPortalRoom().getPortalPos());
				System.out.println("Eyes: " + eyes);

				System.out.println("Corridor chest locations:");
				stronghold.getComponents().stream().filter(it -> it instanceof StrongholdGen.ChestCorridor)
						.map(it -> (StrongholdGen.ChestCorridor) it).map(StrongholdGen.ChestCorridor::getChestPos)
						.forEach(it -> System.out.println("- " + it));

				System.out.println("Library chest locations:");
				stronghold.getComponents().stream().filter(it -> it instanceof StrongholdGen.Library)
						.map(it -> (StrongholdGen.Library) it).flatMap(it -> {
							Stream.Builder<BlockPos> chestPositions = Stream.builder();
							chestPositions.add(it.getBottomChestPos());
							it.getTopChestPos().ifPresent(chestPositions);
							return chestPositions.build();
						}).forEach(it -> System.out.println("- " + it));

				System.out.println("Room crossing chest locations:");
				stronghold.getComponents().stream().filter(it -> it instanceof StrongholdGen.RoomCrossing)
						.map(it -> (StrongholdGen.RoomCrossing) it).map(StrongholdGen.RoomCrossing::getChestPos)
						.filter(Optional::isPresent).map(Optional::get).forEach(it -> System.out.println("- " + it));
			}
		}
	}

	private static void findSeeds() {
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

			System.out.println("Current seed: " + seed);
			System.out.println("Max Eyes: " + eyesThreshold);

			System.exit(0);
		}).start();

		Random random = new Random();
		Storage3D world = new Storage3D(Blocks.AIR);
		ChunkPos[] strongholdPositions = new ChunkPos[3];

		while (true) {
			printSeedInfo(random, world, strongholdPositions);
			seed++;
		}
	}

}