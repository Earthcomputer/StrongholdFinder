package seedfinder.stronghold;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import seedfinder.AABB;
import seedfinder.BlockPos;
import seedfinder.Blocks;
import seedfinder.EnumFacing;
import seedfinder.Storage3D;

public class StrongholdGen {

	private StrongholdGen() {
	}

	// @formatter:off
	private static final PoolEntry[] POOL_ENTRIES = new PoolEntry[] {
			new PoolEntry(Straight::createPiece, 40, 0),
			new PoolEntry(Prison::createPiece, 5, 5),
			new PoolEntry(LeftTurn::createPiece, 20, 0),
			new PoolEntry(RightTurn::createPiece, 20, 0),
			new PoolEntry(RoomCrossing::createPiece, 10, 6),
			new PoolEntry(StairsStraight::createPiece, 5, 5),
			new PoolEntry(Stairs::createPiece, 5, 5),
			new PoolEntry(Crossing::createPiece, 5, 4),
			new PoolEntry(ChestCorridor::createPiece, 5, 4),
			new PoolEntry(Library::createPiece, 10, 2) {
				@Override
				public boolean canAddAtDistance(int distanceFromStart) {
					return super.canAddAtDistance(distanceFromStart) && distanceFromStart > 4;
				}
			},
			new PoolEntry(PortalRoom::createPiece, 20, 1) {
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

	private static int eyes = 0;

	public static void prepareStructurePieces() {
		componentTypePool = new ArrayList<>();

		for (PoolEntry pieceWeight : POOL_ENTRIES) {
			pieceWeight.amtCreated = 0;
			componentTypePool.add(pieceWeight);
		}

		nextComponentCreator = null;
	}

	public static int getNumEyes() {
		return eyes;
	}

	public static void resetNumEyes() {
		eyes = 0;
	}

	private static boolean canAddMoreComponents() {
		totalWeight = componentTypePool.stream().mapToInt(entry -> entry.weight).sum();
		return componentTypePool.stream().anyMatch(it -> it.limit > 0 && it.amtCreated < it.limit);
	}

	private static Component getNextComponent(StartingStairs startComponent, List<Component> components, Random rand,
			int x, int y, int z, EnumFacing facing, int distanceFromStart) {
		if (!canAddMoreComponents()) {
			return null;
		}

		if (nextComponentCreator != null) {
			Component component = nextComponentCreator.create(components, rand, x, y, z, facing, distanceFromStart);
			nextComponentCreator = null;

			if (component != null) {
				return component;
			}
		}

		for (int tries = 0; tries++ < 5;) {

			int randNum = rand.nextInt(totalWeight);

			for (PoolEntry entry : componentTypePool) {
				randNum -= entry.weight;

				if (randNum < 0) {
					if (!entry.canAddAtDistance(distanceFromStart)
							|| entry == startComponent.lastComponentTypeCreated) {
						break;
					}

					Component component = entry.creator.create(components, rand, x, y, z, facing, distanceFromStart);

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

		AABB corridorBounds = Corridor.findIntersectingBB(components, rand, x, y, z, facing);

		if (corridorBounds != null && corridorBounds.getMinY() > 1) {
			return new Corridor(distanceFromStart, rand, corridorBounds, facing);
		} else {
			return null;
		}
	}

	private static Component generateAndAddComponent(StartingStairs startComponent, List<Component> components,
			Random rand, int x, int y, int z, EnumFacing facing, int distanceFromStart) {
		if (distanceFromStart > 50) {
			return null;
		} else if (Math.abs(x - startComponent.getBoundingBox().getMinX()) <= 112
				&& Math.abs(z - startComponent.getBoundingBox().getMinZ()) <= 112) {
			Component component = getNextComponent(startComponent, components, rand, x, y, z, facing,
					distanceFromStart + 1);

			if (component != null) {
				components.add(component);
				startComponent.pendingChildren.add(component);
			}

			return component;
		} else {
			return null;
		}
	}

	public static class ChestCorridor extends Component {
		private boolean hasMadeChest;

		public ChestCorridor(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			entryDoorType = getRandomDoorType(rand);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static ChestCorridor createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new ChestCorridor(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random randomIn, AABB structureBoundingBoxIn) {
			if (this.isLiquidInStructureBoundingBox(world, structureBoundingBoxIn)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, structureBoundingBoxIn, 0, 0, 0, 4, 4, 6, true, randomIn,
						STONE_GEN);
				this.placeDoor(world, randomIn, structureBoundingBoxIn, this.entryDoorType, 1, 1, 0);
				this.placeDoor(world, randomIn, structureBoundingBoxIn, DoorType.OPENING, 1, 1, 6);
				this.fillWithBlocks(world, structureBoundingBoxIn, 3, 1, 2, 3, 1, 4, Blocks.STONEBRICK,
						Blocks.STONEBRICK, false);
				this.setBlockState(world, Blocks.STONE_SLAB, 3, 1, 1, structureBoundingBoxIn);
				this.setBlockState(world, Blocks.STONE_SLAB, 3, 1, 5, structureBoundingBoxIn);
				this.setBlockState(world, Blocks.STONE_SLAB, 3, 2, 2, structureBoundingBoxIn);
				this.setBlockState(world, Blocks.STONE_SLAB, 3, 2, 4, structureBoundingBoxIn);

				for (int z = 2; z <= 4; z++) {
					this.setBlockState(world, Blocks.STONE_SLAB, 2, 1, z, structureBoundingBoxIn);
				}

				if (!this.hasMadeChest && structureBoundingBoxIn.contains(
						new BlockPos(this.getXWithOffset(3, 3), this.getYWithOffset(2), this.getZWithOffset(3, 3)))) {
					this.hasMadeChest = true;
					this.generateChest(world, structureBoundingBoxIn, randomIn, 3, 2, 3);
				}

				return true;
			}
		}

		public BlockPos getChestPos() {
			return new BlockPos(getXWithOffset(3, 3), getYWithOffset(2), getZWithOffset(3, 3));
		}
	}

	private static class Corridor extends Component {
		private int length;

		public Corridor(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			setFacing(facing);
			setBoundingBox(boundingBox);
			this.length = facing.getAxis() == EnumFacing.Axis.X ? boundingBox.getXSize() : boundingBox.getZSize();
		}

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
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				for (int z = 0; z < this.length; ++z) {
					this.setBlockState(world, Blocks.STONEBRICK, 0, 0, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 1, 0, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 2, 0, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 3, 0, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 4, 0, z, bounds);

					for (int j = 1; j <= 3; ++j) {
						this.setBlockState(world, Blocks.STONEBRICK, 0, j, z, bounds);
						this.setBlockState(world, Blocks.AIR, 1, j, z, bounds);
						this.setBlockState(world, Blocks.AIR, 2, j, z, bounds);
						this.setBlockState(world, Blocks.AIR, 3, j, z, bounds);
						this.setBlockState(world, Blocks.STONEBRICK, 4, j, z, bounds);
					}

					this.setBlockState(world, Blocks.STONEBRICK, 0, 4, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 1, 4, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 2, 4, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 3, 4, z, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 4, 4, z, bounds);
				}

				return true;
			}
		}
	}

	private static class Crossing extends Component {
		private boolean leftLow;
		private boolean leftHigh;
		private boolean rightLow;
		private boolean rightHigh;

		public Crossing(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(boundingBox);
			this.leftLow = rand.nextBoolean();
			this.leftHigh = rand.nextBoolean();
			this.rightLow = rand.nextBoolean();
			this.rightHigh = rand.nextInt(3) > 0;
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			int lowHeight = 3;
			int highHeight = 5;
			EnumFacing facing = this.getFacing();

			if (facing == EnumFacing.WEST || facing == EnumFacing.NORTH) {
				lowHeight = 8 - lowHeight;
				highHeight = 8 - highHeight;
			}

			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 5, 1);

			if (this.leftLow) {
				this.getNextComponentLeft((StartingStairs) startComponent, components, rand, lowHeight, 1);
			}

			if (this.leftHigh) {
				this.getNextComponentLeft((StartingStairs) startComponent, components, rand, highHeight, 7);
			}

			if (this.rightLow) {
				this.getNextComponentRight((StartingStairs) startComponent, components, rand, lowHeight, 1);
			}

			if (this.rightHigh) {
				this.getNextComponentRight((StartingStairs) startComponent, components, rand, highHeight, 7);
			}
		}

		public static Crossing createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -3, 0, 10, 9, 11, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Crossing(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 9, 8, 10, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 4, 3, 0);

				if (this.leftLow) {
					this.fillWithBlocks(world, bounds, 0, 3, 1, 0, 5, 3, Blocks.AIR, Blocks.AIR, false);
				}

				if (this.rightLow) {
					this.fillWithBlocks(world, bounds, 9, 3, 1, 9, 5, 3, Blocks.AIR, Blocks.AIR, false);
				}

				if (this.leftHigh) {
					this.fillWithBlocks(world, bounds, 0, 5, 7, 0, 7, 9, Blocks.AIR, Blocks.AIR, false);
				}

				if (this.rightHigh) {
					this.fillWithBlocks(world, bounds, 9, 5, 7, 9, 7, 9, Blocks.AIR, Blocks.AIR, false);
				}

				this.fillWithBlocks(world, bounds, 5, 1, 10, 7, 3, 10, Blocks.AIR, Blocks.AIR, false);
				this.fillWithRandomizedBlocks(world, bounds, 1, 2, 1, 8, 2, 6, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 4, 1, 5, 4, 4, 9, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 8, 1, 5, 8, 4, 9, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 1, 4, 7, 3, 4, 9, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 1, 3, 5, 3, 3, 6, false, rand, STONE_GEN);
				this.fillWithBlocks(world, bounds, 1, 3, 4, 3, 3, 4, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
				this.fillWithBlocks(world, bounds, 1, 4, 6, 3, 4, 6, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
				this.fillWithRandomizedBlocks(world, bounds, 5, 1, 7, 7, 1, 8, false, rand, STONE_GEN);
				this.fillWithBlocks(world, bounds, 5, 1, 9, 7, 1, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
				this.fillWithBlocks(world, bounds, 5, 2, 7, 7, 2, 7, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
				this.fillWithBlocks(world, bounds, 4, 5, 7, 4, 5, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
				this.fillWithBlocks(world, bounds, 8, 5, 7, 8, 5, 9, Blocks.STONE_SLAB, Blocks.STONE_SLAB, false);
				this.fillWithBlocks(world, bounds, 5, 5, 7, 7, 5, 9, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB,
						false);
				this.setBlockState(world, Blocks.TORCH, 6, 5, 6, bounds);
				return true;
			}
		}
	}

	private static class LeftTurn extends Component {
		public LeftTurn() {
		}

		public LeftTurn(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startingComponent, List<Component> components, Random rand) {
			EnumFacing facing = this.getFacing();

			if (facing == EnumFacing.SOUTH || facing == EnumFacing.WEST) {
				this.getNextComponentRight((StartingStairs) startingComponent, components, rand, 1, 1);
			} else {
				this.getNextComponentLeft((StartingStairs) startingComponent, components, rand, 1, 1);
			}
		}

		public static LeftTurn createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 5, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new LeftTurn(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D worldIn, Random randomIn, AABB structureBoundingBoxIn) {
			if (this.isLiquidInStructureBoundingBox(worldIn, structureBoundingBoxIn)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(worldIn, structureBoundingBoxIn, 0, 0, 0, 4, 4, 4, true, randomIn,
						STONE_GEN);
				this.placeDoor(worldIn, randomIn, structureBoundingBoxIn, this.entryDoorType, 1, 1, 0);
				EnumFacing enumfacing = this.getFacing();

				if (enumfacing != EnumFacing.NORTH && enumfacing != EnumFacing.EAST) {
					this.fillWithBlocks(worldIn, structureBoundingBoxIn, 4, 1, 1, 4, 3, 3, Blocks.AIR, Blocks.AIR,
							false);
				} else {
					this.fillWithBlocks(worldIn, structureBoundingBoxIn, 0, 1, 1, 0, 3, 3, Blocks.AIR, Blocks.AIR,
							false);
				}

				return true;
			}
		}
	}

	public static class Library extends Component {
		private boolean isLargeRoom;

		public Library(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(boundingBox);
			this.isLargeRoom = boundingBox.getYSize() > 6;
		}

		public static Library createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 14, 11, 15, facing);

			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 14, 6, 15, facing);

				if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
					return null;
				}
			}

			return new Library(distanceFromStart, rand, bounds, facing);
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				int height = 11;

				if (!this.isLargeRoom) {
					height = 6;
				}

				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 13, height - 1, 14, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 4, 1, 0);
				this.generateMaybeBox(bounds, rand, 0.07F, 2, 1, 1, 11, 4, 13, false, 0);

				for (int z = 1; z <= 13; ++z) {
					if ((z - 1) % 4 == 0) {
						this.fillWithBlocks(world, bounds, 1, 1, z, 1, 4, z, Blocks.PLANKS, Blocks.PLANKS, false);
						this.fillWithBlocks(world, bounds, 12, 1, z, 12, 4, z, Blocks.PLANKS, Blocks.PLANKS, false);
						this.setBlockState(world, Blocks.TORCH, 2, 3, z, bounds);
						this.setBlockState(world, Blocks.TORCH, 11, 3, z, bounds);

						if (this.isLargeRoom) {
							this.fillWithBlocks(world, bounds, 1, 6, z, 1, 9, z, Blocks.PLANKS, Blocks.PLANKS, false);
							this.fillWithBlocks(world, bounds, 12, 6, z, 12, 9, z, Blocks.PLANKS, Blocks.PLANKS, false);
						}
					} else {
						this.fillWithBlocks(world, bounds, 1, 1, z, 1, 4, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
						this.fillWithBlocks(world, bounds, 12, 1, z, 12, 4, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF,
								false);

						if (this.isLargeRoom) {
							this.fillWithBlocks(world, bounds, 1, 6, z, 1, 9, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF,
									false);
							this.fillWithBlocks(world, bounds, 12, 6, z, 12, 9, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF,
									false);
						}
					}
				}

				for (int z = 3; z < 12; z += 2) {
					this.fillWithBlocks(world, bounds, 3, 1, z, 4, 3, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
					this.fillWithBlocks(world, bounds, 6, 1, z, 7, 3, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
					this.fillWithBlocks(world, bounds, 9, 1, z, 10, 3, z, Blocks.BOOKSHELF, Blocks.BOOKSHELF, false);
				}

				if (this.isLargeRoom) {
					this.fillWithBlocks(world, bounds, 1, 5, 1, 3, 5, 13, Blocks.PLANKS, Blocks.PLANKS, false);
					this.fillWithBlocks(world, bounds, 10, 5, 1, 12, 5, 13, Blocks.PLANKS, Blocks.PLANKS, false);
					this.fillWithBlocks(world, bounds, 4, 5, 1, 9, 5, 2, Blocks.PLANKS, Blocks.PLANKS, false);
					this.fillWithBlocks(world, bounds, 4, 5, 12, 9, 5, 13, Blocks.PLANKS, Blocks.PLANKS, false);
					this.setBlockState(world, Blocks.PLANKS, 9, 5, 11, bounds);
					this.setBlockState(world, Blocks.PLANKS, 8, 5, 11, bounds);
					this.setBlockState(world, Blocks.PLANKS, 9, 5, 10, bounds);
					this.fillWithBlocks(world, bounds, 3, 6, 2, 3, 6, 12, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
					this.fillWithBlocks(world, bounds, 10, 6, 2, 10, 6, 10, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
					this.fillWithBlocks(world, bounds, 4, 6, 2, 9, 6, 2, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
					this.fillWithBlocks(world, bounds, 4, 6, 12, 8, 6, 12, Blocks.OAK_FENCE, Blocks.OAK_FENCE, false);
					this.setBlockState(world, Blocks.OAK_FENCE, 9, 6, 11, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 8, 6, 11, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 9, 6, 10, bounds);
					int iblockstate1 = Blocks.LADDER;
					this.setBlockState(world, iblockstate1, 10, 1, 13, bounds);
					this.setBlockState(world, iblockstate1, 10, 2, 13, bounds);
					this.setBlockState(world, iblockstate1, 10, 3, 13, bounds);
					this.setBlockState(world, iblockstate1, 10, 4, 13, bounds);
					this.setBlockState(world, iblockstate1, 10, 5, 13, bounds);
					this.setBlockState(world, iblockstate1, 10, 6, 13, bounds);
					this.setBlockState(world, iblockstate1, 10, 7, 13, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 6, 9, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 7, 9, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 6, 8, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 7, 8, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 6, 7, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 7, 7, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 5, 7, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 8, 7, 7, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 6, 7, 6, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 6, 7, 8, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 7, 7, 6, bounds);
					this.setBlockState(world, Blocks.OAK_FENCE, 7, 7, 8, bounds);
					int iblockstate = Blocks.TORCH;
					this.setBlockState(world, iblockstate, 5, 8, 7, bounds);
					this.setBlockState(world, iblockstate, 8, 8, 7, bounds);
					this.setBlockState(world, iblockstate, 6, 8, 6, bounds);
					this.setBlockState(world, iblockstate, 6, 8, 8, bounds);
					this.setBlockState(world, iblockstate, 7, 8, 6, bounds);
					this.setBlockState(world, iblockstate, 7, 8, 8, bounds);
				}

				this.generateChest(world, bounds, rand, 3, 3, 5);

				if (this.isLargeRoom) {
					this.setBlockState(world, Blocks.AIR, 12, 9, 1, bounds);
					this.generateChest(world, bounds, rand, 12, 8, 1);
				}

				return true;
			}
		}

		public BlockPos getBottomChestPos() {
			return new BlockPos(getXWithOffset(3, 5), getYWithOffset(3), getZWithOffset(3, 5));
		}

		public Optional<BlockPos> getTopChestPos() {
			if (isLargeRoom) {
				return Optional.of(new BlockPos(getXWithOffset(12, 1), getYWithOffset(8), getZWithOffset(12, 1)));
			} else {
				return Optional.empty();
			}
		}
	}

	public static class PortalRoom extends Component {
		private boolean hasSpawner;

		public PortalRoom(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			setBoundingBox(boundingBox);
		}

		public BlockPos getPortalPos() {
			return new BlockPos(Math.min(getXWithOffset(3, 8), getXWithOffset(7, 12)), getYWithOffset(3),
					Math.min(getZWithOffset(3, 8), getZWithOffset(7, 12)));
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			if (startComponent != null) {
				((StartingStairs) startComponent).portalRoom = this;
			}
		}

		public static PortalRoom createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -4, -1, 0, 11, 8, 16, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new PortalRoom(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 10, 7, 15, false, rand, STONE_GEN);
			this.placeDoor(world, rand, bounds, DoorType.GRATES, 4, 1, 0);
			int y = 6;
			this.fillWithRandomizedBlocks(world, bounds, 1, y, 1, 1, y, 14, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 9, y, 1, 9, y, 14, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 2, y, 1, 8, y, 2, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 2, y, 14, 8, y, 14, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 1, 1, 1, 2, 1, 4, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 8, 1, 1, 9, 1, 4, false, rand, STONE_GEN);
			this.fillWithBlocks(world, bounds, 1, 1, 1, 1, 1, 3, Blocks.FLOWING_LAVA, Blocks.FLOWING_LAVA, false);
			this.fillWithBlocks(world, bounds, 9, 1, 1, 9, 1, 3, Blocks.FLOWING_LAVA, Blocks.FLOWING_LAVA, false);
			this.fillWithRandomizedBlocks(world, bounds, 3, 1, 8, 7, 1, 12, false, rand, STONE_GEN);
			this.fillWithBlocks(world, bounds, 4, 1, 9, 6, 1, 11, Blocks.FLOWING_LAVA, Blocks.FLOWING_LAVA, false);

			for (int z = 3; z < 14; z += 2) {
				this.fillWithBlocks(world, bounds, 0, 3, z, 0, 4, z, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
				this.fillWithBlocks(world, bounds, 10, 3, z, 10, 4, z, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
			}

			for (int x = 2; x < 9; x += 2) {
				this.fillWithBlocks(world, bounds, x, 3, 15, x, 4, 15, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
			}

			this.fillWithRandomizedBlocks(world, bounds, 4, 1, 5, 6, 1, 7, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 4, 2, 6, 6, 2, 7, false, rand, STONE_GEN);
			this.fillWithRandomizedBlocks(world, bounds, 4, 3, 7, 6, 3, 7, false, rand, STONE_GEN);

			for (int x = 4; x <= 6; x++) {
				this.setBlockState(world, Blocks.STONE_BRICK_STAIRS, x, 1, 4, bounds);
				this.setBlockState(world, Blocks.STONE_BRICK_STAIRS, x, 2, 5, bounds);
				this.setBlockState(world, Blocks.STONE_BRICK_STAIRS, x, 3, 6, bounds);
			}

			boolean completePortal = true;
			boolean[] eyes = new boolean[12];

			for (int eye = 0 /* haha */; eye < eyes.length; eye++) {
				eyes[eye] = rand.nextFloat() > 0.9F;
				completePortal &= eyes[eye];
			}

			this.placePortalFrame(world, eyes[0], 4, 3, 8, bounds);
			this.placePortalFrame(world, eyes[1], 5, 3, 8, bounds);
			this.placePortalFrame(world, eyes[2], 6, 3, 8, bounds);
			this.placePortalFrame(world, eyes[3], 4, 3, 12, bounds);
			this.placePortalFrame(world, eyes[4], 5, 3, 12, bounds);
			this.placePortalFrame(world, eyes[5], 6, 3, 12, bounds);
			this.placePortalFrame(world, eyes[6], 3, 3, 9, bounds);
			this.placePortalFrame(world, eyes[7], 3, 3, 10, bounds);
			this.placePortalFrame(world, eyes[8], 3, 3, 11, bounds);
			this.placePortalFrame(world, eyes[9], 7, 3, 9, bounds);
			this.placePortalFrame(world, eyes[10], 7, 3, 10, bounds);
			this.placePortalFrame(world, eyes[11], 7, 3, 11, bounds);

			if (completePortal) {
				this.setBlockState(world, Blocks.END_PORTAL, 4, 3, 9, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 5, 3, 9, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 6, 3, 9, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 4, 3, 10, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 5, 3, 10, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 6, 3, 10, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 4, 3, 11, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 5, 3, 11, bounds);
				this.setBlockState(world, Blocks.END_PORTAL, 6, 3, 11, bounds);
			}

			if (!this.hasSpawner) {
				this.hasSpawner = true;
				setBlockState(world, Blocks.MOB_SPAWNER, 5, 3, 6, bounds);
			}

			return true;
		}

		private void placePortalFrame(Storage3D world, boolean eye, int x, int y, int z, AABB popBB) {
			if (popBB.contains(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z))) {
				setBlockState(world, Blocks.END_PORTAL_FRAME, x, y, z, popBB);
				if (eye) {
					eyes++;
				}
			}
		}
	}

	private static class Prison extends Component {

		public Prison(int distanceFromStart, Random rand, AABB boundingBox, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(boundingBox);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static Prison createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 9, 5, 11, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Prison(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 8, 4, 10, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 1, 1, 0);
				this.fillWithBlocks(world, bounds, 1, 1, 10, 3, 3, 10, Blocks.AIR, Blocks.AIR, false);
				this.fillWithRandomizedBlocks(world, bounds, 4, 1, 1, 4, 3, 1, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 4, 1, 3, 4, 3, 3, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 4, 1, 7, 4, 3, 7, false, rand, STONE_GEN);
				this.fillWithRandomizedBlocks(world, bounds, 4, 1, 9, 4, 3, 9, false, rand, STONE_GEN);
				this.fillWithBlocks(world, bounds, 4, 1, 4, 4, 3, 6, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
				this.fillWithBlocks(world, bounds, 5, 1, 5, 7, 3, 5, Blocks.IRON_BARS, Blocks.IRON_BARS, false);
				this.setBlockState(world, Blocks.IRON_BARS, 4, 3, 2, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, 4, 3, 8, bounds);
				this.setBlockState(world, Blocks.IRON_DOOR, 4, 1, 2, bounds);
				this.setBlockState(world, Blocks.IRON_DOOR, 4, 2, 2, bounds);
				this.setBlockState(world, Blocks.IRON_DOOR, 4, 1, 8, bounds);
				this.setBlockState(world, Blocks.IRON_DOOR, 4, 2, 8, bounds);
				return true;
			}
		}
	}

	private static class RightTurn extends LeftTurn {
		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			EnumFacing facing = this.getFacing();

			if (facing != EnumFacing.NORTH && facing != EnumFacing.EAST) {
				this.getNextComponentLeft((StartingStairs) startComponent, components, rand, 1, 1);
			} else {
				this.getNextComponentRight((StartingStairs) startComponent, components, rand, 1, 1);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 4, 4, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 1, 1, 0);
				EnumFacing facing = this.getFacing();

				if (facing != EnumFacing.NORTH && facing != EnumFacing.EAST) {
					this.fillWithBlocks(world, bounds, 0, 1, 1, 0, 3, 3, Blocks.AIR, Blocks.AIR, false);
				} else {
					this.fillWithBlocks(world, bounds, 4, 1, 1, 4, 3, 3, Blocks.AIR, Blocks.AIR, false);
				}

				return true;
			}
		}
	}

	public static class RoomCrossing extends Component {
		private int roomType;

		public RoomCrossing(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(bounds);
			this.roomType = rand.nextInt(5);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 4, 1);
			this.getNextComponentLeft((StartingStairs) startComponent, components, rand, 1, 4);
			this.getNextComponentRight((StartingStairs) startComponent, components, rand, 1, 4);
		}

		public static RoomCrossing createPiece(List<Component> components, Random rand, int x, int y, int z,
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
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 10, 6, 10, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 4, 1, 0);
				this.fillWithBlocks(world, bounds, 4, 1, 10, 6, 3, 10, Blocks.AIR, Blocks.AIR, false);
				this.fillWithBlocks(world, bounds, 0, 1, 4, 0, 3, 6, Blocks.AIR, Blocks.AIR, false);
				this.fillWithBlocks(world, bounds, 10, 1, 4, 10, 3, 6, Blocks.AIR, Blocks.AIR, false);

				switch (this.roomType) {
				case 0:
					this.setBlockState(world, Blocks.STONEBRICK, 5, 1, 5, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 5, 2, 5, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 5, 3, 5, bounds);
					this.setBlockState(world, Blocks.TORCH, 4, 3, 5, bounds);
					this.setBlockState(world, Blocks.TORCH, 6, 3, 5, bounds);
					this.setBlockState(world, Blocks.TORCH, 5, 3, 4, bounds);
					this.setBlockState(world, Blocks.TORCH, 5, 3, 6, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 4, 1, 4, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 4, 1, 5, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 4, 1, 6, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 6, 1, 4, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 6, 1, 5, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 6, 1, 6, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 5, 1, 4, bounds);
					this.setBlockState(world, Blocks.STONE_SLAB, 5, 1, 6, bounds);
					break;

				case 1:
					for (int i = 0; i < 5; i++) {
						this.setBlockState(world, Blocks.STONEBRICK, 3, 1, 3 + i, bounds);
						this.setBlockState(world, Blocks.STONEBRICK, 7, 1, 3 + i, bounds);
						this.setBlockState(world, Blocks.STONEBRICK, 3 + i, 1, 3, bounds);
						this.setBlockState(world, Blocks.STONEBRICK, 3 + i, 1, 7, bounds);
					}

					this.setBlockState(world, Blocks.STONEBRICK, 5, 1, 5, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 5, 2, 5, bounds);
					this.setBlockState(world, Blocks.STONEBRICK, 5, 3, 5, bounds);
					this.setBlockState(world, Blocks.FLOWING_WATER, 5, 4, 5, bounds);
					break;

				case 2:
					for (int z = 1; z <= 9; z++) {
						this.setBlockState(world, Blocks.COBBLESTONE, 1, 3, z, bounds);
						this.setBlockState(world, Blocks.COBBLESTONE, 9, 3, z, bounds);
					}

					for (int x = 1; x <= 9; x++) {
						this.setBlockState(world, Blocks.COBBLESTONE, x, 3, 1, bounds);
						this.setBlockState(world, Blocks.COBBLESTONE, x, 3, 9, bounds);
					}

					this.setBlockState(world, Blocks.COBBLESTONE, 5, 1, 4, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 5, 1, 6, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 5, 3, 4, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 5, 3, 6, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 4, 1, 5, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 6, 1, 5, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 4, 3, 5, bounds);
					this.setBlockState(world, Blocks.COBBLESTONE, 6, 3, 5, bounds);

					for (int y = 1; y <= 3; y++) {
						this.setBlockState(world, Blocks.COBBLESTONE, 4, y, 4, bounds);
						this.setBlockState(world, Blocks.COBBLESTONE, 6, y, 4, bounds);
						this.setBlockState(world, Blocks.COBBLESTONE, 4, y, 6, bounds);
						this.setBlockState(world, Blocks.COBBLESTONE, 6, y, 6, bounds);
					}

					this.setBlockState(world, Blocks.TORCH, 5, 3, 5, bounds);

					for (int z = 2; z <= 8; z++) {
						this.setBlockState(world, Blocks.PLANKS, 2, 3, z, bounds);
						this.setBlockState(world, Blocks.PLANKS, 3, 3, z, bounds);

						if (z <= 3 || z >= 7) {
							this.setBlockState(world, Blocks.PLANKS, 4, 3, z, bounds);
							this.setBlockState(world, Blocks.PLANKS, 5, 3, z, bounds);
							this.setBlockState(world, Blocks.PLANKS, 6, 3, z, bounds);
						}

						this.setBlockState(world, Blocks.PLANKS, 7, 3, z, bounds);
						this.setBlockState(world, Blocks.PLANKS, 8, 3, z, bounds);
					}

					this.setBlockState(world, Blocks.LADDER, 9, 1, 3, bounds);
					this.setBlockState(world, Blocks.LADDER, 9, 2, 3, bounds);
					this.setBlockState(world, Blocks.LADDER, 9, 3, 3, bounds);
					this.generateChest(world, bounds, rand, 3, 4, 8);
				}

				return true;
			}
		}

		public Optional<BlockPos> getChestPos() {
			if (roomType == 2) {
				return Optional.of(new BlockPos(getXWithOffset(3, 8), getYWithOffset(4), getZWithOffset(3, 8)));
			} else {
				return Optional.empty();
			}
		}
	}

