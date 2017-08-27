package seedfinder.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import seedfinder.AABB;
import seedfinder.BlockPos;
import seedfinder.Blocks;
import seedfinder.EnumFacing;
import seedfinder.MathHelper;
import seedfinder.Storage3D;
import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;

public class VillageGen {

	private static final VillagePalette PLAINS_PALETTE = new VillagePalette.Builder().build();
	private static final VillagePalette DESERT_PALETTE = new VillagePalette.Builder().setLog(Blocks.SANDSTONE)
			.setStone(Blocks.SANDSTONE).setPlanks(Blocks.SANDSTONE).setWoodStairs(Blocks.SANDSTONE_STAIRS)
			.setStoneStairs(Blocks.SANDSTONE_STAIRS).setPath(Blocks.SANDSTONE).build();
	private static final VillagePalette TAIGA_PALETTE = new VillagePalette.Builder().setWoodStairs(Blocks.SPRUCE_STAIRS)
			.setFence(Blocks.SPRUCE_FENCE).setDoor(Blocks.SPRUCE_DOOR).build();
	private static final VillagePalette SAVANNA_PALETTE = new VillagePalette.Builder().setLog(Blocks.LOG2)
			.setStone(Blocks.LOG2).setWoodStairs(Blocks.ACACIA_STAIRS).setFence(Blocks.ACACIA_FENCE)
			.setDoor(Blocks.ACACIA_DOOR).build();

	public static List<PoolEntry> createPoolEntries(Random rand, int size) {
		List<PoolEntry> entries = new ArrayList<>();

		// add entries
		entries.add(new PoolEntry(SmallHouse::create, 4, MathHelper.randomRange(rand, 2 + size, 4 + size * 2)));
		entries.add(new PoolEntry(Church::create, 20, MathHelper.randomRange(rand, 0 + size, 1 + size)));
		entries.add(new PoolEntry(Library::create, 20, MathHelper.randomRange(rand, 0 + size, 2 + size)));
		entries.add(new PoolEntry(WoodHut::create, 3, MathHelper.randomRange(rand, 2 + size, 5 + size * 3)));
		entries.add(new PoolEntry(Butchers::create, 15, MathHelper.randomRange(rand, 0 + size, 2 + size)));
		entries.add(new PoolEntry(LargeField::create, 3, MathHelper.randomRange(rand, 1 + size, 4 + size)));
		entries.add(new PoolEntry(SmallField::create, 3, MathHelper.randomRange(rand, 2 + size, 4 + size * 2)));
		entries.add(new PoolEntry(Blacksmith::create, 15, MathHelper.randomRange(rand, 0, 1 + size)));
		entries.add(new PoolEntry(LargeHouse::create, 8, MathHelper.randomRange(rand, 0 + size, 3 + size * 2)));

		// remove entries with limit of 0
		Iterator<PoolEntry> itr = entries.iterator();
		while (itr.hasNext()) {
			if (itr.next().limit == 0) {
				itr.remove();
			}
		}

		return entries;
	}

	private static int getTotalWeight(List<PoolEntry> entries) {
		if (entries.stream().anyMatch(it -> it.limit > 0 && it.amtCreated < it.limit)) {
			return entries.stream().mapToInt(it -> it.weight).sum();
		} else {
			return -1;
		}
	}

	/**
	 * Creates the next random component, or <tt>null</tt> if none can be
	 * created.
	 */
	private static VillageComponent nextRandomComponent(Start start, List<Component> components, Random rand, int x,
			int y, int z, EnumFacing facing, int distanceFromStart) {
		int totalWeight = getTotalWeight(start.componentTypePool);

		if (totalWeight <= 0) {
			// no components left to add!
			return null;
		}

		// Choose a new component to add
		for (int tries = 0; tries < 5; tries++) {

			int randNum = rand.nextInt(totalWeight);

			for (PoolEntry entry : start.componentTypePool) {
				randNum -= entry.weight;

				if (randNum < 0) {
					if (!entry.canAddAtDistance(distanceFromStart)) {
						break;
					}

					// Avoid adding the same component twice in a row
					if (entry == start.lastComponentTypeCreated && start.componentTypePool.size() > 1) {
						break;
					}

					// Create the component
					VillageComponent component = entry.creator.create(start, components, rand, x, y, z, facing,
							distanceFromStart);

					if (component != null) {
						entry.amtCreated++;
						start.lastComponentTypeCreated = entry;

						if (!entry.canAddMore()) {
							start.componentTypePool.remove(entry);
						}

						return component;
					}
				}
			}
		}

		// Try adding a lamp post if we can add nothing else here
		AABB lampPostBounds = LampPost.findNonIntersectingBB(start, components, rand, x, y, z, facing);

		if (lampPostBounds != null) {
			return new LampPost(start, distanceFromStart, rand, lampPostBounds, facing);
		} else {
			return null;
		}
	}

	/**
	 * Creates a new house and adds it to the list of components
	 */
	private static Component generateAndAddHouse(Start start, List<Component> components, Random rand, int x, int y,
			int z, EnumFacing facing, int distanceFromStart) {
		if (distanceFromStart > 50) {
			// The village is WAY too large
			return null;
		}
		if (Math.abs(x - start.getBoundingBox().getMinX()) > 112
				|| Math.abs(z - start.getBoundingBox().getMinZ()) > 112) {
			// The village is getting too large
			return null;
		}

		Component component = nextRandomComponent(start, components, rand, x, y, z, facing, distanceFromStart + 1);

		if (component != null) {
			components.add(component);
			start.pendingHouses.add(component);
		}

		return component;
	}

	/**
	 * Creates a new path and adds it to the list of components
	 */
	private static Component generateAndAddRoad(Start start, List<Component> components, Random rand, int x, int y,
			int z, EnumFacing facing, int distanceFromStart) {
		if (distanceFromStart > 3 + start.extraVillageSize) {
			// The village is getting too large
			return null;
		} else if (Math.abs(x - start.getBoundingBox().getMinX()) > 112
				|| Math.abs(z - start.getBoundingBox().getMinZ()) > 112) {
			// The village is getting too large
			return null;
		}

		AABB bounds = Path.findLongestNonIntersectingBB(start, components, rand, x, y, z, facing);

		if (bounds != null && bounds.getMinY() > 10) {
			Component path = new Path(start, distanceFromStart, rand, bounds, facing);
			components.add(path);
			start.pendingRoads.add(path);
			return path;
		} else {
			return null;
		}
	}

	public static class Church extends VillageComponent {
		public Church(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static Church create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 5, 12, 9, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Church(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(
						getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 12 - 1, 0));
			}

