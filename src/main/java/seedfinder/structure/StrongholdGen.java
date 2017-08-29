package seedfinder.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import seedfinder.AABB;
import seedfinder.BlockPos;
import seedfinder.Blocks;
import seedfinder.CountEyesTask;
import seedfinder.DoneEnoughException;
import seedfinder.EnumFacing;
import seedfinder.Storage3D;
import seedfinder.Task;
import seedfinder.structure.Component.BlockSelector;

public class StrongholdGen {

	private StrongholdGen() {
	}

	// @formatter:off
	private static final PoolEntry[] POOL_ENTRIES = new PoolEntry[] {
			new PoolEntry(Straight::create, 40, 0),
			new PoolEntry(Prison::create, 5, 5),
			new PoolEntry(LeftTurn::create, 20, 0),
			new PoolEntry(RightTurn::create, 20, 0),
			new PoolEntry(RoomCrossing::create, 10, 6),
			new PoolEntry(StairsStraight::create, 5, 5),
			new PoolEntry(Stairs::create, 5, 5),
			new PoolEntry(Crossing::create, 5, 4),
			new PoolEntry(ChestCorridor::create, 5, 4),
			new PoolEntry(Library::create, 10, 2) {
				@Override
				public boolean canAddAtDistance(int distanceFromStart) {
					return super.canAddAtDistance(distanceFromStart) && distanceFromStart > 4;
				}
			},
			new PoolEntry(PortalRoom::create, 20, 1) {
				@Override
				public boolean canAddAtDistance(int distanceFromStart) {
					return super.canAddAtDistance(distanceFromStart) && distanceFromStart > 5;
				}
			}
		};
	// @formatter:on
	private static List<PoolEntry> componentTypePool;
	private static ComponentCreator nextComponentCreator;
	private static int totalWeight;

	private static final StoneGenerator STONE_GEN = new StoneGenerator();

	public static void prepareStructurePieces() {
		componentTypePool = new ArrayList<>();

		for (PoolEntry pieceWeight : POOL_ENTRIES) {
			pieceWeight.amtCreated = 0;
			componentTypePool.add(pieceWeight);
		}

		nextComponentCreator = null;
	}

	private static void recalcTotalWeight() {
		totalWeight = componentTypePool.stream().mapToInt(it -> it.weight).sum();
	}

	private static boolean canAddMoreComponents() {
		return componentTypePool.stream().anyMatch(it -> it.limit > 0 && it.amtCreated < it.limit);
	}

	/**
	 * Creates the next random component, or <tt>null</tt> if none can be
	 * created.
	 */
	private static StrongholdComponent nextRandomComponent(StartingStairs startComponent, List<Component> components,
			Random rand, int x, int y, int z, EnumFacing facing, int distanceFromStart) {
		recalcTotalWeight();

		if (!canAddMoreComponents()) {
			return null;
		}

		// Check if next component was already decided
		if (nextComponentCreator != null) {
			StrongholdComponent component = nextComponentCreator.create(components, rand, x, y, z, facing,
					distanceFromStart);
			nextComponentCreator = null;

			if (component != null) {
				return component;
			}
		}

		// Choose a new component to add
		for (int tries = 0; tries < 5; tries++) {

			int randNum = rand.nextInt(totalWeight);

			for (PoolEntry entry : componentTypePool) {
				randNum -= entry.weight;

				if (randNum < 0) {
					if (!entry.canAddAtDistance(distanceFromStart)) {
						break;
					}
					// Don't add the same component type twice in a row
					if (entry == startComponent.lastComponentTypeCreated) {
						break;
					}

					// Create the component
					StrongholdComponent component = entry.creator.create(components, rand, x, y, z, facing,
							distanceFromStart);

					if (component != null) {
						entry.amtCreated++;
						startComponent.lastComponentTypeCreated = entry;

						if (!entry.canAddMore()) {
							componentTypePool.remove(entry);
						}

						return component;
					}
				}
			}
		}

		// If there is a component close in front, try to make a corridor to it
		AABB corridorBounds = Corridor.findIntersectingBB(components, rand, x, y, z, facing);

		if (corridorBounds != null && corridorBounds.getMinY() > 1) {
			return new Corridor(distanceFromStart, rand, corridorBounds, facing);
		} else {
			return null;
		}
	}

	/**
	 * Creates a new component and adds it to the list of components
	 */
	private static StrongholdComponent generateAndAddComponent(StartingStairs startComponent,
			List<Component> components, Random rand, int x, int y, int z, EnumFacing facing, int distanceFromStart) {
		if (distanceFromStart > 50) {
			// The stronghold is getting too large
			return null;
		}
		if (Math.abs(x - startComponent.getBoundingBox().getMinX()) > 112
				|| Math.abs(z - startComponent.getBoundingBox().getMinZ()) > 112) {
			// The stronghold is getting to large
			return null;
		}

		StrongholdComponent component = nextRandomComponent(startComponent, components, rand, x, y, z, facing,
				distanceFromStart + 1);

		if (component != null) {
			components.add(component);
			startComponent.pendingChildren.add(component);
		}

		return component;
	}

	/**
	 * The corridor with a chest
	 */
	public static class ChestCorridor extends StrongholdComponent {
		private boolean hasMadeChest;

		public ChestCorridor(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			addComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static ChestCorridor create(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new ChestCorridor(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 4, 6, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 1, 0);
			// exit door
			placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 6);

			// structure for chest
			fillWithBlocks(world, bounds, 3, 1, 2, 3, 1, 4, Blocks.STONEBRICK, Blocks.STONEBRICK, false);
			setBlock(world, Blocks.STONE_SLAB, 3, 1, 1, bounds);
			setBlock(world, Blocks.STONE_SLAB, 3, 1, 5, bounds);
			setBlock(world, Blocks.STONE_SLAB, 3, 2, 2, bounds);
			setBlock(world, Blocks.STONE_SLAB, 3, 2, 4, bounds);
			for (int z = 2; z <= 4; z++) {
				setBlock(world, Blocks.STONE_SLAB, 2, 1, z, bounds);
			}

			// chest
			if (!hasMadeChest
					&& bounds.contains(new BlockPos(getXWithOffset(3, 3), getYWithOffset(2), getZWithOffset(3, 3)))) {
				hasMadeChest = true;
				generateChest(world, bounds, rand, 3, 2, 3);
			}

			return true;
		}