	private static class Stairs extends Component {
		private boolean source;

		public Stairs(int distanceFromStart, Random rand, int x, int z) {
			super(distanceFromStart);
			this.source = true;
			this.setFacing(EnumFacing.Plane.HORIZONTAL.random(rand));
			this.entryDoorType = DoorType.OPENING;

			if (this.getFacing().getAxis() == EnumFacing.Axis.Z) {
				setBoundingBox(new AABB(x, 64, z, x + 5 - 1, 74, z + 5 - 1));
			} else {
				setBoundingBox(new AABB(x, 64, z, x + 5 - 1, 74, z + 5 - 1));
			}
		}

		public Stairs(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			this.source = false;
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(bounds);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			if (this.source) {
				nextComponentCreator = Crossing::createPiece;
			}

			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static Stairs createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -7, 0, 5, 11, 5, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Stairs(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 10, 4, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 1, 7, 0);
				this.placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 4);
				this.setBlockState(world, Blocks.STONEBRICK, 2, 6, 1, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 1, 5, 1, bounds);
				this.setBlockState(world, Blocks.STONE_SLAB, 1, 6, 1, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 1, 5, 2, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 1, 4, 3, bounds);
				this.setBlockState(world, Blocks.STONE_SLAB, 1, 5, 3, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 2, 4, 3, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 3, 3, 3, bounds);
				this.setBlockState(world, Blocks.STONE_SLAB, 3, 4, 3, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 3, 3, 2, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 3, 2, 1, bounds);
				this.setBlockState(world, Blocks.STONE_SLAB, 3, 3, 1, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 2, 2, 1, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 1, 1, 1, bounds);
				this.setBlockState(world, Blocks.STONE_SLAB, 1, 2, 1, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, 1, 1, 2, bounds);
				this.setBlockState(world, Blocks.STONE_SLAB, 1, 1, 3, bounds);
				return true;
			}
		}
	}

	public static class StartingStairs extends Stairs {
		public PoolEntry lastComponentTypeCreated;
		public PortalRoom portalRoom;
		public List<Component> pendingChildren = new ArrayList<>();

		public StartingStairs(int distanceFromStart, Random rand, int x, int z) {
			super(0, rand, x, z);
		}
	}

	private static class StairsStraight extends Component {

		public StairsStraight(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(bounds);
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);
		}

		public static StairsStraight createPiece(List<Component> components, Random rand, int x, int y, int z,
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
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 10, 7, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 1, 7, 0);
				this.placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 7);

				for (int i = 0; i < 6; ++i) {
					this.setBlockState(world, Blocks.STONE_STAIRS, 1, 6 - i, 1 + i, bounds);
					this.setBlockState(world, Blocks.STONE_STAIRS, 2, 6 - i, 1 + i, bounds);
					this.setBlockState(world, Blocks.STONE_STAIRS, 3, 6 - i, 1 + i, bounds);

					if (i < 5) {
						this.setBlockState(world, Blocks.STONEBRICK, 1, 5 - i, 1 + i, bounds);
						this.setBlockState(world, Blocks.STONEBRICK, 2, 5 - i, 1 + i, bounds);
						this.setBlockState(world, Blocks.STONEBRICK, 3, 5 - i, 1 + i, bounds);
					}
				}

				return true;
			}
		}
	}

	private static class Straight extends Component {
		private boolean expandsLeft;
		private boolean expandsRight;

		public Straight(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing) {
			super(distanceFromStart);
			this.setFacing(facing);
			this.entryDoorType = this.getRandomDoorType(rand);
			setBoundingBox(bounds);
			this.expandsLeft = rand.nextInt(2) == 0;
			this.expandsRight = rand.nextInt(2) == 0;
		}

		@Override
		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
			this.getNextComponentAhead((StartingStairs) startComponent, components, rand, 1, 1);

			if (this.expandsLeft) {
				this.getNextComponentLeft((StartingStairs) startComponent, components, rand, 1, 2);
			}

			if (this.expandsRight) {
				this.getNextComponentRight((StartingStairs) startComponent, components, rand, 1, 2);
			}
		}

		public static Straight createPiece(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing, int distanceFromStart) {
			AABB bounds = AABB.createComponentBoundingBox(x, y, z, -1, -1, 0, 5, 5, 7, facing);
			if (!canStrongholdGoDeeper(bounds) || findIntersecting(components, bounds) != null) {
				return null;
			} else {
				return new Straight(distanceFromStart, rand, bounds, facing);
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (this.isLiquidInStructureBoundingBox(world, bounds)) {
				return false;
			} else {
				this.fillWithRandomizedBlocks(world, bounds, 0, 0, 0, 4, 4, 6, true, rand, STONE_GEN);
				this.placeDoor(world, rand, bounds, this.entryDoorType, 1, 1, 0);
				this.placeDoor(world, rand, bounds, DoorType.OPENING, 1, 1, 6);
				this.randomlyPlaceBlock(world, bounds, rand, 0.1F, 1, 2, 1, Blocks.TORCH);
				this.randomlyPlaceBlock(world, bounds, rand, 0.1F, 3, 2, 1, Blocks.TORCH);
				this.randomlyPlaceBlock(world, bounds, rand, 0.1F, 1, 2, 5, Blocks.TORCH);
				this.randomlyPlaceBlock(world, bounds, rand, 0.1F, 3, 2, 5, Blocks.TORCH);

				if (this.expandsLeft) {
					this.fillWithBlocks(world, bounds, 0, 1, 2, 0, 3, 4, Blocks.AIR, Blocks.AIR, false);
				}

				if (this.expandsRight) {
					this.fillWithBlocks(world, bounds, 4, 1, 2, 4, 3, 4, Blocks.AIR, Blocks.AIR, false);
				}

				return true;
			}
		}
	}

	public abstract static class Component {
		private AABB boundingBox;
		private EnumFacing facing;
		private int distanceFromStart;
		protected DoorType entryDoorType = DoorType.OPENING;

		public Component() {
		}

		protected Component(int distanceFromStart) {
			this.distanceFromStart = distanceFromStart;
		}

		public void setBoundingBox(AABB boundingBox) {
			this.boundingBox = boundingBox;
		}

		public AABB getBoundingBox() {
			return boundingBox;
		}

		public void setFacing(EnumFacing coordBaseMode) {
			this.facing = coordBaseMode;
		}

		public EnumFacing getFacing() {
			return facing;
		}

		public int getDistanceFromStart() {
			return distanceFromStart;
		}

		public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
		}

		public abstract boolean placeInWorld(Storage3D world, Random rand, AABB bounds);

		public static Component findIntersecting(List<Component> components, AABB bounds) {
			return components.stream()
					.filter(it -> it.getBoundingBox() != null && it.getBoundingBox().intersectsWith(bounds)).findFirst()
					.orElse(null);
		}

		public boolean isLiquidInStructureBoundingBox(Storage3D world, AABB bounds) {
			int minX = Math.max(this.boundingBox.getMinX() - 1, bounds.getMinX());
			int minY = Math.max(this.boundingBox.getMinY() - 1, bounds.getMinY());
			int minZ = Math.max(this.boundingBox.getMinZ() - 1, bounds.getMinZ());
			int maxX = Math.min(this.boundingBox.getMaxX() + 1, bounds.getMaxX());
			int maxY = Math.min(this.boundingBox.getMaxY() + 1, bounds.getMaxY());
			int maxZ = Math.min(this.boundingBox.getMaxZ() + 1, bounds.getMaxZ());

			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (Blocks.isLiquid(world.get(x, minY, z))) {
						return true;
					}
					if (Blocks.isLiquid(world.get(x, minY, z))) {
						return true;
					}
				}
			}

			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					if (Blocks.isLiquid(world.get(x, y, minZ))) {
						return true;
					}
					if (Blocks.isLiquid(world.get(x, y, maxZ))) {
						return true;
					}
				}
			}

			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					if (Blocks.isLiquid(world.get(minX, y, z))) {
						return true;
					}
					if (Blocks.isLiquid(world.get(maxX, y, z))) {
						return true;
					}
				}
			}

			return false;
		}

		protected int getXWithOffset(int x, int z) {
			EnumFacing enumfacing = this.getFacing();

			if (enumfacing == null) {
				return x;
			} else {
				switch (enumfacing) {
				case NORTH:
				case SOUTH:
					return this.boundingBox.getMinX() + x;

				case WEST:
					return this.boundingBox.getMaxX() - z;

				case EAST:
					return this.boundingBox.getMinX() + z;

				default:
					return x;
				}
			}
		}

		protected int getYWithOffset(int y) {
			return this.getFacing() == null ? y : y + this.boundingBox.getMinY();
		}

		protected int getZWithOffset(int x, int z) {
			EnumFacing enumfacing = this.getFacing();

			if (enumfacing == null) {
				return z;
			} else {
				switch (enumfacing) {
				case NORTH:
					return this.boundingBox.getMaxZ() - z;

				case SOUTH:
					return this.boundingBox.getMinZ() + z;

				case WEST:
				case EAST:
					return this.boundingBox.getMinZ() + x;

				default:
					return z;
				}
			}
		}

		protected int getBlockStateFromPos(Storage3D world, int x, int y, int z, AABB boundingBox) {
			int xOff = getXWithOffset(x, z);
			int yOff = getYWithOffset(y);
			int zOff = getZWithOffset(x, z);
			if (!boundingBox.contains(xOff, yOff, zOff)) {
				return Blocks.AIR;
			} else {
				return world.get(xOff, yOff, zOff);
			}
		}

		protected void setBlockState(Storage3D world, int block, int x, int y, int z, AABB bounds) {
			int offX = getXWithOffset(x, z);
			int offY = getYWithOffset(y);
			int offZ = getZWithOffset(x, z);
			if (bounds.contains(offX, offY, offZ)) {
				world.set(offX, offY, offZ, block);
			}
		}

		protected void fillWithBlocks(Storage3D world, AABB bounds, int xMin, int yMin, int zMin, int xMax, int yMax,
				int zMax, int boundaryBlock, int insideBlock, boolean existingOnly) {
			for (int y = yMin; y <= yMax; y++) {
				for (int x = xMin; x <= xMax; x++) {
					for (int z = zMin; z <= zMax; z++) {
						if (!existingOnly || !Blocks.isAir(this.getBlockStateFromPos(world, x, y, z, bounds))) {
							if (y != yMin && y != yMax && x != xMin && x != xMax && z != zMin && z != zMax) {
								this.setBlockState(world, insideBlock, x, y, z, bounds);
							} else {
								this.setBlockState(world, boundaryBlock, x, y, z, bounds);
							}
						}
					}
				}
			}
		}

		protected void fillWithRandomizedBlocks(Storage3D world, AABB bounds, int minX, int minY, int minZ, int maxX,
				int maxY, int maxZ, boolean preserveAir, Random rand, BlockSelector blockSelector) {
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					for (int z = minZ; z <= maxZ; z++) {
						if (!preserveAir || !Blocks.isAir(this.getBlockStateFromPos(world, x, y, z, bounds))) {
							blockSelector.selectBlocks(rand, x, y, z,
									y == minY || y == maxY || x == minX || x == maxX || z == minZ || z == maxZ);
							this.setBlockState(world, blockSelector.getBlockState(), x, y, z, bounds);
						}
					}
				}
			}
		}

		protected boolean generateChest(Storage3D world, AABB bounds, Random rand, int x, int y, int z) {
			int xOff = getXWithOffset(x, z);
			int yOff = getYWithOffset(y);
			int zOff = getZWithOffset(x, z);
			if (bounds.contains(xOff, yOff, zOff)) {
				world.set(xOff, yOff, zOff, Blocks.CHEST);
				rand.nextLong(); // for the loot table
				return true;
			} else {
				return false;
			}
		}

		protected void generateMaybeBox(AABB bounds, Random rand, float chance, int xMin, int yMin, int zMin, int xMax,
				int yMax, int zMax, boolean requireNonAir, int requiredSkylight) {
			for (int y = yMin; y <= yMax; ++y) {
				for (int x = xMin; x <= xMax; ++x) {
					for (int z = zMin; z <= zMax; ++z) {
						// TODO place block
						rand.nextFloat();
					}
				}
			}
		}

		protected void randomlyPlaceBlock(Storage3D world, AABB bounds, Random rand, float chance, int x, int y, int z,
				int block) {
			if (rand.nextFloat() < chance) {
				setBlockState(world, block, x, y, z, bounds);
			}
		}

		protected void placeDoor(Storage3D world, Random rand, AABB bounds, DoorType doorType, int x, int y, int z) {
			switch (doorType) {
			case OPENING:
				this.fillWithBlocks(world, bounds, x, y, z, x + 3 - 1, y + 3 - 1, z, Blocks.AIR, Blocks.AIR, false);
				break;

			case WOOD_DOOR:
				this.setBlockState(world, Blocks.STONEBRICK, x, y, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x, y + 1, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x, y + 2, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 1, y + 2, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 2, y + 2, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 2, y + 1, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 2, y, z, bounds);
				this.setBlockState(world, Blocks.OAK_DOOR, x + 1, y, z, bounds);
				this.setBlockState(world, Blocks.OAK_DOOR, x + 1, y + 1, z, bounds);
				break;

			case GRATES:
				this.setBlockState(world, Blocks.AIR, x + 1, y, z, bounds);
				this.setBlockState(world, Blocks.AIR, x + 1, y + 1, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x, y, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x, y + 1, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x, y + 2, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x + 1, y + 2, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x + 2, y + 2, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x + 2, y + 1, z, bounds);
				this.setBlockState(world, Blocks.IRON_BARS, x + 2, y, z, bounds);
				break;

			case IRON_DOOR:
				this.setBlockState(world, Blocks.STONEBRICK, x, y, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x, y + 1, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x, y + 2, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 1, y + 2, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 2, y + 2, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 2, y + 1, z, bounds);
				this.setBlockState(world, Blocks.STONEBRICK, x + 2, y, z, bounds);
				this.setBlockState(world, Blocks.IRON_DOOR, x + 1, y, z, bounds);
				this.setBlockState(world, Blocks.IRON_DOOR, x + 1, y + 1, z, bounds);
				this.setBlockState(world, Blocks.STONE_BUTTON, x + 2, y + 1, z + 1, bounds);
				this.setBlockState(world, Blocks.STONE_BUTTON, x + 2, y + 1, z - 1, bounds);
			}
		}

		protected DoorType getRandomDoorType(Random rand) {
			switch (rand.nextInt(5)) {
			case 0:
			case 1:
			default:
				return DoorType.OPENING;

			case 2:
				return DoorType.WOOD_DOOR;

			case 3:
				return DoorType.GRATES;

			case 4:
				return DoorType.IRON_DOOR;
			}
		}

		protected Component getNextComponentAhead(StartingStairs startComponent, List<Component> components,
				Random rand, int hOffset, int vOffset) {
			EnumFacing facing = this.getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddComponent(startComponent, components, rand,
							this.boundingBox.getMinX() + hOffset, this.boundingBox.getMinY() + vOffset,
							this.boundingBox.getMinZ() - 1, facing, this.getDistanceFromStart());

				case SOUTH:
					return generateAndAddComponent(startComponent, components, rand,
							this.boundingBox.getMinX() + hOffset, this.boundingBox.getMinY() + vOffset,
							this.boundingBox.getMaxZ() + 1, facing, this.getDistanceFromStart());

				case WEST:
					return generateAndAddComponent(startComponent, components, rand, this.boundingBox.getMinX() - 1,
							this.boundingBox.getMinY() + vOffset, this.boundingBox.getMinZ() + hOffset, facing,
							this.getDistanceFromStart());

				case EAST:
					return generateAndAddComponent(startComponent, components, rand, this.boundingBox.getMaxX() + 1,
							this.boundingBox.getMinY() + vOffset, this.boundingBox.getMinZ() + hOffset, facing,
							this.getDistanceFromStart());
				default:
					return null;
				}
			}
			return null;
		}

		protected Component getNextComponentLeft(StartingStairs startComponent, List<Component> components, Random rand,
				int vOffset, int hOffset) {
			EnumFacing facing = this.getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddComponent(startComponent, components, rand, this.boundingBox.getMinX() - 1,
							this.boundingBox.getMinY() + vOffset, this.boundingBox.getMinZ() + hOffset, EnumFacing.WEST,
							this.getDistanceFromStart());

				case SOUTH:
					return generateAndAddComponent(startComponent, components, rand, this.boundingBox.getMinX() - 1,
							this.boundingBox.getMinY() + vOffset, this.boundingBox.getMinZ() + hOffset, EnumFacing.WEST,
							this.getDistanceFromStart());

				case WEST:
					return generateAndAddComponent(startComponent, components, rand,
							this.boundingBox.getMinX() + hOffset, this.boundingBox.getMinY() + vOffset,
							this.boundingBox.getMinZ() - 1, EnumFacing.NORTH, this.getDistanceFromStart());

				case EAST:
					return generateAndAddComponent(startComponent, components, rand,
							this.boundingBox.getMinX() + hOffset, this.boundingBox.getMinY() + vOffset,
							this.boundingBox.getMinZ() - 1, EnumFacing.NORTH, this.getDistanceFromStart());
				default:
					return null;
				}
			}

			return null;
		}

		protected Component getNextComponentRight(StartingStairs startComponent, List<Component> components,
				Random rand, int vOffset, int hOffset) {
			EnumFacing facing = this.getFacing();

			if (facing != null) {
				switch (facing) {
				case NORTH:
					return generateAndAddComponent(startComponent, components, rand, this.boundingBox.getMaxX() + 1,
							this.boundingBox.getMinY() + vOffset, this.boundingBox.getMinZ() + hOffset, EnumFacing.EAST,
							this.getDistanceFromStart());

				case SOUTH:
					return generateAndAddComponent(startComponent, components, rand, this.boundingBox.getMaxX() + 1,
							this.boundingBox.getMinY() + vOffset, this.boundingBox.getMinZ() + hOffset, EnumFacing.EAST,
							this.getDistanceFromStart());

				case WEST:
					return generateAndAddComponent(startComponent, components, rand,
							this.boundingBox.getMinX() + hOffset, this.boundingBox.getMinY() + vOffset,
							this.boundingBox.getMaxZ() + 1, EnumFacing.SOUTH, this.getDistanceFromStart());

				case EAST:
					return generateAndAddComponent(startComponent, components, rand,
							this.boundingBox.getMinX() + hOffset, this.boundingBox.getMinY() + vOffset,
							this.boundingBox.getMaxZ() + 1, EnumFacing.SOUTH, this.getDistanceFromStart());
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

		public abstract static class BlockSelector {
			public abstract void selectBlocks(Random rand, int x, int y, int z, boolean wall);

			public abstract int getBlockState();
		}
	}

	private static interface ComponentCreator {
		Component create(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing,
				int distanceFromStart);
	}

	private static class StoneGenerator extends Component.BlockSelector {
		private int block;

		private StoneGenerator() {
		}

		@Override
		public void selectBlocks(Random rand, int x, int y, int z, boolean wall) {
			if (wall) {
				rand.nextFloat();
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
			return this.limit == 0 || this.amtCreated < this.limit;
		}

		public boolean canAddMore() {
			return this.limit == 0 || this.amtCreated < this.limit;
		}
	}

}
