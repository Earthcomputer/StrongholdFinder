package seedfinder.structure;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import seedfinder.Blocks;
import seedfinder.loot.LootTable;
import seedfinder.loot.LootTables;
import seedfinder.task.GatherChestsTask;
import seedfinder.task.Task;
import seedfinder.task.Task.Type;
import seedfinder.util.AABB;
import seedfinder.util.BlockPos;
import seedfinder.util.EnumFacing;
import seedfinder.util.Rotation;
import seedfinder.util.Storage3D;

public class TempleGen {

	public static class DesertTemple extends AbstractTemple {
		private final boolean[] hasPlacedChest = new boolean[4];

		public DesertTemple(Random rand, int x, int z) {
			super(rand, x, 64, z, 21, 15, 21);
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			// ground
			fillWithBlocks(world, bounds, 0, -4, 0, xSize - 1, 0, zSize - 1, Blocks.SANDSTONE, Blocks.SANDSTONE, false);

			// pyramid roof
			for (int i = 1; i <= 9; i++) {
				fillWithBlocks(world, bounds, i, i, i, xSize - 1 - i, i, zSize - 1 - i, Blocks.SANDSTONE,
						Blocks.SANDSTONE, false);
				fillWithBlocks(world, bounds, i + 1, i, i + 1, xSize - 2 - i, i, zSize - 2 - i, Blocks.AIR, Blocks.AIR,
						false);
			}

			// fill below
			for (int x = 0; x < xSize; x++) {
				for (int z = 0; z < zSize; z++) {
					final int y = -5;
					fillBelow(world, Blocks.SANDSTONE, x, y, z, bounds);
				}
			}

			// walls of left tower
			fillWithBlocks(world, bounds, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE, Blocks.AIR, false);
			// roof of left tower
			fillWithBlocks(world, bounds, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 2, 10, 0, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 2, 10, 4, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 0, 10, 2, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 4, 10, 2, bounds);
			// walls of right tower
			fillWithBlocks(world, bounds, xSize - 5, 0, 0, xSize - 1, 9, 4, Blocks.SANDSTONE, Blocks.AIR, false);
			// roof of right tower
			fillWithBlocks(world, bounds, xSize - 4, 10, 1, xSize - 2, 10, 3, Blocks.SANDSTONE, Blocks.SANDSTONE,
					false);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 3, 10, 0, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 3, 10, 4, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 5, 10, 2, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 1, 10, 2, bounds);
			// entrance
			fillWithBlocks(world, bounds, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 9, 1, 0, 11, 3, 4, Blocks.AIR, Blocks.AIR, false);
			setBlock(world, Blocks.SANDSTONE, 9, 1, 1, bounds);
			setBlock(world, Blocks.SANDSTONE, 9, 2, 1, bounds);
			setBlock(world, Blocks.SANDSTONE, 9, 3, 1, bounds);
			setBlock(world, Blocks.SANDSTONE, 10, 3, 1, bounds);
			setBlock(world, Blocks.SANDSTONE, 11, 3, 1, bounds);
			setBlock(world, Blocks.SANDSTONE, 11, 2, 1, bounds);
			setBlock(world, Blocks.SANDSTONE, 11, 1, 1, bounds);
			// corridors to towers
			fillWithBlocks(world, bounds, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 4, 1, 2, 8, 2, 2, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 12, 1, 2, 16, 2, 2, Blocks.AIR, Blocks.AIR, false);
			// top floor
			fillWithBlocks(world, bounds, 5, 4, 5, xSize - 6, 4, zSize - 6, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			// hole in top floor
			fillWithBlocks(world, bounds, 9, 4, 9, 11, 4, 11, Blocks.AIR, Blocks.AIR, false);
			// central inside pillars
			fillWithBlocks(world, bounds, 8, 1, 8, 8, 3, 8, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 12, 1, 8, 12, 3, 8, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 8, 1, 12, 8, 3, 12, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 12, 1, 12, 12, 3, 12, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			// extra space near front left and right
			fillWithBlocks(world, bounds, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, xSize - 5, 1, 5, xSize - 2, 4, 11, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			// top-middle side entrances
			fillWithBlocks(world, bounds, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, xSize - 7, 7, 9, xSize - 7, 7, 11, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 5, 5, 9, 5, 7, 11, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, xSize - 6, 5, 9, xSize - 6, 7, 11, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			setBlock(world, Blocks.AIR, 5, 5, 10, bounds);
			setBlock(world, Blocks.AIR, 5, 6, 10, bounds);
			setBlock(world, Blocks.AIR, 6, 6, 10, bounds);
			setBlock(world, Blocks.AIR, xSize - 6, 5, 10, bounds);
			setBlock(world, Blocks.AIR, xSize - 6, 6, 10, bounds);
			setBlock(world, Blocks.AIR, xSize - 7, 6, 10, bounds);
			// stairs in towers
			fillWithBlocks(world, bounds, 2, 4, 4, 2, 6, 4, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, xSize - 3, 4, 4, xSize - 3, 6, 4, Blocks.AIR, Blocks.AIR, false);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 2, 4, 5, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 2, 3, 4, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 3, 4, 5, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 3, 3, 4, bounds);
			fillWithBlocks(world, bounds, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, xSize - 3, 1, 3, xSize - 2, 2, 3, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			setBlock(world, Blocks.SANDSTONE, 1, 1, 2, bounds);
			setBlock(world, Blocks.SANDSTONE, xSize - 2, 1, 2, bounds);
			setBlock(world, Blocks.STONE_SLAB, 1, 2, 2, bounds);
			setBlock(world, Blocks.STONE_SLAB, xSize - 2, 2, 2, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, 2, 1, 2, bounds);
			setBlock(world, Blocks.SANDSTONE_STAIRS, xSize - 3, 1, 2, bounds);
			// more walls
			fillWithBlocks(world, bounds, 4, 3, 5, 4, 3, 18, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, xSize - 5, 3, 5, xSize - 5, 3, 17, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 3, 1, 5, 4, 2, 16, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, xSize - 6, 1, 5, xSize - 5, 2, 16, Blocks.AIR, Blocks.AIR, false);

			// edge pillars
			for (int z = 5; z <= 17; z += 2) {
				setBlock(world, Blocks.SANDSTONE, 4, 1, z, bounds);
				setBlock(world, Blocks.SANDSTONE, 4, 2, z, bounds);
				setBlock(world, Blocks.SANDSTONE, xSize - 5, 1, z, bounds);
				setBlock(world, Blocks.SANDSTONE, xSize - 5, 2, z, bounds);
			}

			// stained clay pattern on floor
			setBlock(world, Blocks.HARDENED_CLAY, 10, 0, 7, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 10, 0, 8, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 9, 0, 9, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 11, 0, 9, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 8, 0, 10, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 12, 0, 10, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 7, 0, 10, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 13, 0, 10, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 9, 0, 11, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 11, 0, 11, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 10, 0, 12, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 10, 0, 13, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 10, 0, 10, bounds);

			// ankh on side of each tower
			for (int x = 0; x <= xSize - 1; x += xSize - 1) {
				setBlock(world, Blocks.SANDSTONE, x, 2, 1, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 2, 2, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 2, 3, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 3, 1, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 3, 2, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 3, 3, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 4, 1, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 4, 2, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 4, 3, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 5, 1, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 5, 2, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 5, 3, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 6, 1, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 6, 2, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 6, 3, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 7, 1, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 7, 2, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 7, 3, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 8, 1, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 8, 2, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 8, 3, bounds);
			}

			// ankh on front of each tower
			for (int x = 2; x <= xSize - 3; x += xSize - 3 - 2) {
				setBlock(world, Blocks.SANDSTONE, x - 1, 2, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 2, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x + 1, 2, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x - 1, 3, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 3, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x + 1, 3, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x - 1, 4, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 4, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x + 1, 4, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x - 1, 5, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 5, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x + 1, 5, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x - 1, 6, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 6, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x + 1, 6, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x - 1, 7, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x, 7, 0, bounds);
				setBlock(world, Blocks.HARDENED_CLAY, x + 1, 7, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x - 1, 8, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x, 8, 0, bounds);
				setBlock(world, Blocks.SANDSTONE, x + 1, 8, 0, bounds);
			}

			// decoration above entrance
			fillWithBlocks(world, bounds, 8, 4, 0, 12, 6, 0, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			setBlock(world, Blocks.AIR, 8, 6, 0, bounds);
			setBlock(world, Blocks.AIR, 12, 6, 0, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 9, 5, 0, bounds);
			setBlock(world, Blocks.SANDSTONE, 10, 5, 0, bounds);
			setBlock(world, Blocks.HARDENED_CLAY, 11, 5, 0, bounds);
			// column to treasure
			fillWithBlocks(world, bounds, 8, -14, 8, 12, -11, 12, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 8, -10, 8, 12, -10, 12, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 8, -9, 8, 12, -9, 12, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE, Blocks.SANDSTONE, false);
			fillWithBlocks(world, bounds, 9, -11, 9, 11, -1, 11, Blocks.AIR, Blocks.AIR, false);
			// trap
			setBlock(world, Blocks.STONE_PRESSURE_PLATE, 10, -11, 10, bounds);
			fillWithBlocks(world, bounds, 9, -13, 9, 11, -13, 11, Blocks.TNT, Blocks.AIR, false);
			// clear space for chests
			setBlock(world, Blocks.AIR, 8, -11, 10, bounds);
			setBlock(world, Blocks.AIR, 8, -10, 10, bounds);
			setBlock(world, Blocks.SANDSTONE, 7, -10, 10, bounds);
			setBlock(world, Blocks.SANDSTONE, 7, -11, 10, bounds);
			setBlock(world, Blocks.AIR, 12, -11, 10, bounds);
			setBlock(world, Blocks.AIR, 12, -10, 10, bounds);
			setBlock(world, Blocks.SANDSTONE, 13, -10, 10, bounds);
			setBlock(world, Blocks.SANDSTONE, 13, -11, 10, bounds);
			setBlock(world, Blocks.AIR, 10, -11, 8, bounds);
			setBlock(world, Blocks.AIR, 10, -10, 8, bounds);
			setBlock(world, Blocks.SANDSTONE, 10, -10, 7, bounds);
			setBlock(world, Blocks.SANDSTONE, 10, -11, 7, bounds);
			setBlock(world, Blocks.AIR, 10, -11, 12, bounds);
			setBlock(world, Blocks.AIR, 10, -10, 12, bounds);
			setBlock(world, Blocks.SANDSTONE, 10, -10, 13, bounds);
			setBlock(world, Blocks.SANDSTONE, 10, -11, 13, bounds);

			// place chests
			int index = 0;
			for (EnumFacing side : EnumFacing.Plane.HORIZONTAL.facings()) {
				if (!hasPlacedChest[index]) {
					int dx = side.getXOffset() * 2;
					int dz = side.getZOffset() * 2;
					hasPlacedChest[index] = generateChest(world, bounds, rand, 10 + dx, -11, 10 + dz,
							LootTables.DESERT_TEMPLE);
				}
				index++;
			}

			return true;
		}
	}

	public abstract static class AbstractTemple extends Component {
		protected int xSize;
		protected int ySize;
		protected int zSize;
		protected int averageGroundLevel = -1;

		protected AbstractTemple(Random rand, int x, int y, int z, int xSize, int ySize, int zSize) {
			super(0);
			this.xSize = xSize;
			this.ySize = ySize;
			this.zSize = zSize;
			setFacing(EnumFacing.Plane.HORIZONTAL.random(rand));

			if (getFacing().getAxis() == EnumFacing.Axis.Z) {
				setBoundingBox(new AABB(x, y, z, x + xSize - 1, y + ySize - 1, z + zSize - 1));
			} else {
				setBoundingBox(new AABB(x, y, z, x + zSize - 1, y + ySize - 1, z + xSize - 1));
			}
		}

		protected boolean offsetToAverageGroundLevel(Storage3D world, AABB bounds, int yOffset) {
			if (averageGroundLevel >= 0) {
				return true;
			} else {
				int totalHeight = 0;
				int n = 0;

				for (int z = getBoundingBox().getMinZ(); z <= getBoundingBox().getMaxZ(); ++z) {
					for (int x = getBoundingBox().getMinX(); x <= getBoundingBox().getMaxX(); ++x) {
						if (bounds.contains(x, 64, z)) {
							totalHeight += Math.max(getTopSolidOrLiquidBlock(world, x, z), 64);
							n++;
						}
					}
				}

				if (n == 0) {
					return false;
				} else {
					averageGroundLevel = totalHeight / n;
					setBoundingBox(getBoundingBox().getOffset(0,
							averageGroundLevel - getBoundingBox().getMinY() + yOffset, 0));
					return true;
				}
			}
		}
	}

	public static class Igloo extends AbstractTemple {

		public Igloo(Random rand, int x, int z) {
			super(rand, x, 64, z, 7, 5, 8);
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (!offsetToAverageGroundLevel(world, bounds, -1)) {
				return false;
			}

			AABB structBounds = getBoundingBox();
			Rotation[] rotations = Rotation.values();

			NBTStructurePlacementSettings settings = new NBTStructurePlacementSettings()
					.setRotation(rotations[rand.nextInt(rotations.length)]).setReplacedBlock(Blocks.STRUCTURE_VOID)
					.setBoundingBox(structBounds);

			BlockPos topPos = new BlockPos(structBounds.getMinX(), structBounds.getMinY(), structBounds.getMinZ());
			NBTStructure iglooTop = NBTStructures.get(NBTStructures.IGLOO_TOP);

			// place top igloo
			iglooTop.placeInWorld(world, topPos.getX(), topPos.getY(), topPos.getZ(), settings);

			// 50% chance of basement being present
			if (rand.nextDouble() < 0.5) {
				NBTStructure iglooMiddle = NBTStructures.get(NBTStructures.IGLOO_MIDDLE);
				NBTStructure iglooBottom = NBTStructures.get(NBTStructures.IGLOO_BOTTOM);
				// no of sections the ladder repeats for
				int middleRepeats = rand.nextInt(8) + 4;

				// place ladder
				for (int i = 0; i < middleRepeats; i++) {
					BlockPos middlePos = iglooTop.findConnectedPos(settings, 3, -1 - i * 3, 5, settings, 1, 2, 1)
							.add(topPos);
					iglooMiddle.placeInWorld(world, middlePos.getX(), middlePos.getY(), middlePos.getZ(), settings);
				}

				// place basement
				BlockPos bottomPos = iglooTop
						.findConnectedPos(settings, 3, -1 - middleRepeats * 3, 5, settings, 3, 5, 7).add(topPos);
				iglooBottom.placeInChunk(world, bottomPos.getX(), bottomPos.getY(), bottomPos.getZ(), settings);

				// set loot table of chest(s) in basement
				Map<BlockPos, String> dataBlocks = iglooBottom.getDataBlocks(bottomPos.getX(), bottomPos.getY(),
						bottomPos.getZ(), settings);
				for (Map.Entry<BlockPos, String> dataBlock : dataBlocks.entrySet()) {
					if ("chest".equals(dataBlock.getValue())) {
						BlockPos dataBlockPos = dataBlock.getKey();
						int dataBlockX = dataBlockPos.getX();
						int dataBlockY = dataBlockPos.getY();
						int dataBlockZ = dataBlockPos.getZ();
						world.set(dataBlockX, dataBlockY, dataBlockZ, Blocks.AIR);

						long lootSeed = rand.nextLong();
						Optional<GatherChestsTask> task = Task.getCurrentTask(Type.GATHER_CHESTS);
						task.ifPresent(
								it -> it.addChest(dataBlockX, dataBlockY - 1, dataBlockZ, LootTables.IGLOO, lootSeed));
					}
				}
			} else {
				// replace the trapdoor with snow since there's no basement
				BlockPos trapdoorPos = NBTStructure.transform(3, 0, 5, settings).add(topPos);
				world.set(trapdoorPos.getX(), trapdoorPos.getY(), trapdoorPos.getZ(), Blocks.SNOW);
			}

			return true;
		}
	}

	public static class JungleTemple extends AbstractTemple {
		private boolean placedMainChest;
		private boolean placedHiddenChest;
		private boolean placedFirstDispenser;
		private boolean placedSecondDispenser;
		private static final CobblestoneSelector cobblestoneSelector = new CobblestoneSelector();

		public JungleTemple(Random rand, int x, int z) {
			super(rand, x, 64, z, 12, 10, 15);
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (!offsetToAverageGroundLevel(world, bounds, 0)) {
				return false;
			}

			// bottom floor
			fillWithRandomizedBlocks(world, bounds, 0, -4, 0, xSize - 1, 0, zSize - 1, false, rand,
					cobblestoneSelector);
			// middle floor walls
			fillWithRandomizedBlocks(world, bounds, 2, 1, 2, 9, 2, 2, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 2, 1, 12, 9, 2, 12, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 2, 1, 3, 2, 2, 11, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 9, 1, 3, 9, 2, 11, false, rand, cobblestoneSelector);
			// top floor walls
			fillWithRandomizedBlocks(world, bounds, 1, 3, 1, 10, 6, 1, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 1, 3, 13, 10, 6, 13, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 1, 3, 2, 1, 6, 12, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 10, 3, 2, 10, 6, 12, false, rand, cobblestoneSelector);
			// top floor
			fillWithRandomizedBlocks(world, bounds, 2, 3, 2, 9, 3, 12, false, rand, cobblestoneSelector);
			// pyramid ceiling (air replaces later)
			fillWithRandomizedBlocks(world, bounds, 2, 6, 2, 9, 6, 12, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 3, 7, 3, 8, 7, 11, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 4, 8, 4, 7, 8, 10, false, rand, cobblestoneSelector);
			// clear middle floor
			fillWithBlocks(world, bounds, 3, 1, 3, 8, 2, 11, Blocks.AIR, Blocks.AIR, false);
			// gap in top floor
			fillWithBlocks(world, bounds, 4, 3, 6, 7, 3, 9, Blocks.AIR, Blocks.AIR, false);
			// clear top floor
			fillWithBlocks(world, bounds, 2, 4, 2, 9, 5, 12, Blocks.AIR, Blocks.AIR, false);
			// clear inside of pyramid ceiling
			fillWithBlocks(world, bounds, 4, 6, 5, 7, 6, 9, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 5, 7, 6, 6, 7, 8, Blocks.AIR, Blocks.AIR, false);
			// clear entrance
			fillWithBlocks(world, bounds, 5, 1, 2, 6, 2, 2, Blocks.AIR, Blocks.AIR, false);
			// clear back window
			fillWithBlocks(world, bounds, 5, 2, 12, 6, 2, 12, Blocks.AIR, Blocks.AIR, false);
			// clear top windows
			fillWithBlocks(world, bounds, 5, 5, 1, 6, 5, 1, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 5, 5, 13, 6, 5, 13, Blocks.AIR, Blocks.AIR, false);
			setBlock(world, Blocks.AIR, 1, 5, 5, bounds);
			setBlock(world, Blocks.AIR, 10, 5, 5, bounds);
			setBlock(world, Blocks.AIR, 1, 5, 9, bounds);
			setBlock(world, Blocks.AIR, 10, 5, 9, bounds);

			// Outside decorations on top floor

			// front and back
			for (int z = 0; z <= 14; z += 14) {
				fillWithRandomizedBlocks(world, bounds, 2, 4, z, 2, 5, z, false, rand, cobblestoneSelector);
				fillWithRandomizedBlocks(world, bounds, 4, 4, z, 4, 5, z, false, rand, cobblestoneSelector);
				fillWithRandomizedBlocks(world, bounds, 7, 4, z, 7, 5, z, false, rand, cobblestoneSelector);
				fillWithRandomizedBlocks(world, bounds, 9, 4, z, 9, 5, z, false, rand, cobblestoneSelector);
			}

			// blocks above front window
			fillWithRandomizedBlocks(world, bounds, 5, 6, 0, 6, 6, 0, false, rand, cobblestoneSelector);

			// left and right
			for (int x = 0; x <= 11; x += 11) {
				for (int z = 2; z <= 12; z += 2) {
					fillWithRandomizedBlocks(world, bounds, x, 4, z, x, 5, z, false, rand, cobblestoneSelector);
				}

				// block above windows
				fillWithRandomizedBlocks(world, bounds, x, 6, 5, x, 6, 5, false, rand, cobblestoneSelector);
				fillWithRandomizedBlocks(world, bounds, x, 6, 9, x, 6, 9, false, rand, cobblestoneSelector);
			}

			// spikes on top
			fillWithRandomizedBlocks(world, bounds, 2, 7, 2, 2, 9, 2, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 9, 7, 2, 9, 9, 2, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 2, 7, 12, 2, 9, 12, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 9, 7, 12, 9, 9, 12, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 4, 9, 4, 4, 9, 4, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 7, 9, 4, 7, 9, 4, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 4, 9, 10, 4, 9, 10, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 7, 9, 10, 7, 9, 10, false, rand, cobblestoneSelector);
			// top of pyramid roof
			fillWithRandomizedBlocks(world, bounds, 5, 9, 7, 6, 9, 7, false, rand, cobblestoneSelector);
			setBlock(world, Blocks.STONE_STAIRS, 5, 9, 6, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 6, 9, 6, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 5, 9, 8, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 6, 9, 8, bounds);

			// stairs at entrance
			setBlock(world, Blocks.STONE_STAIRS, 4, 0, 0, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 5, 0, 0, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 6, 0, 0, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 7, 0, 0, bounds);
			// stairs to top floor
			setBlock(world, Blocks.STONE_STAIRS, 4, 1, 8, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 4, 2, 9, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 4, 3, 10, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 7, 1, 8, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 7, 2, 9, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 7, 3, 10, bounds);
			// walls under stairs
			fillWithRandomizedBlocks(world, bounds, 4, 1, 9, 4, 1, 9, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 7, 1, 9, 7, 1, 9, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 4, 1, 10, 7, 2, 10, false, rand, cobblestoneSelector);
			// decoration on top floor
			fillWithRandomizedBlocks(world, bounds, 5, 4, 5, 6, 4, 5, false, rand, cobblestoneSelector);
			setBlock(world, Blocks.STONE_STAIRS, 4, 4, 5, bounds);
			setBlock(world, Blocks.STONE_STAIRS, 7, 4, 5, bounds);

			// stairs to bottom floor
			for (int i = 0; i < 4; i++) {
				setBlock(world, Blocks.STONE_STAIRS, 5, 0 - i, 6 + i, bounds);
				setBlock(world, Blocks.STONE_STAIRS, 6, 0 - i, 6 + i, bounds);
				fillWithBlocks(world, bounds, 5, 0 - i, 7 + i, 6, 0 - i, 9 + i, Blocks.AIR, Blocks.AIR, false);
			}

			// clear corridors on bottom floor
			fillWithBlocks(world, bounds, 1, -3, 12, 10, -1, 13, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 1, -3, 1, 3, -1, 13, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 1, -3, 1, 9, -1, 5, Blocks.AIR, Blocks.AIR, false);

			// supports on right of bottom floor corridor
			for (int z = 1; z <= 13; z += 2) {
				fillWithRandomizedBlocks(world, bounds, 1, -3, z, 1, -2, z, false, rand, cobblestoneSelector);
			}

			// supports on ceiling of bottom floor corridor
			for (int z = 2; z <= 12; z += 2) {
				fillWithRandomizedBlocks(world, bounds, 1, -1, z, 3, -1, z, false, rand, cobblestoneSelector);
			}

			// pattern on back of bottom floor
			fillWithRandomizedBlocks(world, bounds, 2, -2, 1, 5, -2, 1, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 7, -2, 1, 9, -2, 1, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 6, -3, 1, 6, -3, 1, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 6, -1, 1, 6, -1, 1, false, rand, cobblestoneSelector);

			// first trap
			setBlock(world, Blocks.TRIPWIRE_HOOK, 1, -3, 8, bounds);
			setBlock(world, Blocks.TRIPWIRE_HOOK, 4, -3, 8, bounds);
			setBlock(world, Blocks.TRIPWIRE, 2, -3, 8, bounds);
			setBlock(world, Blocks.TRIPWIRE, 3, -3, 8, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 7, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 6, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 5, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 4, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 3, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 2, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 5, -3, 1, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 4, -3, 1, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 3, -3, 1, bounds);
			if (!placedFirstDispenser) {
				placedFirstDispenser = placeDispenser(world, bounds, rand, 3, -2, 1, EnumFacing.NORTH,
						LootTables.JUNGLE_TEMPLE_DISPENSER);
			}
			setBlock(world, Blocks.VINE, 3, -2, 2, bounds);

			// second trap
			setBlock(world, Blocks.TRIPWIRE_HOOK, 7, -3, 1, bounds);
			setBlock(world, Blocks.TRIPWIRE_HOOK, 7, -3, 5, bounds);
			setBlock(world, Blocks.TRIPWIRE, 7, -3, 2, bounds);
			setBlock(world, Blocks.TRIPWIRE, 7, -3, 3, bounds);
			setBlock(world, Blocks.TRIPWIRE, 7, -3, 4, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 8, -3, 6, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 9, -3, 6, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 9, -3, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 9, -3, 4, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 9, -2, 4, bounds);
			if (!placedSecondDispenser) {
				placedSecondDispenser = placeDispenser(world, bounds, rand, 9, -2, 3, EnumFacing.WEST,
						LootTables.JUNGLE_TEMPLE_DISPENSER);
			}
			setBlock(world, Blocks.VINE, 8, -1, 3, bounds);
			setBlock(world, Blocks.VINE, 8, -2, 3, bounds);

			// chest
			if (!placedMainChest) {
				placedMainChest = generateChest(world, bounds, rand, 8, -3, 3, LootTables.JUNGLE_TEMPLE);
			}

			// decorations on final corridor
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 9, -3, 2, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 8, -3, 1, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 4, -3, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 5, -2, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 5, -1, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 6, -3, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 7, -2, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 7, -1, 5, bounds);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 8, -3, 5, bounds);
			fillWithRandomizedBlocks(world, bounds, 9, -1, 1, 9, -1, 5, false, rand, cobblestoneSelector);

			// clear space for hidden chest
			fillWithBlocks(world, bounds, 8, -3, 8, 10, -1, 10, Blocks.AIR, Blocks.AIR, false);
			// hidden chest contraption
			setBlock(world, Blocks.STONEBRICK, 8, -2, 11, bounds);
			setBlock(world, Blocks.STONEBRICK, 9, -2, 11, bounds);
			setBlock(world, Blocks.STONEBRICK, 10, -2, 11, bounds);
			setBlock(world, Blocks.LEVER, 8, -2, 12, bounds);
			setBlock(world, Blocks.LEVER, 9, -2, 12, bounds);
			setBlock(world, Blocks.LEVER, 10, -2, 12, bounds);
			fillWithRandomizedBlocks(world, bounds, 8, -3, 8, 8, -3, 10, false, rand, cobblestoneSelector);
			fillWithRandomizedBlocks(world, bounds, 10, -3, 8, 10, -3, 10, false, rand, cobblestoneSelector);
			setBlock(world, Blocks.MOSSY_COBBLESTONE, 10, -2, 9, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 8, -2, 9, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 8, -2, 10, bounds);
			setBlock(world, Blocks.REDSTONE_WIRE, 10, -1, 9, bounds);
			setBlock(world, Blocks.STICKY_PISTON, 9, -2, 8, bounds);
			setBlock(world, Blocks.STICKY_PISTON, 10, -2, 8, bounds);
			setBlock(world, Blocks.STICKY_PISTON, 10, -1, 8, bounds);
			setBlock(world, Blocks.UNPOWERED_REPEATER, 10, -2, 10, bounds);
			// hidden chest
			if (!placedHiddenChest) {
				placedHiddenChest = generateChest(world, bounds, rand, 9, -3, 10, LootTables.JUNGLE_TEMPLE);
			}

			return true;
		}

		private boolean placeDispenser(Storage3D world, AABB bounds, Random rand, int x, int y, int z,
				EnumFacing facing, LootTable lootTable) {
			if (generateChest(world, bounds, rand, x, y, z, lootTable)) {
				setBlock(world, Blocks.DISPENSER, x, y, z, bounds);
				return true;
			} else {
				return false;
			}
		}

		private static class CobblestoneSelector extends BlockSelector {
			private int block;

			private CobblestoneSelector() {
			}

			@Override
			public void selectBlocks(Random rand, int x, int y, int z, boolean wall) {
				if (rand.nextFloat() < 0.4F) {
					block = Blocks.COBBLESTONE;
				} else {
					block = Blocks.MOSSY_COBBLESTONE;
				}
			}

			@Override
			public int getBlockState() {
				return block;
			}
		}
	}

	public static class WitchHit extends AbstractTemple {
		private boolean placedWitch;

		public WitchHit(Random rand, int x, int z) {
			super(rand, x, 64, z, 7, 7, 9);
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (!offsetToAverageGroundLevel(world, bounds, 0)) {
				return false;
			}

			// bulk of the floor
			fillWithBlocks(world, bounds, 1, 1, 1, 5, 1, 7, Blocks.PLANKS, Blocks.PLANKS, false);
			// bulk of the roof
			fillWithBlocks(world, bounds, 1, 4, 2, 5, 4, 7, Blocks.PLANKS, Blocks.PLANKS, false);
			// extra balcony
			fillWithBlocks(world, bounds, 2, 1, 0, 4, 1, 0, Blocks.PLANKS, Blocks.PLANKS, false);
			// walls
			fillWithBlocks(world, bounds, 2, 2, 2, 3, 3, 2, Blocks.PLANKS, Blocks.PLANKS, false);
			fillWithBlocks(world, bounds, 1, 2, 3, 1, 3, 6, Blocks.PLANKS, Blocks.PLANKS, false);
			fillWithBlocks(world, bounds, 5, 2, 3, 5, 3, 6, Blocks.PLANKS, Blocks.PLANKS, false);
			fillWithBlocks(world, bounds, 2, 2, 7, 4, 3, 7, Blocks.PLANKS, Blocks.PLANKS, false);
			// supports
			fillWithBlocks(world, bounds, 1, 0, 2, 1, 3, 2, Blocks.LOG, Blocks.LOG, false);
			fillWithBlocks(world, bounds, 5, 0, 2, 5, 3, 2, Blocks.LOG, Blocks.LOG, false);
			fillWithBlocks(world, bounds, 1, 0, 7, 1, 3, 7, Blocks.LOG, Blocks.LOG, false);
			fillWithBlocks(world, bounds, 5, 0, 7, 5, 3, 7, Blocks.LOG, Blocks.LOG, false);
			// windows
			setBlock(world, Blocks.OAK_FENCE, 2, 3, 2, bounds);
			setBlock(world, Blocks.OAK_FENCE, 3, 3, 7, bounds);
			setBlock(world, Blocks.AIR, 1, 3, 4, bounds);
			setBlock(world, Blocks.AIR, 5, 3, 4, bounds);
			setBlock(world, Blocks.AIR, 5, 3, 5, bounds);
			// flower pot
			setBlock(world, Blocks.FLOWER_POT, 1, 3, 5, bounds);
			// crafting table
			setBlock(world, Blocks.CRAFTING_TABLE, 3, 2, 6, bounds);
			// cauldron
			setBlock(world, Blocks.CAULDRON, 4, 2, 6, bounds);
			// fence on balcony
			setBlock(world, Blocks.OAK_FENCE, 1, 2, 1, bounds);
			setBlock(world, Blocks.OAK_FENCE, 5, 2, 1, bounds);
			// roof
			fillWithBlocks(world, bounds, 0, 4, 1, 6, 4, 1, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_STAIRS, false);
			fillWithBlocks(world, bounds, 0, 4, 2, 0, 4, 7, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_STAIRS, false);
			fillWithBlocks(world, bounds, 6, 4, 2, 6, 4, 7, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_STAIRS, false);
			fillWithBlocks(world, bounds, 0, 4, 8, 6, 4, 8, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_STAIRS, false);

			// stilts
			for (int z = 2; z <= 7; z += 5) {
				for (int x = 1; x <= 5; x += 4) {
					fillBelow(world, Blocks.LOG, x, -1, z, bounds);
				}
			}

			if (!placedWitch) {
				// spawn witch
			}

			return true;
		}
	}

}