		public BlockPos getChestPos() {
			return new BlockPos(getXWithOffset(3, 3), getYWithOffset(2), getZWithOffset(3, 3));
		}
	}

	/**
	 * A standard corridor
	 */
	private static class Corridor extends StrongholdComponent {
		/**
		 * The length of the corridor
		 */
		private int length;

		public Corridor(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			setBoundingBox(boundingBox);
			length = facing.getAxis() == EnumFacing.Axis.X ? boundingBox.getXSize() : boundingBox.getZSize();
		}

		/**
		 * Finds a bounding box for a corridor which leads to a component close
		 * in front
		 */
		public static AABB findIntersectingBB(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 4, facing);
			Component intersectingComponent = findIntersecting(components, bounds);

			if (intersectingComponent == null) {
				return null;
			} else {
				if (intersectingComponent.getBoundingBox().getMinY() == bounds.getMinY()) {
					for (int maxZ = 3; maxZ >= 1; maxZ--) {
						bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, maxZ - 1, facing);

						if (!intersectingComponent.getBoundingBox().intersectsWith(bounds)) {
							return AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, maxZ, facing);
						}
					}
				}

				return null;
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			for (int z = 0; z < length; z++) {
				// floor
				setBlock(world, Blocks.STONEBRICK, 0, 0, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 1, 0, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 2, 0, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 3, 0, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 4, 0, z, bounds);

				for (int y = 1; y <= 3; y++) {
					// left wall
					setBlock(world, Blocks.STONEBRICK, 0, y, z, bounds);
					// air in middle
					setBlock(world, Blocks.AIR, 1, y, z, bounds);
					setBlock(world, Blocks.AIR, 2, y, z, bounds);
					setBlock(world, Blocks.AIR, 3, y, z, bounds);
					// right wall
					setBlock(world, Blocks.STONEBRICK, 4, y, z, bounds);
				}

				// ceiling
				setBlock(world, Blocks.STONEBRICK, 0, 4, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 1, 4, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 2, 4, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 3, 4, z, bounds);
				setBlock(world, Blocks.STONEBRICK, 4, 4, z, bounds);
			}