			int stoneStairs = palette.stoneStairs;
			// clear with air
			fillWithBlocks(world, bounds, 1, 1, 1, 3, 3, 7, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 1, 5, 1, 3, 9, 3, Blocks.AIR, Blocks.AIR, false);
			// bottom floor
			fillWithBlocks(world, bounds, 1, 0, 0, 3, 0, 8, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// walls
			fillWithBlocks(world, bounds, 1, 1, 0, 3, 10, 0, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			fillWithBlocks(world, bounds, 0, 1, 1, 0, 10, 3, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			fillWithBlocks(world, bounds, 4, 1, 1, 4, 10, 3, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			fillWithBlocks(world, bounds, 0, 0, 4, 0, 4, 7, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			fillWithBlocks(world, bounds, 4, 0, 4, 4, 4, 7, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			fillWithBlocks(world, bounds, 1, 1, 8, 3, 4, 8, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			fillWithBlocks(world, bounds, 1, 5, 4, 3, 10, 4, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// middle floor
			fillWithBlocks(world, bounds, 1, 5, 5, 3, 5, 7, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// top floor
			fillWithBlocks(world, bounds, 0, 9, 0, 4, 9, 4, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// middle floor
			fillWithBlocks(world, bounds, 0, 4, 0, 4, 4, 4, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// crenellation
			setBlock(world, Blocks.COBBLESTONE, 0, 11, 2, bounds);
			setBlock(world, Blocks.COBBLESTONE, 4, 11, 2, bounds);
			setBlock(world, Blocks.COBBLESTONE, 2, 11, 0, bounds);
			setBlock(world, Blocks.COBBLESTONE, 2, 11, 4, bounds);
			// altar
			setBlock(world, Blocks.COBBLESTONE, 1, 1, 6, bounds);
			setBlock(world, Blocks.COBBLESTONE, 1, 1, 7, bounds);
			setBlock(world, Blocks.COBBLESTONE, 2, 1, 7, bounds);
			setBlock(world, Blocks.COBBLESTONE, 3, 1, 6, bounds);
			setBlock(world, Blocks.COBBLESTONE, 3, 1, 7, bounds);
			setBlock(world, stoneStairs, 1, 1, 5, bounds);
			setBlock(world, stoneStairs, 2, 1, 6, bounds);
			setBlock(world, stoneStairs, 3, 1, 5, bounds);
			setBlock(world, stoneStairs, 1, 2, 7, bounds);
			setBlock(world, stoneStairs, 3, 2, 7, bounds);
			// windows
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 3, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 3, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 6, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 7, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 6, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 7, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 6, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 7, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 6, 4, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 7, 4, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 3, 6, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 3, 6, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 3, 8, bounds);
			// torches in first floor
			placeTorch(world, EnumFacing.SOUTH, 2, 4, 7, bounds);
			placeTorch(world, EnumFacing.EAST, 1, 4, 6, bounds);
			placeTorch(world, EnumFacing.WEST, 3, 4, 6, bounds);
			placeTorch(world, EnumFacing.NORTH, 2, 4, 5, bounds);

			// ladder
			for (int y = 1; y <= 9; y++) {
				setBlock(world, Blocks.LADDER, 3, y, 3, bounds);
			}

			// door
			setBlock(world, Blocks.AIR, 2, 1, 0, bounds);
			setBlock(world, Blocks.AIR, 2, 2, 0, bounds);
			placeDoor(world, bounds, rand, 2, 1, 0, EnumFacing.NORTH);

			// door step
			if (Blocks.isAir(getBlock(world, 2, 0, -1, bounds)) && !Blocks.isAir(getBlock(world, 2, -1, -1, bounds))) {
				setBlock(world, stoneStairs, 2, 0, -1, bounds);

				if (getBlock(world, 2, -1, -1, bounds) == Blocks.GRASS_PATH) {
					setBlock(world, Blocks.GRASS, 2, -1, -1, bounds);
				}
			}

			// clear space
			for (int z = 0; z < 9; z++) {
				for (int x = 0; x < 5; x++) {
					clearAbove(world, x, 12, z, bounds);
					fillBelow(world, palette.stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 2, 1, 2, 1);
			return true;
		}

		@Override
		protected VillagerProfession chooseProfession(int villagersSpawned, VillagerProfession _default) {
			return VillagerProfession.PRIEST;
		}
	}

	public static class LargeField extends VillageComponent {
		private int cropType1;
		private int cropType2;
		private int cropType3;
		private int cropType4;

		public LargeField(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
			cropType1 = getRandomCropType(rand);
			cropType2 = getRandomCropType(rand);
			cropType3 = getRandomCropType(rand);
			cropType4 = getRandomCropType(rand);
		}

		private int getRandomCropType(Random rand) {
			switch (rand.nextInt(10)) {
			case 0:
			case 1:
				return Blocks.CARROTS;
			case 2:
			case 3:
				return Blocks.POTATOES;
			case 4:
				return Blocks.BEETROOTS;
			default:
				return Blocks.WHEAT;
			}
		}

		public static LargeField create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 13, 4, 9, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new LargeField(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 4 - 1, 0));
			}

			int log = palette.log;
			// clear space
			fillWithBlocks(world, bounds, 0, 1, 0, 12, 4, 8, Blocks.AIR, Blocks.AIR, false);
			// farmland
			fillWithBlocks(world, bounds, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND, Blocks.FARMLAND, false);
			fillWithBlocks(world, bounds, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND, Blocks.FARMLAND, false);
			fillWithBlocks(world, bounds, 7, 0, 1, 8, 0, 7, Blocks.FARMLAND, Blocks.FARMLAND, false);
			fillWithBlocks(world, bounds, 10, 0, 1, 11, 0, 7, Blocks.FARMLAND, Blocks.FARMLAND, false);
			// borders
			fillWithBlocks(world, bounds, 0, 0, 0, 0, 0, 8, log, log, false);
			fillWithBlocks(world, bounds, 6, 0, 0, 6, 0, 8, log, log, false);
			fillWithBlocks(world, bounds, 12, 0, 0, 12, 0, 8, log, log, false);
			fillWithBlocks(world, bounds, 1, 0, 0, 11, 0, 0, log, log, false);
			fillWithBlocks(world, bounds, 1, 0, 8, 11, 0, 8, log, log, false);
			// irrigation
			fillWithBlocks(world, bounds, 3, 0, 1, 3, 0, 7, Blocks.WATER, Blocks.WATER, false);
			fillWithBlocks(world, bounds, 9, 0, 1, 9, 0, 7, Blocks.WATER, Blocks.WATER, false);

			// crops
			for (int z = 1; z <= 7; z++) {
				int maxAge1 = getMaxCropsAge(cropType1);
				int minAge1 = maxAge1 / 3;
				setBlock(world, cropType1, 1, 1, z, bounds);
				MathHelper.randomRange(rand, minAge1, maxAge1);
				setBlock(world, cropType1, 2, 1, z, bounds);
				MathHelper.randomRange(rand, minAge1, maxAge1);

				int maxAge2 = getMaxCropsAge(cropType2);
				int minAge2 = maxAge2 / 3;
				setBlock(world, cropType2, 4, 1, z, bounds);
				MathHelper.randomRange(rand, minAge2, maxAge2);
				setBlock(world, cropType2, 5, 1, z, bounds);
				MathHelper.randomRange(rand, minAge2, maxAge2);

				int maxAge3 = getMaxCropsAge(cropType3);
				int minAge3 = maxAge3 / 3;
				setBlock(world, cropType3, 7, 1, z, bounds);
				MathHelper.randomRange(rand, minAge3, maxAge3);
				setBlock(world, cropType3, 8, 1, z, bounds);
				MathHelper.randomRange(rand, minAge3, maxAge3);

				int maxAge4 = getMaxCropsAge(cropType4);
				int minAge4 = maxAge4 / 3;
				setBlock(world, cropType4, 10, 1, z, bounds);
				MathHelper.randomRange(rand, minAge4, maxAge4);
				setBlock(world, cropType4, 11, 1, z, bounds);
				MathHelper.randomRange(rand, minAge4, maxAge4);
			}

			// clear space
			for (int z = 0; z < 9; z++) {
				for (int x = 0; x < 13; x++) {
					clearAbove(world, x, 4, z, bounds);
					fillBelow(world, Blocks.DIRT, x, -1, z, bounds);
				}
			}

			return true;
		}
	}

	public static class SmallField extends VillageComponent {
		private int cropType1;
		private int cropType2;

		public SmallField(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
			cropType1 = getRandomCropType(rand);
			cropType2 = getRandomCropType(rand);
		}

		private int getRandomCropType(Random rand) {
			switch (rand.nextInt(10)) {
			case 0:
			case 1:
				return Blocks.CARROTS;
			case 2:
			case 3:
				return Blocks.POTATOES;
			case 4:
				return Blocks.BEETROOTS;
			default:
				return Blocks.WHEAT;
			}
		}

		public static SmallField create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 7, 4, 9, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new SmallField(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 4 - 1, 0));
			}

			int log = palette.log;
			// clear space
			fillWithBlocks(world, bounds, 0, 1, 0, 6, 4, 8, Blocks.AIR, Blocks.AIR, false);
			// farmland
			fillWithBlocks(world, bounds, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND, Blocks.FARMLAND, false);
			fillWithBlocks(world, bounds, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND, Blocks.FARMLAND, false);
			// borders
			fillWithBlocks(world, bounds, 0, 0, 0, 0, 0, 8, log, log, false);
			fillWithBlocks(world, bounds, 6, 0, 0, 6, 0, 8, log, log, false);
			fillWithBlocks(world, bounds, 1, 0, 0, 5, 0, 0, log, log, false);
			fillWithBlocks(world, bounds, 1, 0, 8, 5, 0, 8, log, log, false);
			// irrigation
			fillWithBlocks(world, bounds, 3, 0, 1, 3, 0, 7, Blocks.WATER, Blocks.WATER, false);

			// crops
			for (int z = 1; z <= 7; z++) {
				int maxAge1 = getMaxCropsAge(cropType1);
				int minAge1 = maxAge1 / 3;
				setBlock(world, cropType1, 1, 1, z, bounds);
				MathHelper.randomRange(rand, minAge1, maxAge1);
				setBlock(world, cropType1, 2, 1, z, bounds);
				MathHelper.randomRange(rand, minAge1, maxAge1);

				int maxAge2 = getMaxCropsAge(cropType2);
				int minAge2 = maxAge2 / 3;
				setBlock(world, cropType2, 4, 1, z, bounds);
				MathHelper.randomRange(rand, minAge2, maxAge2);
				setBlock(world, cropType2, 5, 1, z, bounds);
				MathHelper.randomRange(rand, minAge2, maxAge2);
			}

			// clear space
			for (int z = 0; z < 9; z++) {
				for (int x = 0; x < 7; x++) {
					clearAbove(world, x, 4, z, bounds);
					fillBelow(world, Blocks.DIRT, x, -1, z, bounds);
				}
			}

			return true;
		}
	}

	public static class Butchers extends VillageComponent {
		public Butchers(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static Butchers create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 9, 7, 11, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Butchers(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 7 - 1, 0));
			}

			int stone = palette.stone;
			int woodStairs = palette.woodStairs;
			int planks = palette.planks;
			int log = palette.log;
			int fence = palette.fence;
			// clear space for house
			fillWithBlocks(world, bounds, 1, 1, 1, 7, 4, 4, Blocks.AIR, Blocks.AIR, false);
			// clear space for garden
			fillWithBlocks(world, bounds, 2, 1, 6, 8, 4, 10, Blocks.AIR, Blocks.AIR, false);
			// garden
			fillWithBlocks(world, bounds, 2, 0, 6, 8, 0, 10, Blocks.DIRT, Blocks.DIRT, false);
			// ?
			setBlock(world, stone, 6, 0, 6, bounds);
			// garden fence
			fillWithBlocks(world, bounds, 2, 1, 6, 2, 1, 10, fence, fence, false);
			fillWithBlocks(world, bounds, 8, 1, 6, 8, 1, 10, fence, fence, false);
			fillWithBlocks(world, bounds, 3, 1, 10, 7, 1, 10, fence, fence, false);
			// floor
			fillWithBlocks(world, bounds, 1, 0, 1, 7, 0, 4, planks, planks, false);
			// walls
			fillWithBlocks(world, bounds, 0, 0, 0, 0, 3, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 8, 0, 0, 8, 3, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 0, 0, 7, 1, 0, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 0, 5, 7, 1, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 2, 0, 7, 3, 0, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 2, 5, 7, 3, 5, planks, planks, false);
			fillWithBlocks(world, bounds, 0, 4, 1, 8, 4, 1, planks, planks, false);
			fillWithBlocks(world, bounds, 0, 4, 4, 8, 4, 4, planks, planks, false);
			fillWithBlocks(world, bounds, 0, 5, 2, 8, 5, 3, planks, planks, false);
			setBlock(world, planks, 0, 4, 2, bounds);
			setBlock(world, planks, 0, 4, 3, bounds);
			setBlock(world, planks, 8, 4, 2, bounds);
			setBlock(world, planks, 8, 4, 3, bounds);

			// roof
			for (int i = -1; i <= 2; i++) {
				for (int x = 0; x <= 8; x++) {
					setBlock(world, woodStairs, x, 4 + i, i, bounds);
					setBlock(world, woodStairs, x, 4 + i, 5 - i, bounds);
				}
			}

			// windows
			setBlock(world, log, 0, 2, 1, bounds);
			setBlock(world, log, 0, 2, 4, bounds);
			setBlock(world, log, 8, 2, 1, bounds);
			setBlock(world, log, 8, 2, 4, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 3, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 3, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 2, 5, bounds);
			setBlock(world, Blocks.GLASS_PANE, 3, 2, 5, bounds);
			setBlock(world, Blocks.GLASS_PANE, 5, 2, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 6, 2, 5, bounds);
			// table
			setBlock(world, fence, 2, 1, 3, bounds);
			setBlock(world, Blocks.WOODEN_PRESSURE_PLATE, 2, 2, 3, bounds);
			// chairs
			setBlock(world, planks, 1, 1, 4, bounds);
			setBlock(world, woodStairs, 2, 1, 4, bounds);
			setBlock(world, woodStairs, 1, 1, 3, bounds);
			// kitchen floor
			fillWithBlocks(world, bounds, 5, 0, 1, 7, 0, 3, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB, false);
			// kitchen surface
			setBlock(world, Blocks.DOUBLE_STONE_SLAB, 6, 1, 1, bounds);
			setBlock(world, Blocks.DOUBLE_STONE_SLAB, 6, 1, 2, bounds);
			// clear space for door
			setBlock(world, Blocks.AIR, 2, 1, 0, bounds);
			setBlock(world, Blocks.AIR, 2, 2, 0, bounds);
			// torch above door
			placeTorch(world, EnumFacing.NORTH, 2, 3, 1, bounds);
			// front door
			placeDoor(world, bounds, rand, 2, 1, 0, EnumFacing.NORTH);

			// door step
			if (Blocks.isAir(getBlock(world, 2, 0, -1, bounds)) && !Blocks.isAir(getBlock(world, 2, -1, -1, bounds))) {
				setBlock(world, woodStairs, 2, 0, -1, bounds);

				if (getBlock(world, 2, -1, -1, bounds) == Blocks.GRASS_PATH) {
					setBlock(world, Blocks.GRASS, 2, -1, -1, bounds);
				}
			}

			// clear space for door
			setBlock(world, Blocks.AIR, 6, 1, 5, bounds);
			setBlock(world, Blocks.AIR, 6, 2, 5, bounds);
			// torch above door
			placeTorch(world, EnumFacing.SOUTH, 6, 3, 4, bounds);
			// back door
			placeDoor(world, bounds, rand, 6, 1, 5, EnumFacing.SOUTH);

			// clear space
			for (int z = 0; z < 5; z++) {
				for (int x = 0; x < 9; x++) {
					clearAbove(world, x, 7, z, bounds);
					fillBelow(world, stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 4, 1, 2, 2);
			return true;
		}

		@Override
		protected VillagerProfession chooseProfession(int villagersSpawned, VillagerProfession _default) {
			if (villagersSpawned == 0) {
				return VillagerProfession.BUTCHER;
			} else {
				return super.chooseProfession(villagersSpawned, _default);
			}
		}
	}

	public static class Library extends VillageComponent {
		public Library(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static Library create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 9, 9, 6, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Library(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 9 - 1, 0));
			}

			int stone = palette.stone;
			int woodStairs = palette.woodStairs;
			int planks = palette.planks;
			int stoneStairs = palette.stoneStairs;
			int fence = palette.fence;
			// clear space
			fillWithBlocks(world, bounds, 1, 1, 1, 7, 5, 4, Blocks.AIR, Blocks.AIR, false);
			// floor
			fillWithBlocks(world, bounds, 0, 0, 0, 8, 0, 5, stone, stone, false);
			// ceiling
			fillWithBlocks(world, bounds, 0, 5, 0, 8, 5, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 0, 6, 1, 8, 6, 4, stone, stone, false);
			fillWithBlocks(world, bounds, 0, 7, 2, 8, 7, 3, stone, stone, false);

			// roof
			for (int i = -1; i <= 2; i++) {
				for (int x = 0; x <= 8; x++) {
					setBlock(world, woodStairs, x, 6 + i, i, bounds);
					setBlock(world, woodStairs, x, 6 + i, 5 - i, bounds);
				}
			}

			// walls
			fillWithBlocks(world, bounds, 0, 1, 0, 0, 1, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 1, 5, 8, 1, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 8, 1, 0, 8, 1, 4, stone, stone, false);
			fillWithBlocks(world, bounds, 2, 1, 0, 7, 1, 0, stone, stone, false);
			fillWithBlocks(world, bounds, 0, 2, 0, 0, 4, 0, stone, stone, false);
			fillWithBlocks(world, bounds, 0, 2, 5, 0, 4, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 8, 2, 5, 8, 4, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 8, 2, 0, 8, 4, 0, stone, stone, false);
			fillWithBlocks(world, bounds, 0, 2, 1, 0, 4, 4, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 2, 5, 7, 4, 5, planks, planks, false);
			fillWithBlocks(world, bounds, 8, 2, 1, 8, 4, 4, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 2, 0, 7, 4, 0, planks, planks, false);
			// windows
			setBlock(world, Blocks.GLASS_PANE, 4, 2, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 5, 2, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 6, 2, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 3, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 5, 3, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 6, 3, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 3, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 3, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 3, 3, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 3, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 3, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 3, 3, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 2, 5, bounds);
			setBlock(world, Blocks.GLASS_PANE, 3, 2, 5, bounds);
			setBlock(world, Blocks.GLASS_PANE, 5, 2, 5, bounds);
			setBlock(world, Blocks.GLASS_PANE, 6, 2, 5, bounds);
			// support beams
			fillWithBlocks(world, bounds, 1, 4, 1, 7, 4, 1, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 4, 4, 7, 4, 4, planks, planks, false);
			// bookshelves
			fillWithBlocks(world, bounds, 1, 3, 4, 7, 3, 4, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
			// seats
			setBlock(world, planks, 7, 1, 4, bounds);
			setBlock(world, woodStairs, 7, 1, 3, bounds);
			setBlock(world, woodStairs, 6, 1, 4, bounds);
			setBlock(world, woodStairs, 5, 1, 4, bounds);
			setBlock(world, woodStairs, 4, 1, 4, bounds);
			setBlock(world, woodStairs, 3, 1, 4, bounds);
			// tables
			setBlock(world, fence, 6, 1, 3, bounds);
			setBlock(world, Blocks.WOODEN_PRESSURE_PLATE, 6, 2, 3, bounds);
			setBlock(world, fence, 4, 1, 3, bounds);
			setBlock(world, Blocks.WOODEN_PRESSURE_PLATE, 4, 2, 3, bounds);
			// crafting table
			setBlock(world, Blocks.CRAFTING_TABLE, 7, 1, 1, bounds);
			// clear space for door
			setBlock(world, Blocks.AIR, 1, 1, 0, bounds);
			setBlock(world, Blocks.AIR, 1, 2, 0, bounds);
			// door
			placeDoor(world, bounds, rand, 1, 1, 0, EnumFacing.NORTH);

			// door step
			if (Blocks.isAir(getBlock(world, 1, 0, -1, bounds)) && !Blocks.isAir(getBlock(world, 1, -1, -1, bounds))) {
				setBlock(world, stoneStairs, 1, 0, -1, bounds);

				if (getBlock(world, 1, -1, -1, bounds) == Blocks.GRASS_PATH) {
					setBlock(world, Blocks.GRASS, 1, -1, -1, bounds);
				}
			}

			// clear space
			for (int z = 0; z < 6; z++) {
				for (int x = 0; x < 9; x++) {
					clearAbove(world, x, 9, z, bounds);
					fillBelow(world, stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 2, 1, 2, 1);
			return true;
		}

		@Override
		protected VillagerProfession chooseProfession(int villagersSpawned, VillagerProfession _default) {
			return VillagerProfession.LIBRARIAN;
		}
	}

	public static class Blacksmith extends VillageComponent {
		private boolean hasMadeChest;

		public Blacksmith(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static Blacksmith create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 10, 6, 7, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Blacksmith(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 6 - 1, 0));
			}

			int woodStairs = palette.woodStairs;
			int planks = palette.planks;
			int stoneStairs = palette.stoneStairs;
			int log = palette.log;
			int fence = palette.fence;
			// clear space
			fillWithBlocks(world, bounds, 0, 1, 0, 9, 4, 6, Blocks.AIR, Blocks.AIR, false);
			// floor
			fillWithBlocks(world, bounds, 0, 0, 0, 9, 0, 6, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// ceiling
			fillWithBlocks(world, bounds, 0, 4, 0, 9, 4, 6, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			// stone slab rim
			fillWithBlocks(world, bounds, 0, 5, 0, 9, 5, 6, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			fillWithBlocks(world, bounds, 1, 5, 1, 8, 5, 5, Blocks.AIR, Blocks.AIR, false);
			// front wall
			fillWithBlocks(world, bounds, 1, 1, 0, 2, 3, 0, planks, planks, false);
			// corner pillars
			fillWithBlocks(world, bounds, 0, 1, 0, 0, 4, 0, log, log, false);
			fillWithBlocks(world, bounds, 3, 1, 0, 3, 4, 0, log, log, false);
			fillWithBlocks(world, bounds, 0, 1, 6, 0, 4, 6, log, log, false);
			// block above door
			setBlock(world, planks, 3, 3, 1, bounds);
			// walls
			fillWithBlocks(world, bounds, 3, 1, 2, 3, 3, 2, planks, planks, false);
			fillWithBlocks(world, bounds, 4, 1, 3, 5, 3, 3, planks, planks, false);
			fillWithBlocks(world, bounds, 0, 1, 1, 0, 3, 5, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 1, 6, 5, 3, 6, planks, planks, false);
			// support pillars
			fillWithBlocks(world, bounds, 5, 1, 0, 5, 3, 0, fence, fence, false);
			fillWithBlocks(world, bounds, 9, 1, 0, 9, 3, 0, fence, fence, false);
			// lava pool
			fillWithBlocks(world, bounds, 6, 1, 4, 9, 4, 6, Blocks.COBBLESTONE, Blocks.COBBLESTONE, false);
			setBlock(world, Blocks.FLOWING_LAVA, 7, 1, 5, bounds);
			setBlock(world, Blocks.FLOWING_LAVA, 8, 1, 5, bounds);
			setBlock(world, Blocks.IRON_BARS, 9, 2, 5, bounds);
			setBlock(world, Blocks.IRON_BARS, 9, 2, 4, bounds);
			fillWithBlocks(world, bounds, 7, 2, 4, 8, 2, 5, Blocks.AIR, Blocks.AIR, false);
			// furnaces
			setBlock(world, Blocks.COBBLESTONE, 6, 1, 3, bounds);
			setBlock(world, Blocks.FURNACE, 6, 2, 3, bounds);
			setBlock(world, Blocks.FURNACE, 6, 3, 3, bounds);
			// outside table
			setBlock(world, Blocks.DOUBLE_STONE_SLAB, 8, 1, 1, bounds);
			// windows
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 4, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 2, 6, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 2, 6, bounds);
			// table
			setBlock(world, fence, 2, 1, 4, bounds);
			setBlock(world, Blocks.WOODEN_PRESSURE_PLATE, 2, 2, 4, bounds);
			// chairs
			setBlock(world, woodStairs, 1, 1, 5, bounds);
			setBlock(world, woodStairs, 2, 1, 5, bounds);
			setBlock(world, woodStairs, 1, 1, 4, bounds);

			// chest
			if (!hasMadeChest && bounds.contains(getXWithOffset(5, 5), getYWithOffset(1), getZWithOffset(5, 5))) {
				hasMadeChest = true;
				generateChest(world, bounds, rand, 5, 1, 5);
			}

			// stairs onto entrance
			for (int x = 6; x <= 8; x++) {
				if (Blocks.isAir(getBlock(world, x, 0, -1, bounds))
						&& !Blocks.isAir(getBlock(world, x, -1, -1, bounds))) {
					setBlock(world, stoneStairs, x, 0, -1, bounds);

					if (getBlock(world, x, -1, -1, bounds) == Blocks.GRASS_PATH) {
						setBlock(world, Blocks.GRASS, x, -1, -1, bounds);
					}
				}
			}

			// clear space
			for (int z = 0; z < 7; z++) {
				for (int x = 0; x < 10; x++) {
					clearAbove(world, x, 6, z, bounds);
					fillBelow(world, palette.stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 7, 1, 1, 1);
			return true;
		}

		@Override
		protected VillagerProfession chooseProfession(int villagersSpawned, VillagerProfession _default) {
			return VillagerProfession.BLACKSMITH;
		}
	}

	public static class LargeHouse extends VillageComponent {
		public LargeHouse(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static LargeHouse create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 9, 7, 12, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new LargeHouse(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 7 - 1, 0));
			}

			int stone = palette.stone;
			int woodStairs = palette.woodStairs;
			int planks = palette.planks;
			int log = palette.log;
			// clear space
			fillWithBlocks(world, bounds, 1, 1, 1, 7, 4, 4, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 2, 1, 6, 8, 4, 10, Blocks.AIR, Blocks.AIR, false);
			// floor
			fillWithBlocks(world, bounds, 2, 0, 5, 8, 0, 10, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 0, 1, 7, 0, 4, planks, planks, false);
			// walls
			fillWithBlocks(world, bounds, 0, 0, 0, 0, 3, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 8, 0, 0, 8, 3, 10, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 0, 0, 7, 2, 0, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 0, 5, 2, 1, 5, stone, stone, false);
			fillWithBlocks(world, bounds, 2, 0, 6, 2, 3, 10, stone, stone, false);
			fillWithBlocks(world, bounds, 3, 0, 10, 7, 3, 10, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 2, 0, 7, 3, 0, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 2, 5, 2, 3, 5, planks, planks, false);
			// front ceiling
			fillWithBlocks(world, bounds, 0, 4, 1, 8, 4, 1, planks, planks, false);
			fillWithBlocks(world, bounds, 0, 4, 4, 3, 4, 4, planks, planks, false);
			fillWithBlocks(world, bounds, 0, 5, 2, 8, 5, 3, planks, planks, false);
			setBlock(world, planks, 0, 4, 2, bounds);
			setBlock(world, planks, 0, 4, 3, bounds);
			setBlock(world, planks, 8, 4, 2, bounds);
			setBlock(world, planks, 8, 4, 3, bounds);
			setBlock(world, planks, 8, 4, 4, bounds);

			// front roof
			for (int i = -1; i <= 2; i++) {
				for (int x = 0; x <= 8; x++) {
					setBlock(world, woodStairs, x, 4 + i, i, bounds);

					// avoid placing back part of front roof if it would
					// intersect with the back roof
					if ((i > -1 || x <= 1) && (i > 0 || x <= 3) && (i > 1 || x <= 4 || x >= 6)) {
						setBlock(world, woodStairs, x, 4 + i, 5 - i, bounds);
					}
				}
			}

			// back ceiling
			fillWithBlocks(world, bounds, 3, 4, 5, 3, 4, 10, planks, planks, false);
			fillWithBlocks(world, bounds, 7, 4, 2, 7, 4, 10, planks, planks, false);
			fillWithBlocks(world, bounds, 4, 5, 4, 4, 5, 10, planks, planks, false);
			fillWithBlocks(world, bounds, 6, 5, 4, 6, 5, 10, planks, planks, false);
			fillWithBlocks(world, bounds, 5, 6, 3, 5, 6, 10, planks, planks, false);

			// back roof
			for (int i = 4; i >= 1; i--) {
				setBlock(world, planks, i, 2 + i, 7 - i, bounds);

				for (int z = 8 - i; z <= 10; z++) {
					setBlock(world, woodStairs, i, 2 + i, z, bounds);
				}
			}

			setBlock(world, planks, 6, 6, 3, bounds);
			setBlock(world, planks, 7, 5, 4, bounds);
			setBlock(world, woodStairs, 6, 6, 4, bounds);

			for (int i = 6; i <= 8; i++) {
				for (int z = 5; z <= 10; z++) {
					setBlock(world, woodStairs, i, 12 - i, z, bounds);
				}
			}

			// windows
			setBlock(world, log, 0, 2, 1, bounds);
			setBlock(world, log, 0, 2, 4, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 3, bounds);
			setBlock(world, log, 4, 2, 0, bounds);
			setBlock(world, Blocks.GLASS_PANE, 5, 2, 0, bounds);
			setBlock(world, log, 6, 2, 0, bounds);
			setBlock(world, log, 8, 2, 1, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 3, bounds);
			setBlock(world, log, 8, 2, 4, bounds);
			setBlock(world, planks, 8, 2, 5, bounds);
			setBlock(world, log, 8, 2, 6, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 7, bounds);
			setBlock(world, Blocks.GLASS_PANE, 8, 2, 8, bounds);
			setBlock(world, log, 8, 2, 9, bounds);
			setBlock(world, log, 2, 2, 6, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 2, 7, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 2, 8, bounds);
			setBlock(world, log, 2, 2, 9, bounds);
			setBlock(world, log, 4, 4, 10, bounds);
			setBlock(world, Blocks.GLASS_PANE, 5, 4, 10, bounds);
			setBlock(world, log, 6, 4, 10, bounds);
			// finish back wall
			setBlock(world, planks, 5, 5, 10, bounds);
			// clear space for door
			setBlock(world, Blocks.AIR, 2, 1, 0, bounds);
			setBlock(world, Blocks.AIR, 2, 2, 0, bounds);
			// torch above door
			placeTorch(world, EnumFacing.NORTH, 2, 3, 1, bounds);
			// door
			placeDoor(world, bounds, rand, 2, 1, 0, EnumFacing.NORTH);

			// door step
			fillWithBlocks(world, bounds, 1, 0, -1, 3, 2, -1, Blocks.AIR, Blocks.AIR, false);
			if (Blocks.isAir(getBlock(world, 2, 0, -1, bounds)) && !Blocks.isAir(getBlock(world, 2, -1, -1, bounds))) {
				setBlock(world, woodStairs, 2, 0, -1, bounds);

				if (getBlock(world, 2, -1, -1, bounds) == Blocks.GRASS_PATH) {
					setBlock(world, Blocks.GRASS, 2, -1, -1, bounds);
				}
			}

			// clear space (front)
			for (int z = 0; z < 5; z++) {
				for (int x = 0; x < 9; x++) {
					clearAbove(world, x, 7, z, bounds);
					fillBelow(world, stone, x, -1, z, bounds);
				}
			}

			// clear space (back)
			for (int z = 5; z < 11; z++) {
				for (int x = 2; x < 9; x++) {
					clearAbove(world, x, 7, z, bounds);
					fillBelow(world, stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 4, 1, 2, 2);
			return true;
		}
	}

	public static class SmallHouse extends VillageComponent {
		private boolean isRoofAccessible;

		public SmallHouse(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
			isRoofAccessible = rand.nextBoolean();
		}

		public static SmallHouse create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 5, 6, 5, facing);

			if (Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new SmallHouse(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 6 - 1, 0));
			}

			int stone = palette.stone;
			int planks = palette.planks;
			int stoneStairs = palette.stoneStairs;
			int log = palette.log;
			int fence = palette.fence;
			// floor
			fillWithBlocks(world, bounds, 0, 0, 0, 4, 0, 4, stone, stone, false);
			// roof
			fillWithBlocks(world, bounds, 0, 4, 0, 4, 4, 4, log, log, false);
			fillWithBlocks(world, bounds, 1, 4, 1, 3, 4, 3, planks, planks, false);
			// corner supports
			setBlock(world, stone, 0, 1, 0, bounds);
			setBlock(world, stone, 0, 2, 0, bounds);
			setBlock(world, stone, 0, 3, 0, bounds);
			setBlock(world, stone, 4, 1, 0, bounds);
			setBlock(world, stone, 4, 2, 0, bounds);
			setBlock(world, stone, 4, 3, 0, bounds);
			setBlock(world, stone, 0, 1, 4, bounds);
			setBlock(world, stone, 0, 2, 4, bounds);
			setBlock(world, stone, 0, 3, 4, bounds);
			setBlock(world, stone, 4, 1, 4, bounds);
			setBlock(world, stone, 4, 2, 4, bounds);
			setBlock(world, stone, 4, 3, 4, bounds);
			// walls
			fillWithBlocks(world, bounds, 0, 1, 1, 0, 3, 3, planks, planks, false);
			fillWithBlocks(world, bounds, 4, 1, 1, 4, 3, 3, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 1, 4, 3, 3, 4, planks, planks, false);
			// windows
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 2, 2, 4, bounds);
			setBlock(world, Blocks.GLASS_PANE, 4, 2, 2, bounds);
			// front wall
			setBlock(world, planks, 1, 1, 0, bounds);
			setBlock(world, planks, 1, 2, 0, bounds);
			setBlock(world, planks, 1, 3, 0, bounds);
			setBlock(world, planks, 2, 3, 0, bounds);
			setBlock(world, planks, 3, 3, 0, bounds);
			setBlock(world, planks, 3, 2, 0, bounds);
			setBlock(world, planks, 3, 1, 0, bounds);
			// no door

			// door step
			if (Blocks.isAir(getBlock(world, 2, 0, -1, bounds)) && !Blocks.isAir(getBlock(world, 2, -1, -1, bounds))) {
				setBlock(world, stoneStairs, 2, 0, -1, bounds);

				if (getBlock(world, 2, -1, -1, bounds) == Blocks.GRASS_PATH) {
					setBlock(world, Blocks.GRASS, 2, -1, -1, bounds);
				}
			}

			// clear middle
			fillWithBlocks(world, bounds, 1, 1, 1, 3, 3, 3, Blocks.AIR, Blocks.AIR, false);

			if (isRoofAccessible) {
				// fence on roof
				setBlock(world, fence, 0, 5, 0, bounds);
				setBlock(world, fence, 1, 5, 0, bounds);
				setBlock(world, fence, 2, 5, 0, bounds);
				setBlock(world, fence, 3, 5, 0, bounds);
				setBlock(world, fence, 4, 5, 0, bounds);
				setBlock(world, fence, 0, 5, 4, bounds);
				setBlock(world, fence, 1, 5, 4, bounds);
				setBlock(world, fence, 2, 5, 4, bounds);
				setBlock(world, fence, 3, 5, 4, bounds);
				setBlock(world, fence, 4, 5, 4, bounds);
				setBlock(world, fence, 4, 5, 1, bounds);
				setBlock(world, fence, 4, 5, 2, bounds);
				setBlock(world, fence, 4, 5, 3, bounds);
				setBlock(world, fence, 0, 5, 1, bounds);
				setBlock(world, fence, 0, 5, 2, bounds);
				setBlock(world, fence, 0, 5, 3, bounds);
			}

			if (isRoofAccessible) {
				// ladder to roof
				setBlock(world, Blocks.LADDER, 3, 1, 3, bounds);
				setBlock(world, Blocks.LADDER, 3, 2, 3, bounds);
				setBlock(world, Blocks.LADDER, 3, 3, 3, bounds);
				setBlock(world, Blocks.LADDER, 3, 4, 3, bounds);
			}

			// torch inside
			placeTorch(world, EnumFacing.NORTH, 2, 3, 1, bounds);

			// clear space
			for (int z = 0; z < 5; z++) {
				for (int x = 0; x < 5; x++) {
					clearAbove(world, x, 6, z, bounds);
					fillBelow(world, stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 1, 1, 2, 1);
			return true;
		}
	}

	public static class Path extends Road {
		private int length;

		public Path(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
			length = Math.max(bounds.getXSize(), bounds.getZSize());
		}

		@Override
		public void addMoreComponents(Component start, List<Component> components, Random rand) {
			boolean addedHouse = false;

			// add buildings in negative direction
			for (int hOffset = rand.nextInt(5); hOffset < length - 8; hOffset += 2 + rand.nextInt(5)) {
				Component house = addHouseNegative((Start) start, components, rand, 0, hOffset);

				if (house != null) {
					// increment offset further to avoid house collision
					hOffset += Math.max(house.getBoundingBox().getXSize(), house.getBoundingBox().getZSize());
					addedHouse = true;
				}
			}

			// add buildings in positive direction
			for (int hOffset = rand.nextInt(5); hOffset < length - 8; hOffset += 2 + rand.nextInt(5)) {
				Component house = addHousePositive((Start) start, components, rand, 0, hOffset);

				if (house != null) {
					// increment offset further to avoid house collision
					hOffset += Math.max(house.getBoundingBox().getXSize(), house.getBoundingBox().getZSize());
					addedHouse = true;
				}
			}

			EnumFacing facing = getFacing();

			if (addedHouse && rand.nextInt(3) > 0) {
				// add road at end in negative direction
				if (facing != null) {
					switch (facing) {
					case NORTH:
					default:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX() - 1,
								getBoundingBox().getMinY(), getBoundingBox().getMinZ(), EnumFacing.WEST,
								getDistanceFromStart());
						break;

					case SOUTH:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX() - 1,
								getBoundingBox().getMinY(), getBoundingBox().getMaxZ() - 2, EnumFacing.WEST,
								getDistanceFromStart());
						break;

					case WEST:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX(),
								getBoundingBox().getMinY(), getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
								getDistanceFromStart());
						break;

					case EAST:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMaxX() - 2,
								getBoundingBox().getMinY(), getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
								getDistanceFromStart());
						break;
					}
				}
			}

			if (addedHouse && rand.nextInt(3) > 0) {
				// add road at end in positive direction
				if (facing != null) {
					switch (facing) {
					case NORTH:
					default:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMaxX() + 1,
								getBoundingBox().getMinY(), getBoundingBox().getMinZ(), EnumFacing.EAST,
								getDistanceFromStart());
						break;

					case SOUTH:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMaxX() + 1,
								getBoundingBox().getMinY(), getBoundingBox().getMaxZ() - 2, EnumFacing.EAST,
								getDistanceFromStart());
						break;

					case WEST:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX(),
								getBoundingBox().getMinY(), getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
								getDistanceFromStart());
						break;

					case EAST:
						generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMaxX() - 2,
								getBoundingBox().getMinY(), getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
								getDistanceFromStart());
						break;
					}
				}
			}
		}

		/**
		 * Finds the bounding box for the longest (sensible-length) road which
		 * does not collide with existing village components.
		 */
		public static AABB findLongestNonIntersectingBB(Start start, List<Component> components, Random rand, int x,
				int y, int z, EnumFacing facing) {
			for (int pathLength = 7 * MathHelper.randomRange(rand, 3, 5); pathLength >= 7; pathLength -= 7) {
				AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 3, 3, pathLength, facing);
				if (Component.findIntersecting(components, bounds) == null) {
					return bounds;
				}
			}

			return null;
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			int planks = palette.planks;
			int pathBlock = palette.path;
			int stone = palette.stone;

			for (int x = getBoundingBox().getMinX(); x <= getBoundingBox().getMaxX(); x++) {
				for (int z = getBoundingBox().getMinZ(); z <= getBoundingBox().getMaxZ(); z++) {
					if (bounds.contains(x, 64, z)) {

						int y = getTopSolidOrLiquidBlock(world, x, z) - 1;

						// make sure roads can't generate below sea level
						if (y < 63) {
							y = 63 - 1;
						}

						while (y >= 63 - 1) {
							int block = world.get(x, y, z);

							// replace grass with grass path
							if (block == Blocks.GRASS && Blocks.isAir(world.get(x, y + 1, z))) {
								world.set(x, y, z, Blocks.GRASS_PATH);
								break;
							}

							// replace liquid with planks bridge
							if (Blocks.isLiquid(block)) {
								world.set(x, y, z, planks);
								break;
							}

							// replace sand with the path block and the stone
							// block underneath
							if (block == Blocks.SAND || block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE) {
								world.set(x, y, z, pathBlock);
								world.set(x, y - 1, z, stone);
								break;
							}

							// if none of these blocks were found, keep
							// searching downwards until one is found.
							y--;
						}
					}
				}
			}

			return true;
		}
	}

	public abstract static class Road extends VillageComponent {
		protected Road(Start start, int distanceFromStart) {
			super(start, distanceFromStart);
		}
	}

	public static class Start extends Well {
		public int extraVillageSize;
		public PoolEntry lastComponentTypeCreated;
		public List<PoolEntry> componentTypePool;
		public List<Component> pendingHouses = new ArrayList<>();
		public List<Component> pendingRoads = new ArrayList<>();

		public Start(Random rand, int x, int z, List<PoolEntry> componentTypePool, int extraVillageSize) {
			super((Start) null, 0, rand, x, z);

			this.componentTypePool = componentTypePool;
			this.extraVillageSize = extraVillageSize;

			int biome = BiomeProvider.getBiomes(null, x, z, 1, 1)[0];
			Biomes.EnumType biomeType = Biomes.getType(biome);
			if (biomeType == Biomes.EnumType.DESERT) {
				palette = DESERT_PALETTE;
			} else if (biomeType == Biomes.EnumType.SAVANNA) {
				palette = SAVANNA_PALETTE;
			} else if (biomeType == Biomes.EnumType.TAIGA) {
				palette = TAIGA_PALETTE;
			} else {
				palette = PLAINS_PALETTE;
			}

			isZombieVillage = rand.nextInt(50) == 0;
		}
	}

	public static class LampPost extends VillageComponent {
		public LampPost(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static AABB findNonIntersectingBB(Start start, List<Component> components, Random rand, int x, int y,
				int z, EnumFacing facing) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 3, 4, 2, facing);

			if (Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return bounds;
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 4 - 1, 0));
			}

			int fence = palette.fence;
			// clear space
			fillWithBlocks(world, bounds, 0, 0, 0, 2, 3, 1, Blocks.AIR, Blocks.AIR, false);
			// post
			setBlock(world, fence, 1, 0, 0, bounds);
			setBlock(world, fence, 1, 1, 0, bounds);
			setBlock(world, fence, 1, 2, 0, bounds);
			// wool
			setBlock(world, Blocks.WOOL, 1, 3, 0, bounds);
			// torches
			placeTorch(world, EnumFacing.EAST, 2, 3, 0, bounds);
			placeTorch(world, EnumFacing.NORTH, 1, 3, 1, bounds);
			placeTorch(world, EnumFacing.WEST, 0, 3, 0, bounds);
			placeTorch(world, EnumFacing.SOUTH, 1, 3, -1, bounds);
			return true;
		}
	}

	public abstract static class VillageComponent extends Component {
		protected int averageGroundLvl = -1;
		private int villagersSpawned;
		protected boolean isZombieVillage;
		protected VillagePalette palette;

		protected VillageComponent(Start start, int distanceFromStart) {
			super(distanceFromStart);

			if (start != null) {
				isZombieVillage = start.isZombieVillage;
				palette = start.palette;
			}
		}

		/**
		 * Adds a house in the negative direction on the relevant axis. That is,
		 * to the left if the current component is facing north or east, and to
		 * the right if the current component is facing south or west.
		 */
		protected Component addHouseNegative(Start start, List<Component> components, Random rand, int vOffset,
				int hOffset) {
			EnumFacing facing = getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, EnumFacing.WEST,
							getDistanceFromStart());

				case SOUTH:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, EnumFacing.WEST,
							getDistanceFromStart());

				case WEST:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMinX() + hOffset,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
							getDistanceFromStart());

				case EAST:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMinX() + hOffset,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
							getDistanceFromStart());

				default:
					throw new AssertionError();
				}
			}

			return null;
		}

		/**
		 * Adds a house in the positive direction on the relevant axis. That is,
		 * to the right if the current component is facing north or east, and to
		 * the left if the current component is facing south or west.
		 */
		protected Component addHousePositive(Start start, List<Component> components, Random rand, int hOffset,
				int vOffset) {
			EnumFacing facing = getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY() + hOffset, getBoundingBox().getMinZ() + vOffset, EnumFacing.EAST,
							getDistanceFromStart());

				case SOUTH:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY() + hOffset, getBoundingBox().getMinZ() + vOffset, EnumFacing.EAST,
							getDistanceFromStart());

				case WEST:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMinX() + vOffset,
							getBoundingBox().getMinY() + hOffset, getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
							getDistanceFromStart());

				case EAST:
					return generateAndAddHouse(start, components, rand, getBoundingBox().getMinX() + vOffset,
							getBoundingBox().getMinY() + hOffset, getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
							getDistanceFromStart());

				default:
					throw new AssertionError();
				}
			}

			return null;
		}

		/**
		 * Calculates the mean height of the ground above sea level within the
		 * intersection of this component's bounding box and the given bounding
		 * box.
		 */
		protected int calcAverageGroundLevel(Storage3D world, AABB bounds) {
			int totalHeight = 0;
			int area = 0;

			for (int z = getBoundingBox().getMinZ(); z <= getBoundingBox().getMaxZ(); z++) {
				for (int x = getBoundingBox().getMinX(); x <= getBoundingBox().getMaxX(); x++) {
					if (bounds.contains(x, 64, z)) {
						totalHeight += Math.max(getTopSolidOrLiquidBlock(world, x, z), 63);
						area++;
					}
				}
			}

			if (area == 0) {
				return -1;
			} else {
				return totalHeight / area;
			}
		}

		protected static boolean canVillageGoDeeper(AABB bounds) {
			return bounds != null && bounds.getMinY() > 10;
		}

		protected void spawnVillagers(Storage3D world, AABB bounds, int x, int y, int z, int count) {
			if (villagersSpawned < count) {
				for (int dx = villagersSpawned; dx < count; dx++) {
					int xOff = getXWithOffset(x + dx, z);
					int yOff = getYWithOffset(y);
					int zOff = getZWithOffset(x + dx, z);

					if (!bounds.contains(new BlockPos(xOff, yOff, zOff))) {
						break;
					}

					villagersSpawned++;

					if (isZombieVillage) {
						chooseProfession(dx, VillagerProfession.FARMER);
					} else {
						chooseProfession(dx, VillagerProfession.UNDEFINED);
					}
				}
			}
		}

		protected VillagerProfession chooseProfession(int villagersSpawned, VillagerProfession _default) {
			return _default;
		}

		protected void placeDoor(Storage3D world, AABB bounds, Random rand, int x, int y, int z, EnumFacing facing) {
			if (!isZombieVillage) {
				int door = palette.door;
				setBlock(world, door, x, y, z, bounds);
				setBlock(world, door, x, y + 1, z, bounds);
			}
		}

		protected void placeTorch(Storage3D world, EnumFacing facing, int x, int y, int z, AABB bounds) {
			if (!isZombieVillage) {
				setBlock(world, Blocks.TORCH, x, y, z, bounds);
			}
		}

		protected int getMaxCropsAge(int block) {
			return block == Blocks.BEETROOTS ? 3 : 7;
		}
	}

	public static class Well extends VillageComponent {
		public Well(Start start, int type, Random rand, int x, int z) {
			super(start, type);
			setFacing(EnumFacing.Plane.HORIZONTAL.random(rand));

			if (getFacing().getAxis() == EnumFacing.Axis.Z) {
				setBoundingBox(new AABB(x, 64, z, x + 6 - 1, 78, z + 6 - 1));
			} else {
				setBoundingBox(new AABB(x, 64, z, x + 6 - 1, 78, z + 6 - 1));
			}
		}

		@Override
		public void addMoreComponents(Component start, List<Component> components, Random rand) {
			generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX() - 1,
					getBoundingBox().getMaxY() - 4, getBoundingBox().getMinZ() + 1, EnumFacing.WEST,
					getDistanceFromStart());
			generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMaxX() + 1,
					getBoundingBox().getMaxY() - 4, getBoundingBox().getMinZ() + 1, EnumFacing.EAST,
					getDistanceFromStart());
			generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX() + 1,
					getBoundingBox().getMaxY() - 4, getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
					getDistanceFromStart());
			generateAndAddRoad((Start) start, components, rand, getBoundingBox().getMinX() + 1,
					getBoundingBox().getMaxY() - 4, getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
					getDistanceFromStart());
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 3, 0));
			}

			int stone = palette.stone;
			int fence = palette.fence;
			// water inside
			fillWithBlocks(world, bounds, 1, 0, 1, 4, 12, 4, stone, Blocks.FLOWING_WATER, false);
			// clear space above water
			setBlock(world, Blocks.AIR, 2, 12, 2, bounds);
			setBlock(world, Blocks.AIR, 3, 12, 2, bounds);
			setBlock(world, Blocks.AIR, 2, 12, 3, bounds);
			setBlock(world, Blocks.AIR, 3, 12, 3, bounds);
			// supports
			setBlock(world, fence, 1, 13, 1, bounds);
			setBlock(world, fence, 1, 14, 1, bounds);
			setBlock(world, fence, 4, 13, 1, bounds);
			setBlock(world, fence, 4, 14, 1, bounds);
			setBlock(world, fence, 1, 13, 4, bounds);
			setBlock(world, fence, 1, 14, 4, bounds);
			setBlock(world, fence, 4, 13, 4, bounds);
			setBlock(world, fence, 4, 14, 4, bounds);
			// roof
			fillWithBlocks(world, bounds, 1, 15, 1, 4, 15, 4, stone, stone, false);

			// stone rim
			for (int z = 0; z <= 5; z++) {
				for (int x = 0; x <= 5; x++) {
					if (x == 0 || x == 5 || z == 0 || z == 5) {
						setBlock(world, stone, x, 11, z, bounds);
						clearAbove(world, x, 12, z, bounds);
					}
				}
			}

			return true;
		}
	}

	public static class WoodHut extends VillageComponent {
		private boolean hasFlatRoof;
		private TablePosition tablePosition;

		public WoodHut(Start start, int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(start, distanceFromStart);
			setFacing(facing);
			setBoundingBox(bounds);
			hasFlatRoof = rand.nextBoolean();
			switch (rand.nextInt(3)) {
			case 0:
				tablePosition = TablePosition.NONE;
				break;
			case 1:
				tablePosition = TablePosition.LEFT;
				break;
			case 2:
				tablePosition = TablePosition.RIGHT;
				break;
			default:
				throw new AssertionError();
			}
		}

		public static WoodHut create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, 0, 0, 0, 4, 6, 5, facing);

			if (!canVillageGoDeeper(bounds) || Component.findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new WoodHut(start, distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (averageGroundLvl < 0) {
				averageGroundLvl = calcAverageGroundLevel(world, bounds);

				if (averageGroundLvl < 0) {
					return true;
				}

				setBoundingBox(getBoundingBox().getOffset(0, averageGroundLvl - getBoundingBox().getMaxY() + 6 - 1, 0));
			}

			int stone = palette.stone;
			int planks = palette.planks;
			int stoneStairs = palette.stoneStairs;
			int log = palette.log;
			int fence = palette.fence;
			// clear space
			fillWithBlocks(world, bounds, 1, 1, 1, 3, 5, 4, Blocks.AIR, Blocks.AIR, false);
			// floor
			fillWithBlocks(world, bounds, 0, 0, 0, 3, 0, 4, stone, stone, false);
			fillWithBlocks(world, bounds, 1, 0, 1, 2, 0, 3, Blocks.DIRT, Blocks.DIRT, false);

			// roof
			if (hasFlatRoof) {
				fillWithBlocks(world, bounds, 1, 4, 1, 2, 4, 3, log, log, false);
			} else {
				fillWithBlocks(world, bounds, 1, 5, 1, 2, 5, 3, log, log, false);
			}
			setBlock(world, log, 1, 4, 0, bounds);
			setBlock(world, log, 2, 4, 0, bounds);
			setBlock(world, log, 1, 4, 4, bounds);
			setBlock(world, log, 2, 4, 4, bounds);
			setBlock(world, log, 0, 4, 1, bounds);
			setBlock(world, log, 0, 4, 2, bounds);
			setBlock(world, log, 0, 4, 3, bounds);
			setBlock(world, log, 3, 4, 1, bounds);
			setBlock(world, log, 3, 4, 2, bounds);
			setBlock(world, log, 3, 4, 3, bounds);
			// corner supports
			fillWithBlocks(world, bounds, 0, 1, 0, 0, 3, 0, log, log, false);
			fillWithBlocks(world, bounds, 3, 1, 0, 3, 3, 0, log, log, false);
			fillWithBlocks(world, bounds, 0, 1, 4, 0, 3, 4, log, log, false);
			fillWithBlocks(world, bounds, 3, 1, 4, 3, 3, 4, log, log, false);
			// walls
			fillWithBlocks(world, bounds, 0, 1, 1, 0, 3, 3, planks, planks, false);
			fillWithBlocks(world, bounds, 3, 1, 1, 3, 3, 3, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 1, 0, 2, 3, 0, planks, planks, false);
			fillWithBlocks(world, bounds, 1, 1, 4, 2, 3, 4, planks, planks, false);
			// windows
			setBlock(world, Blocks.GLASS_PANE, 0, 2, 2, bounds);
			setBlock(world, Blocks.GLASS_PANE, 3, 2, 2, bounds);

			if (tablePosition != TablePosition.NONE) {
				int x = tablePosition == TablePosition.LEFT ? 1 : 2;
				setBlock(world, fence, x, 1, 3, bounds);
				setBlock(world, Blocks.WOODEN_PRESSURE_PLATE, x, 2, 3, bounds);
			}

			// clear space for door
			setBlock(world, Blocks.AIR, 1, 1, 0, bounds);
			setBlock(world, Blocks.AIR, 1, 2, 0, bounds);
			// door
			placeDoor(world, bounds, rand, 1, 1, 0, EnumFacing.NORTH);

			// door step
			if (Blocks.isAir(getBlock(world, 1, 0, -1, bounds)) && !Blocks.isAir(getBlock(world, 1, -1, -1, bounds))) {
				setBlock(world, stoneStairs, 1, 0, -1, bounds);

				if (getBlock(world, 1, -1, -1, bounds) == Blocks.GRASS_PATH) {
					setBlock(world, Blocks.GRASS, 1, -1, -1, bounds);
				}
			}

			// clear space
			for (int z = 0; z < 5; z++) {
				for (int x = 0; x < 4; x++) {
					clearAbove(world, x, 6, z, bounds);
					fillBelow(world, stone, x, -1, z, bounds);
				}
			}

			spawnVillagers(world, bounds, 1, 1, 2, 1);
			return true;
		}

		private static enum TablePosition {
			NONE, LEFT, RIGHT
		}
	}

	public static class PoolEntry {
		ComponentCreator creator;
		public final int weight;
		public int amtCreated;
		public int limit;

		public PoolEntry(ComponentCreator creator, int weight, int limit) {
			this.creator = creator;
			this.weight = weight;
			this.limit = limit;
		}

		public boolean canAddAtDistance(int componentType) {
			return limit == 0 || amtCreated < limit;
		}

		public boolean canAddMore() {
			return limit == 0 || amtCreated < limit;
		}
	}

	@FunctionalInterface
	private static interface ComponentCreator {
		VillageComponent create(Start start, List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart);
	}

	public static enum VillagerProfession {
		FARMER, LIBRARIAN, PRIEST, BLACKSMITH, BUTCHER, NITWIT, UNDEFINED
	}

	private static class VillagePalette {
		public final int log;
		public final int stone;
		public final int planks;
		public final int woodStairs;
		public final int stoneStairs;
		public final int fence;
		public final int path;
		public final int door;

		private VillagePalette(int log, int stone, int planks, int woodStairs, int stoneStairs, int fence, int path,
				int door) {
			this.log = log;
			this.stone = stone;
			this.planks = planks;
			this.woodStairs = woodStairs;
			this.stoneStairs = stoneStairs;
			this.fence = fence;
			this.path = path;
			this.door = door;
		}

		public static class Builder {
			private int log = Blocks.LOG;
			private int stone = Blocks.COBBLESTONE;
			private int planks = Blocks.PLANKS;
			private int woodStairs = Blocks.OAK_STAIRS;
			private int stoneStairs = Blocks.STONE_STAIRS;
			private int fence = Blocks.OAK_FENCE;
			private int path = Blocks.GRAVEL;
			private int door = Blocks.OAK_DOOR;

			public Builder setLog(int log) {
				this.log = log;
				return this;
			}

			public Builder setStone(int stone) {
				this.stone = stone;
				return this;
			}

			public Builder setPlanks(int planks) {
				this.planks = planks;
				return this;
			}

			public Builder setWoodStairs(int woodStairs) {
				this.woodStairs = woodStairs;
				return this;
			}

			public Builder setStoneStairs(int stoneStairs) {
				this.stoneStairs = stoneStairs;
				return this;
			}

			public Builder setFence(int fence) {
				this.fence = fence;
				return this;
			}

			public Builder setPath(int path) {
				this.path = path;
				return this;
			}

			public Builder setDoor(int door) {
				this.door = door;
				return this;
			}

			public VillagePalette build() {
				return new VillagePalette(log, stone, planks, woodStairs, stoneStairs, fence, path, door);
			}
		}
	}
}