			return true;
		}
	}

	/**
	 * The crossing with the bridge made out of stone slabs. 2 corridors can be
	 * created on both the left and the right, one low on each side, and one
	 * high on each side (attaching to the bridge). One corridor is always
	 * created straight ahead going under the bridge.
	 */
	private static class Crossing extends StrongholdComponent {

		// Whether corridors will be placed in each of these locations
		private boolean leftNear;
		private boolean leftFar;
		private boolean rightNear;
		private boolean rightFar;

		public Crossing(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(boundingBox);

			leftNear = rand.nextBoolean();
			leftFar = rand.nextBoolean();
			rightNear = rand.nextBoolean();
			rightFar = rand.nextInt(3) > 0;
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			// The heights of the corridors at each of these locations
			int nearHeight = 3;
			int farHeight = 5;
			EnumFacing facing = getFacing();

			if (facing == EnumFacing.WEST || facing == EnumFacing.NORTH) {
				nearHeight = 5;
				farHeight = 3;
			}

			addComponentAhead((StartingStairs) startComponent, components, rand, 5, 1);

			if (leftNear) {
				addComponentNegative((StartingStairs) startComponent, components, rand, nearHeight, 1);
			}

			if (leftFar) {
				addComponentNegative((StartingStairs) startComponent, components, rand, farHeight, 7);
			}

			if (rightNear) {
				addComponentPositive((StartingStairs) startComponent, components, rand, nearHeight, 1);
			}

			if (rightFar) {
				addComponentPositive((StartingStairs) startComponent, components, rand, farHeight, 7);
			}
		}

		public static Crossing create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -3, 0, 10, 9, 11, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Crossing(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// outer walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 9, 8, 10, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 4, 3, 0);

			if (leftNear) {
				// near left exit
				fillWithBlocks(world, bounds, 0, 3, 1, 0, 5, 3, Blocks.AIR, Blocks.AIR, false);
			}

			if (rightNear) {
				// near right exit
				fillWithBlocks(world, bounds, 9, 3, 1, 9, 5, 3, Blocks.AIR, Blocks.AIR, false);
			}

			if (leftFar) {
				// far left exit
				fillWithBlocks(world, bounds, 0, 5, 7, 0, 7, 9, Blocks.AIR, Blocks.AIR, false);
			}

			if (rightFar) {
				// far right exit
				fillWithBlocks(world, bounds, 9, 5, 7, 9, 7, 9, Blocks.AIR, Blocks.AIR, false);
			}

			// air in underpass
			fillWithBlocks(world, bounds, 5, 1, 10, 7, 3, 10, Blocks.AIR, Blocks.AIR, false);
			// slightly raised floor in front half
			fillWithRandomizedBlocks(world, bounds, 1, 2, 1, 8, 2, 6, false, rand, STONE_GEN);
			// left wall of underpass
			fillWithRandomizedBlocks(world, bounds, 4, 1, 5, 4, 4, 9, false, rand, STONE_GEN);
			// right wall of underpass
			fillWithRandomizedBlocks(world, bounds, 8, 1, 5, 8, 4, 9, false, rand, STONE_GEN);
			// platform next to bridge
			fillWithRandomizedBlocks(world, bounds, 1, 4, 7, 3, 4, 9, false, rand, STONE_GEN);
			// stairs to bridge
			fillWithRandomizedBlocks(world, bounds, 1, 3, 5, 3, 3, 6, false, rand, STONE_GEN);
			fillWithBlocks(world, bounds, 1, 3, 4, 3, 3, 4, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			fillWithBlocks(world, bounds, 1, 4, 6, 3, 4, 6, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			// stairs in underpass
			fillWithRandomizedBlocks(world, bounds, 5, 1, 7, 7, 1, 8, false, rand, STONE_GEN);
			fillWithBlocks(world, bounds, 5, 1, 9, 7, 1, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			fillWithBlocks(world, bounds, 5, 2, 7, 7, 2, 7, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			// bridge
			fillWithBlocks(world, bounds, 4, 5, 7, 4, 5, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			fillWithBlocks(world, bounds, 8, 5, 7, 8, 5, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
			fillWithBlocks(world, bounds, 5, 5, 7, 7, 5, 9, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB, false);
			setBlock(world, Blocks.TORCH, 6, 5, 6, bounds);

			return true;
		}
	}

	private static class LeftTurn extends StrongholdComponent {
		public LeftTurn() {
		}

		public LeftTurn(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startingComponent, List<Component> components, Random rand) {
			EnumFacing facing = getFacing();

			if (facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) {
				addComponentPositive((StartingStairs) startingComponent, components, rand, 1, 1);
			} else {
				addComponentNegative((StartingStairs) startingComponent, components, rand, 1, 1);
			}
		}

		public static LeftTurn create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 5, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new LeftTurn(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 4, 4, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 1, 0);

			// opening on left
			EnumFacing facing = getFacing();
			if (facing != EnumFacing.NORTH && facing != EnumFacing.EAST) {
				fillWithBlocks(world, bounds, 4, 1, 1, 4, 3, 3, Blocks.AIR, Blocks.AIR, false);
			} else {
				fillWithBlocks(world, bounds, 0, 1, 1, 0, 3, 3, Blocks.AIR, Blocks.AIR, false);
			}

			return true;
		}
	}

	public static class Library extends StrongholdComponent {
		private boolean isTallLibrary;

		public Library(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(boundingBox);

			isTallLibrary = boundingBox.getYSize() > 6;
		}

		public static Library create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			// Try to create a tall library
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 14, 11, 15, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				// If can't create tall library, try a single-floor library
				bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 14, 6, 15, facing);

				if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
					return null;
				}
			}

			return new Library(distanceFromStart, rand, bounds, facing);
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			int height = 11;
			if (!isTallLibrary) {
				height = 6;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 13, height - 1, 14, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 4, 1, 0);
			// cobwebs
			randomlyFill(world, bounds, rand, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.WEB, Blocks.WEB, false);

			for (int z = 1; z <= 13; z++) {
				if ((z - 1) % 4 == 0) {
					// pillar separating bookshelves on left and right
					fillWithBlocks(world, bounds, 1, 1, z, 1, 4, z, Blocks.PLANKS, Blocks.PLANKS, false);
					fillWithBlocks(world, bounds, 12, 1, z, 12, 4, z, Blocks.PLANKS, Blocks.PLANKS, false);
					setBlock(world, Blocks.TORCH, 2, 3, z, bounds);
					setBlock(world, Blocks.TORCH, 11, 3, z, bounds);

					if (isTallLibrary) {
						fillWithBlocks(world, bounds, 1, 6, z, 1, 9, z, Blocks.PLANKS, Blocks.PLANKS, false);
						fillWithBlocks(world, bounds, 12, 6, z, 12, 9, z, Blocks.PLANKS, Blocks.PLANKS, false);
					}
				} else {
					// bookshelves on left and right
					fillWithBlocks(world, bounds, 1, 1, z, 1, 4, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
					fillWithBlocks(world, bounds, 12, 1, z, 12, 4, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);

					if (isTallLibrary) {
						fillWithBlocks(world, bounds, 1, 6, z, 1, 9, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
						fillWithBlocks(world, bounds, 12, 6, z, 12, 9, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
					}
				}
			}

			// bookshelves in middle
			for (int z = 3; z < 12; z += 2) {
				fillWithBlocks(world, bounds, 3, 1, z, 4, 3, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
				fillWithBlocks(world, bounds, 6, 1, z, 7, 3, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
				fillWithBlocks(world, bounds, 9, 1, z, 10, 3, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
			}

			if (isTallLibrary) {
				// floor of second layer
				fillWithBlocks(world, bounds, 1, 5, 1, 3, 5, 13, Blocks.PLANKS, Blocks.PLANKS, false);
				fillWithBlocks(world, bounds, 10, 5, 1, 12, 5, 13, Blocks.PLANKS, Blocks.PLANKS, false);
				fillWithBlocks(world, bounds, 4, 5, 1, 9, 5, 2, Blocks.PLANKS, Blocks.PLANKS, false);
				fillWithBlocks(world, bounds, 4, 5, 12, 9, 5, 13, Blocks.PLANKS, Blocks.PLANKS, false);
				setBlock(world, Blocks.PLANKS, 9, 5, 11, bounds);
				setBlock(world, Blocks.PLANKS, 8, 5, 11, bounds);
				setBlock(world, Blocks.PLANKS, 9, 5, 10, bounds);
				// fence around balcony
				fillWithBlocks(world, bounds, 3, 6, 2, 3, 6, 12, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
				fillWithBlocks(world, bounds, 10, 6, 2, 10, 6, 10, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
				fillWithBlocks(world, bounds, 4, 6, 2, 9, 6, 2, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
				fillWithBlocks(world, bounds, 4, 6, 12, 8, 6, 12, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
				setBlock(world, Blocks.OAK_FENCE, 9, 6, 11, bounds);
				setBlock(world, Blocks.OAK_FENCE, 8, 6, 11, bounds);
				setBlock(world, Blocks.OAK_FENCE, 9, 6, 10, bounds);
				// ladder
				setBlock(world, Blocks.LADDER, 10, 1, 13, bounds);
				setBlock(world, Blocks.LADDER, 10, 2, 13, bounds);
				setBlock(world, Blocks.LADDER, 10, 3, 13, bounds);
				setBlock(world, Blocks.LADDER, 10, 4, 13, bounds);
				setBlock(world, Blocks.LADDER, 10, 5, 13, bounds);
				setBlock(world, Blocks.LADDER, 10, 6, 13, bounds);
				setBlock(world, Blocks.LADDER, 10, 7, 13, bounds);
				// chandelier
				setBlock(world, Blocks.OAK_FENCE, 6, 9, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 7, 9, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 6, 8, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 7, 8, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 6, 7, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 7, 7, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 5, 7, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 8, 7, 7, bounds);
				setBlock(world, Blocks.OAK_FENCE, 6, 7, 6, bounds);
				setBlock(world, Blocks.OAK_FENCE, 6, 7, 8, bounds);
				setBlock(world, Blocks.OAK_FENCE, 7, 7, 6, bounds);
				setBlock(world, Blocks.OAK_FENCE, 7, 7, 8, bounds);
				setBlock(world, Blocks.TORCH, 5, 8, 7, bounds);
				setBlock(world, Blocks.TORCH, 8, 8, 7, bounds);
				setBlock(world, Blocks.TORCH, 6, 8, 6, bounds);
				setBlock(world, Blocks.TORCH, 6, 8, 8, bounds);
				setBlock(world, Blocks.TORCH, 7, 8, 6, bounds);
				setBlock(world, Blocks.TORCH, 7, 8, 8, bounds);
			}

			// chest on bottom floor
			generateChest(world, bounds, rand, 3, 3, 5);

			if (isTallLibrary) {
				// chest on top floor
				setBlock(world, Blocks.AIR, 12, 9, 1, bounds);
				generateChest(world, bounds, rand, 12, 8, 1);
			}

			return true;
		}

		public BlockPos getBottomChestPos() {
			return new BlockPos(getXWithOffset(3, 5), getYWithOffset(3), getZWithOffset(3, 5));
		}

		/**
		 * Gets the location of the top-floor chest, if this is a tall library
		 */
		public Optional<BlockPos> getTopChestPos() {
			if (isTallLibrary) {
				return Optional.of(new BlockPos(getXWithOffset(12, 1), getYWithOffset(8), getZWithOffset(12, 1)));
			} else {
				return Optional.empty();
			}
		}
	}

	public static class PortalRoom extends StrongholdComponent {
		private boolean hasSpawner;

		public PortalRoom(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			if (startComponent != null) {
				((StartingStairs) startComponent).portalRoom = this;
			}
		}

		public static PortalRoom create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 11, 8, 16, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new PortalRoom(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 10, 7, 15, false, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, DoorType.GRATES, 4, 1, 0);

			// rim around ceiling
			fillWithRandomizedBlocks(world, bounds, 1, 6, 1, 1, 6, 14, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 9, 6, 1, 9, 6, 14, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 2, 6, 1, 8, 6, 2, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 2, 6, 14, 8, 6, 14, false, rand, STONE_GEN);
			// stone around lava on left and right
			fillWithRandomizedBlocks(world, bounds, 1, 1, 1, 2, 1, 4, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 8, 1, 1, 9, 1, 4, false, rand, STONE_GEN);
			// lava on left and right
			fillWithBlocks(world, bounds, 1, 1, 1, 1, 1, 3, Blocks.FLOWING_LAVA, Blocks.FLOWING_LAVA, false);
			fillWithBlocks(world, bounds, 9, 1, 1, 9, 1, 3, Blocks.FLOWING_LAVA, Blocks.FLOWING_LAVA, false);
			// stone around lava under portal
			fillWithRandomizedBlocks(world, bounds, 3, 1, 8, 7, 1, 12, false, rand, STONE_GEN);
			// lava under portal
			fillWithBlocks(world, bounds, 4, 1, 9, 6, 1, 11, Blocks.FLOWING_LAVA, Blocks.FLOWING_LAVA, false);

			// windows on left and right
			for (int z = 3; z < 14; z += 2) {
				fillWithBlocks(world, bounds, 0, 3, z, 0, 4, z, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
				fillWithBlocks(world, bounds, 10, 3, z, 10, 4, z, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
			}

			// windows in back of room
			for (int x = 2; x < 9; x += 2) {
				fillWithBlocks(world, bounds, x, 3, 15, x, 4, 15, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
			}

			// stairs
			fillWithRandomizedBlocks(world, bounds, 4, 1, 5, 6, 1, 7, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 4, 2, 6, 6, 2, 7, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 4, 3, 7, 6, 3, 7, false, rand, STONE_GEN);
			for (int x = 4; x <= 6; x++) {
				setBlock(world, Blocks.STONE_BRICK_STAIRS, x, 1, 4, bounds);
				setBlock(world, Blocks.STONE_BRICK_STAIRS, x, 2, 5, bounds);
				setBlock(world, Blocks.STONE_BRICK_STAIRS, x, 3, 6, bounds);
			}

			// end portal
			boolean isPortalComplete = true;
			boolean[] eyes = new boolean[12];

			for (int eye = 0 /* haha */; eye < eyes.length; eye++) {
				eyes[eye] = rand.nextFloat() > 0.9F;
				isPortalComplete &= eyes[eye];
			}

			placePortalFrame(world, eyes[0], 4, 3, 8, bounds);
			placePortalFrame(world, eyes[1], 5, 3, 8, bounds);
			placePortalFrame(world, eyes[2], 6, 3, 8, bounds);
			placePortalFrame(world, eyes[3], 4, 3, 12, bounds);
			placePortalFrame(world, eyes[4], 5, 3, 12, bounds);
			placePortalFrame(world, eyes[5], 6, 3, 12, bounds);
			placePortalFrame(world, eyes[6], 3, 3, 9, bounds);
			placePortalFrame(world, eyes[7], 3, 3, 10, bounds);
			placePortalFrame(world, eyes[8], 3, 3, 11, bounds);
			placePortalFrame(world, eyes[9], 7, 3, 9, bounds);
			placePortalFrame(world, eyes[10], 7, 3, 10, bounds);
			placePortalFrame(world, eyes[11], 7, 3, 11, bounds);

			if (Task.isCurrentTaskOfType(Task.Type.COUNT_EYES)) {
				throw new DoneEnoughException();
			}

			if (isPortalComplete) {
				setBlock(world, Blocks.END_PORTAL, 4, 3, 9, bounds);
				setBlock(world, Blocks.END_PORTAL, 5, 3, 9, bounds);
				setBlock(world, Blocks.END_PORTAL, 6, 3, 9, bounds);
				setBlock(world, Blocks.END_PORTAL, 4, 3, 10, bounds);
				setBlock(world, Blocks.END_PORTAL, 5, 3, 10, bounds);
				setBlock(world, Blocks.END_PORTAL, 6, 3, 10, bounds);
				setBlock(world, Blocks.END_PORTAL, 4, 3, 11, bounds);
				setBlock(world, Blocks.END_PORTAL, 5, 3, 11, bounds);
				setBlock(world, Blocks.END_PORTAL, 6, 3, 11, bounds);
			}

			// silverfish spawner
			if (!hasSpawner) {
				hasSpawner = true;
				setBlock(world, Blocks.MOB_SPAWNER, 5, 3, 6, bounds);
			}

			return true;
		}

		/**
		 * Separate method to place portal frame so we can count eyes
		 */
		private void placePortalFrame(Storage3D world, boolean eye, int x, int y, int z, AABB popBB) {
			if (popBB.contains(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z))) {
				setBlock(world, Blocks.END_PORTAL_FRAME, x, y, z, popBB);
				if (eye) {
					Optional<CountEyesTask> task = Task.getCurrentTask(Task.Type.COUNT_EYES);
					task.ifPresent(CountEyesTask::addEye);
				}
			}
		}

		public BlockPos getPortalPos() {
			return new BlockPos(Math.min(getXWithOffset(3, 8), getXWithOffset(7, 12)), getYWithOffset(3),
					Math.min(getZWithOffset(3, 8), getZWithOffset(7, 12)));
		}
	}

	private static class Prison extends StrongholdComponent {

		public Prison(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			addComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static Prison create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 9, 5, 11, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Prison(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 8, 4, 10, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 1, 0);
			// fill cells with air
			fillWithBlocks(world, bounds, 1, 1, 10, 3, 3, 10, Blocks.AIR, Blocks.AIR, false);
			// supports for cells
			fillWithRandomizedBlocks(world, bounds, 4, 1, 1, 4, 3, 1, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 4, 1, 3, 4, 3, 3, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 4, 1, 7, 4, 3, 7, false, rand, STONE_GEN);
			fillWithRandomizedBlocks(world, bounds, 4, 1, 9, 4, 3, 9, false, rand, STONE_GEN);
			// bars separating corridor from cells
			fillWithBlocks(world, bounds, 4, 1, 4, 4, 3, 6, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
			// bars separating cells
			fillWithBlocks(world, bounds, 5, 1, 5, 7, 3, 5, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
			// bars on ceiling
			setBlock(world, Blocks.IRON_BARS, 4, 3, 2, bounds);
			setBlock(world, Blocks.IRON_BARS, 4, 3, 8, bounds);
			// doors to cells
			setBlock(world, Blocks.IRON_DOOR, 4, 1, 2, bounds);
			setBlock(world, Blocks.IRON_DOOR, 4, 2, 2, bounds);
			setBlock(world, Blocks.IRON_DOOR, 4, 1, 8, bounds);
			setBlock(world, Blocks.IRON_DOOR, 4, 2, 8, bounds);

			return true;
		}
	}

	/**
	 * Right turns do not generate due to RightTurn::create referring to
	 * LeftTurn::create
	 */
	private static class RightTurn extends LeftTurn {
		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			EnumFacing facing = getFacing();

			if (facing != EnumFacing.NORTH && facing != EnumFacing.EAST) {
				addComponentNegative((StartingStairs) startComponent, components, rand, 1, 1);
			} else {
				addComponentPositive((StartingStairs) startComponent, components, rand, 1, 1);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 4, 4, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 1, 0);

			// opening on right
			EnumFacing facing = getFacing();
			if (facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) {
				fillWithBlocks(world, bounds, 0, 1, 1, 0, 3, 3, Blocks.AIR, Blocks.AIR, false);
			} else {
				fillWithBlocks(world, bounds, 4, 1, 1, 4, 3, 3, Blocks.AIR, Blocks.AIR, false);
			}

			return true;
		}
	}

	public static class RoomCrossing extends StrongholdComponent {
		private Type roomType;

		public RoomCrossing(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(bounds);
			roomType = Type.random(rand);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			addComponentAhead((StartingStairs) startComponent, components, rand, 4, 1);
			addComponentNegative((StartingStairs) startComponent, components, rand, 1, 4);
			addComponentPositive((StartingStairs) startComponent, components, rand, 1, 4);
		}

		public static RoomCrossing create(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 11, 7, 11, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new RoomCrossing(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 10, 6, 10, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 4, 1, 0);
			// clear openings on exits
			fillWithBlocks(world, bounds, 4, 1, 10, 6, 3, 10, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 0, 1, 4, 0, 3, 6, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 10, 1, 4, 10, 3, 6, Blocks.AIR, Blocks.AIR, false);

			switch (roomType) {
			case STONE_PILLAR:
				// stone pillar
				setBlock(world, Blocks.STONEBRICK, 5, 1, 5, bounds);
				setBlock(world, Blocks.STONEBRICK, 5, 2, 5, bounds);
				setBlock(world, Blocks.STONEBRICK, 5, 3, 5, bounds);
				setBlock(world, Blocks.TORCH, 4, 3, 5, bounds);
				setBlock(world, Blocks.TORCH, 6, 3, 5, bounds);
				setBlock(world, Blocks.TORCH, 5, 3, 4, bounds);
				setBlock(world, Blocks.TORCH, 5, 3, 6, bounds);
				setBlock(world, Blocks.STONE_SLAB, 4, 1, 4, bounds);
				setBlock(world, Blocks.STONE_SLAB, 4, 1, 5, bounds);
				setBlock(world, Blocks.STONE_SLAB, 4, 1, 6, bounds);
				setBlock(world, Blocks.STONE_SLAB, 6, 1, 4, bounds);
				setBlock(world, Blocks.STONE_SLAB, 6, 1, 5, bounds);
				setBlock(world, Blocks.STONE_SLAB, 6, 1, 6, bounds);
				setBlock(world, Blocks.STONE_SLAB, 5, 1, 4, bounds);
				setBlock(world, Blocks.STONE_SLAB, 5, 1, 6, bounds);
				break;

			case FOUNTAIN:
				// fountain
				for (int i = 0; i < 5; i++) {
					setBlock(world, Blocks.STONEBRICK, 3, 1, 3 + i, bounds);
					setBlock(world, Blocks.STONEBRICK, 7, 1, 3 + i, bounds);
					setBlock(world, Blocks.STONEBRICK, 3 + i, 1, 3, bounds);
					setBlock(world, Blocks.STONEBRICK, 3 + i, 1, 7, bounds);
				}

				setBlock(world, Blocks.STONEBRICK, 5, 1, 5, bounds);
				setBlock(world, Blocks.STONEBRICK, 5, 2, 5, bounds);
				setBlock(world, Blocks.STONEBRICK, 5, 3, 5, bounds);
				setBlock(world, Blocks.FLOWING_WATER, 5, 4, 5, bounds);
				break;

			case STORE:
				// cobblestone border
				for (int z = 1; z <= 9; z++) {
					setBlock(world, Blocks.COBBLESTONE, 1, 3, z, bounds);
					setBlock(world, Blocks.COBBLESTONE, 9, 3, z, bounds);
				}
				for (int x = 1; x <= 9; x++) {
					setBlock(world, Blocks.COBBLESTONE, x, 3, 1, bounds);
					setBlock(world, Blocks.COBBLESTONE, x, 3, 9, bounds);
				}

				// central cobblestone column
				setBlock(world, Blocks.COBBLESTONE, 5, 1, 4, bounds);
				setBlock(world, Blocks.COBBLESTONE, 5, 1, 6, bounds);
				setBlock(world, Blocks.COBBLESTONE, 5, 3, 4, bounds);
				setBlock(world, Blocks.COBBLESTONE, 5, 3, 6, bounds);
				setBlock(world, Blocks.COBBLESTONE, 4, 1, 5, bounds);
				setBlock(world, Blocks.COBBLESTONE, 6, 1, 5, bounds);
				setBlock(world, Blocks.COBBLESTONE, 4, 3, 5, bounds);
				setBlock(world, Blocks.COBBLESTONE, 6, 3, 5, bounds);
				for (int y = 1; y <= 3; y++) {
					setBlock(world, Blocks.COBBLESTONE, 4, y, 4, bounds);
					setBlock(world, Blocks.COBBLESTONE, 6, y, 4, bounds);
					setBlock(world, Blocks.COBBLESTONE, 4, y, 6, bounds);
					setBlock(world, Blocks.COBBLESTONE, 6, y, 6, bounds);
				}

				// torch in center
				setBlock(world, Blocks.TORCH, 5, 3, 5, bounds);

				// wooden floor of top floor
				for (int z = 2; z <= 8; z++) {
					setBlock(world, Blocks.PLANKS, 2, 3, z, bounds);
					setBlock(world, Blocks.PLANKS, 3, 3, z, bounds);

					if (z <= 3 || z >= 7) {
						setBlock(world, Blocks.PLANKS, 4, 3, z, bounds);
						setBlock(world, Blocks.PLANKS, 5, 3, z, bounds);
						setBlock(world, Blocks.PLANKS, 6, 3, z, bounds);
					}

					setBlock(world, Blocks.PLANKS, 7, 3, z, bounds);
					setBlock(world, Blocks.PLANKS, 8, 3, z, bounds);
				}

				// ladder
				setBlock(world, Blocks.LADDER, 9, 1, 3, bounds);
				setBlock(world, Blocks.LADDER, 9, 2, 3, bounds);
				setBlock(world, Blocks.LADDER, 9, 3, 3, bounds);

				// chest
				generateChest(world, bounds, rand, 3, 4, 8);

			case EMPTY:
				break;
			}

			return true;
		}

		/**
		 * Returns the position of the chest if this is a store room
		 */
		public Optional<BlockPos> getChestPos() {
			if (roomType == Type.STORE) {
				return Optional.of(new BlockPos(getXWithOffset(3, 8), getYWithOffset(4), getZWithOffset(3, 8)));
			} else {
				return Optional.empty();
			}
		}

		private static enum Type {
			EMPTY, STONE_PILLAR, FOUNTAIN, STORE;

			public static Type random(Random rand) {
				switch (rand.nextInt(5)) {
				case 0:
					return STONE_PILLAR;
				case 1:
					return FOUNTAIN;
				case 2:
					return STORE;
				case 3:
				case 4:
					return EMPTY;
				default:
					throw new AssertionError();
				}
			}
		}
	}

	/**
	 * Spiral stairs
	 */
	private static class Stairs extends StrongholdComponent {
		/**
		 * Whether these spiral stairs are the starting stairs
		 */
		private boolean source;

		public Stairs(int distanceFromStart, Random rand, int x, int z) {
			super(distanceFromStart);

			source = true;
			setFacing(EnumFacing.Plane.HORIZONTAL.random(rand));
			entryDoorType = DoorType.OPENING;

			if (getFacing().getAxis() == EnumFacing.Axis.Z) {
				setBoundingBox(new AABB(x, 64, z, x + 5 - 1, 74, z + 5 - 1));
			} else {
				setBoundingBox(new AABB(x, 64, z, x + 5 - 1, 74, z + 5 - 1));
			}
		}

		public Stairs(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);

			source = false;
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(bounds);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			if (source) {
				// Always start off with a crossing after the start
				nextComponentCreator = Crossing::create;
			}

			addComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static Stairs create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -7, 0, 5, 11, 5, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Stairs(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 10, 4, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 7, 0);
			// exit door
			placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 4);
			// spiral stairs
			setBlock(world, Blocks.STONEBRICK, 2, 6, 1, bounds);
			setBlock(world, Blocks.STONEBRICK, 1, 5, 1, bounds);
			setBlock(world, Blocks.STONE_SLAB, 1, 6, 1, bounds);
			setBlock(world, Blocks.STONEBRICK, 1, 5, 2, bounds);
			setBlock(world, Blocks.STONEBRICK, 1, 4, 3, bounds);
			setBlock(world, Blocks.STONE_SLAB, 1, 5, 3, bounds);
			setBlock(world, Blocks.STONEBRICK, 2, 4, 3, bounds);
			setBlock(world, Blocks.STONEBRICK, 3, 3, 3, bounds);
			setBlock(world, Blocks.STONE_SLAB, 3, 4, 3, bounds);
			setBlock(world, Blocks.STONEBRICK, 3, 3, 2, bounds);
			setBlock(world, Blocks.STONEBRICK, 3, 2, 1, bounds);
			setBlock(world, Blocks.STONE_SLAB, 3, 3, 1, bounds);
			setBlock(world, Blocks.STONEBRICK, 2, 2, 1, bounds);
			setBlock(world, Blocks.STONEBRICK, 1, 1, 1, bounds);
			setBlock(world, Blocks.STONE_SLAB, 1, 2, 1, bounds);
			setBlock(world, Blocks.STONEBRICK, 1, 1, 2, bounds);
			setBlock(world, Blocks.STONE_SLAB, 1, 1, 3, bounds);

			return true;
		}
	}

	public static class StartingStairs extends Stairs {
		public PoolEntry lastComponentTypeCreated;
		public PortalRoom portalRoom;
		public List<StrongholdComponent> pendingChildren = new ArrayList<>();

		public StartingStairs(int distanceFromStart, Random rand, int x, int z) {
			super(0, rand, x, z);
		}
	}

	private static class StairsStraight extends StrongholdComponent {

		public StairsStraight(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(bounds);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			addComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static StairsStraight create(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -7, 0, 5, 11, 8, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new StairsStraight(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 10, 7, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 7, 0);
			// exit door
			placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 7);

			for (int i = 0; i < 6; i++) {
				// stairs
				setBlock(world, Blocks.STONE_STAIRS, 1, 6 - i, 1 + i, bounds);
				setBlock(world, Blocks.STONE_STAIRS, 2, 6 - i, 1 + i, bounds);
				setBlock(world, Blocks.STONE_STAIRS, 3, 6 - i, 1 + i, bounds);

				// ceiling
				if (i < 5) {
					setBlock(world, Blocks.STONEBRICK, 1, 5 - i, 1 + i, bounds);
					setBlock(world, Blocks.STONEBRICK, 2, 5 - i, 1 + i, bounds);
					setBlock(world, Blocks.STONEBRICK, 3, 5 - i, 1 + i, bounds);
				}
			}

			return true;
		}
	}

	/**
	 * A straight corridor which may branch out to either side
	 */
	private static class Straight extends StrongholdComponent {
		private boolean expandsNegative;
		private boolean expandsPositive;

		public Straight(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(bounds);

			expandsNegative = rand.nextInt(2) == 0;
			expandsPositive = rand.nextInt(2) == 0;
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			addComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);

			if (expandsNegative) {
				addComponentNegative((StartingStairs) startComponent, components, rand, 1, 2);
			}

			if (expandsPositive) {
				addComponentPositive((StartingStairs) startComponent, components, rand, 1, 2);
			}
		}

		public static Straight create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Straight(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// walls
			fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 4, 6, true, rand, STONE_GEN);
			// entrance door
			placeDoor(world, rand, bounds, entryDoorType, 1, 1, 0);
			// exit door
			placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 6);
			// torches
			randomlyPlaceBlock(world, bounds, rand, 0.1F, 1, 2, 1, Blocks.TORCH);
			randomlyPlaceBlock(world, bounds, rand, 0.1F, 3, 2, 1, Blocks.TORCH);
			randomlyPlaceBlock(world, bounds, rand, 0.1F, 1, 2, 5, Blocks.TORCH);
			randomlyPlaceBlock(world, bounds, rand, 0.1F, 3, 2, 5, Blocks.TORCH);

			// openings on either side
			if (expandsNegative) {
				fillWithBlocks(world, bounds, 0, 1, 2, 0, 3, 4, Blocks.AIR, Blocks.AIR, false);
			}

			if (expandsPositive) {
				fillWithBlocks(world, bounds, 4, 1, 2, 4, 3, 4, Blocks.AIR, Blocks.AIR, false);
			}

			return true;
		}
	}

	public abstract static class StrongholdComponent extends Component {
		protected DoorType entryDoorType = DoorType.OPENING;

		public StrongholdComponent() {
		}

		protected StrongholdComponent(int distanceFromStart) {
			super(distanceFromStart);
		}

		/**
		 * Places a stronghold door of the given type at the given coordinates
		 */
		protected void placeDoor(Storage3D world, Random rand, AABB bounds, DoorType doorType, int x, int y, int z) {
			switch (doorType) {
			case OPENING:
				// openings are just air
				fillWithBlocks(world, bounds, x, y, z, x + 3 - 1, y + 3 - 1, z, Blocks.AIR, Blocks.AIR, false);
				break;

			case WOOD_DOOR:
				// stonebrick surrounding oak door
				setBlock(world, Blocks.STONEBRICK, x, y, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x, y + 1, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x, y + 2, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 1, y + 2, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 2, y + 2, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 2, y + 1, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 2, y, z, bounds);
				setBlock(world, Blocks.OAK_DOOR, x + 1, y, z, bounds);
				setBlock(world, Blocks.OAK_DOOR, x + 1, y + 1, z, bounds);
				break;

			case GRATES:
				// air opening surrounded by iron bars
				setBlock(world, Blocks.AIR, x + 1, y, z, bounds);
				setBlock(world, Blocks.AIR, x + 1, y + 1, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x, y, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x, y + 1, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x, y + 2, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x + 1, y + 2, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x + 2, y + 2, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x + 2, y + 1, z, bounds);
				setBlock(world, Blocks.IRON_BARS, x + 2, y, z, bounds);
				break;

			case IRON_DOOR:
				// stonebrick surrounding iron door with stone buttons
				setBlock(world, Blocks.STONEBRICK, x, y, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x, y + 1, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x, y + 2, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 1, y + 2, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 2, y + 2, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 2, y + 1, z, bounds);
				setBlock(world, Blocks.STONEBRICK, x + 2, y, z, bounds);
				setBlock(world, Blocks.IRON_DOOR, x + 1, y, z, bounds);
				setBlock(world, Blocks.IRON_DOOR, x + 1, y + 1, z, bounds);
				setBlock(world, Blocks.STONE_BUTTON, x + 2, y + 1, z + 1, bounds);
				setBlock(world, Blocks.STONE_BUTTON, x + 2, y + 1, z - 1, bounds);
			}
		}

		protected DoorType getRandomDoorType(Random rand) {
			switch (rand.nextInt(5)) {
			case 0:
			case 1:
				return DoorType.OPENING;
			case 2:
				return DoorType.WOOD_DOOR;
			case 3:
				return DoorType.GRATES;
			case 4:
				return DoorType.IRON_DOOR;
			default:
				throw new AssertionError();
			}
		}

		/**
		 * Adds a component straight on
		 */
		protected StrongholdComponent addComponentAhead(StartingStairs startComponent, List<Component> components,
				Random rand, int hOffset, int vOffset) {
			EnumFacing facing = getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddComponent(startComponent, components, rand,
							getBoundingBox().getMinX() + hOffset, getBoundingBox().getMinY() + vOffset,
							getBoundingBox().getMinZ() - 1, facing, getDistanceFromStart());

				case SOUTH:
					return generateAndAddComponent(startComponent, components, rand,
							getBoundingBox().getMinX() + hOffset, getBoundingBox().getMinY() + vOffset,
							getBoundingBox().getMaxZ() + 1, facing, getDistanceFromStart());

				case WEST:
					return generateAndAddComponent(startComponent, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, facing,
							getDistanceFromStart());

				case EAST:
					return generateAndAddComponent(startComponent, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, facing,
							getDistanceFromStart());

				default:
					return null;
				}
			}

			return null;
		}

		/**
		 * Adds a component in the negative direction on the relevant axis. That
		 * is, to the left if the current component is facing north or east, and
		 * to the right if the current component is facing south or west.
		 */
		protected StrongholdComponent addComponentNegative(StartingStairs startComponent, List<Component> components,
				Random rand, int vOffset, int hOffset) {
			EnumFacing facing = getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddComponent(startComponent, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, EnumFacing.WEST,
							getDistanceFromStart());

				case SOUTH:
					return generateAndAddComponent(startComponent, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, EnumFacing.WEST,
							getDistanceFromStart());

				case WEST:
					return generateAndAddComponent(startComponent, components, rand,
							getBoundingBox().getMinX() + hOffset, getBoundingBox().getMinY() + vOffset,
							getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, getDistanceFromStart());

				case EAST:
					return generateAndAddComponent(startComponent, components, rand,
							getBoundingBox().getMinX() + hOffset, getBoundingBox().getMinY() + vOffset,
							getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, getDistanceFromStart());

				default:
					return null;
				}
			}

			return null;
		}

		/**
		 * Adds a component in the positive direction on the relevant axis. That
		 * is, to the right if the current component is facing north or east,
		 * and to the left if the current component is facing south or west.
		 */
		protected StrongholdComponent addComponentPositive(StartingStairs startComponent, List<Component> components,
				Random rand, int vOffset, int hOffset) {
			EnumFacing facing = getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddComponent(startComponent, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, EnumFacing.EAST,
							getDistanceFromStart());

				case SOUTH:
					return generateAndAddComponent(startComponent, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY() + vOffset, getBoundingBox().getMinZ() + hOffset, EnumFacing.EAST,
							getDistanceFromStart());

				case WEST:
					return generateAndAddComponent(startComponent, components, rand,
							getBoundingBox().getMinX() + hOffset, getBoundingBox().getMinY() + vOffset,
							getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, getDistanceFromStart());

				case EAST:
					return generateAndAddComponent(startComponent, components, rand,
							getBoundingBox().getMinX() + hOffset, getBoundingBox().getMinY() + vOffset,
							getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, getDistanceFromStart());

				default:
					return null;
				}
			}

			return null;
		}

		protected static boolean canStrongholdGoDeeper(AABB bounds) {
			return bounds != null && bounds.getMinY() > 10;
		}

		public static enum DoorType {
			OPENING, WOOD_DOOR, GRATES, IRON_DOOR;
		}
	}

	@FunctionalInterface
	private static interface ComponentCreator {
		StrongholdComponent create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart);
	}

	/**
	 * Generates stonebrick as an outline
	 */
	private static class StoneGenerator extends BlockSelector {
		private int block;

		private StoneGenerator() {
		}

		@Override
		public void selectBlocks(Random rand, int x, int y, int z, boolean wall) {
			if (wall) {
				rand.nextFloat(); // to choose the stonebrick type
				block = Blocks.STONEBRICK;
			} else {
				block = Blocks.AIR;
			}
		}

		@Override
		public int getBlockState() {
			return block;
		}
	}

	/**
	 * An entry into the weighted random pool
	 */
	private static class PoolEntry {
		public ComponentCreator creator;
		public final int weight;
		public int amtCreated;
		public int limit;

		public PoolEntry(ComponentCreator creator, int weight, int limit) {
			this.creator = creator;
			this.weight = weight;
			this.limit = limit;
		}

		public boolean canAddAtDistance(int distanceFromStart) {
			return canAddMore();
		}

		public boolean canAddMore() {
			return limit == 0 || amtCreated < limit;
		}
	}

}
