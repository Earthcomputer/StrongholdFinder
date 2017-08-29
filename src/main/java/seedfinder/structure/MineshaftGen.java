package seedfinder.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.AABB;
import seedfinder.Blocks;
import seedfinder.EnumFacing;
import seedfinder.Storage3D;
import seedfinder.worldgen.SkylightCalculator;

public class MineshaftGen {

	/**
	 * Creates the next random component, or <tt>null</tt> if none can be
	 * created.
	 */
	private static MineshaftComponent nextRandomComponent(List<Component> components, Random rand, int x, int y, int z,
			EnumFacing facing, int distanceFromStart, Type mineshaftType) {

		// 0 <= corridor < 70 <= stairs < 80 <= crossing < 100
		int randNum = rand.nextInt(100);

		if (randNum >= 80) {
			// crossing
			AABB crossingBB = Crossing.findCrossing(components, rand, x, y, z, facing);

			if (crossingBB != null) {
				return new Crossing(distanceFromStart, rand, crossingBB, facing, mineshaftType);
			}
		} else if (randNum >= 70) {
			// stairs
			AABB stairsBB = Stairs.findStairs(components, rand, x, y, z, facing);

			if (stairsBB != null) {
				return new Stairs(distanceFromStart, rand, stairsBB, facing, mineshaftType);
			}
		} else {
			// corridor
			AABB corridorBB = Corridor.findCorridor(components, rand, x, y, z, facing);

			if (corridorBB != null) {
				return new Corridor(distanceFromStart, rand, corridorBB, facing, mineshaftType);
			}
		}

		return null;
	}

	/**
	 * Creates a new mineshaft component and adds it to the list of components
	 */
	private static MineshaftComponent generateAndAddComponent(Component start, List<Component> components, Random rand,
			int x, int y, int z, EnumFacing facing, int distanceFromStart) {
		if (distanceFromStart > 8) {
			// The mineshaft is too large
			return null;
		}
		if (Math.abs(x - start.getBoundingBox().getMinX()) > 80
				|| Math.abs(z - start.getBoundingBox().getMinZ()) > 80) {
			// The mineshaft is too large
			return null;
		}

		Type mineshaftType = ((MineshaftComponent) start).mineshaftType;
		MineshaftComponent component = nextRandomComponent(components, rand, x, y, z, facing, distanceFromStart + 1,
				mineshaftType);

		if (component != null) {
			components.add(component);
			component.addMoreComponents(start, components, rand);
		}

		return component;
	}

	public static class Corridor extends MineshaftComponent {
		private boolean hasRails;
		private boolean hasSpiders;
		private boolean hasPlacedSpawner;
		private int sectionCount;

		public Corridor(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing, Type mineshaftType) {
			super(distanceFromStart, mineshaftType);
			setFacing(facing);
			setBoundingBox(bounds);
			hasRails = rand.nextInt(3) == 0;
			hasSpiders = !hasRails && rand.nextInt(23) == 0;

			if (getFacing().getAxis() == EnumFacing.Axis.Z) {
				sectionCount = bounds.getZSize() / 5;
			} else {
				sectionCount = bounds.getXSize() / 5;
			}
		}

		public static AABB findCorridor(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing) {
			AABB bounds = new AABB(x, y, z, x, y + 2, z);

			int sections;
			for (sections = rand.nextInt(3) + 2; sections > 0; sections--) {
				int length = sections * 5;

				switch (facing) {
				case NORTH:
					bounds = new AABB(bounds.getMinX(), bounds.getMinY(), z - (length - 1), x + 2, bounds.getMaxY(),
							bounds.getMaxZ());
					break;

				case SOUTH:
					bounds = new AABB(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(), x + 2, bounds.getMaxY(),
							z + length - 1);
					break;

				case WEST:
					bounds = new AABB(x - (length - 1), bounds.getMinY(), bounds.getMinZ(), bounds.getMaxX(),
							bounds.getMaxY(), z + 2);
					break;

				case EAST:
					bounds = new AABB(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(), x + length - 1,
							bounds.getMaxY(), z + 2);
					break;

				default:
					throw new AssertionError();
				}

				if (Component.findIntersecting(components, bounds) == null) {
					break;
				}
			}

			return sections > 0 ? bounds : null;
		}

		@Override
		public void addMoreComponents(Component start, List<Component> components, Random rand) {
			int distFromStart = getDistanceFromStart();
			// 0 <= ahead < 2 <= negative < 3 <= positive < 4
			int randNum = rand.nextInt(4);

			EnumFacing facing = getFacing();
			if (facing != null) {
				switch (facing) {
				case NORTH:
					if (randNum <= 1) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX(),
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ() - 1,
								facing, distFromStart);
					} else if (randNum == 2) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ(),
								EnumFacing.WEST, distFromStart);
					} else {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ(),
								EnumFacing.EAST, distFromStart);
					}

					break;

				case SOUTH:
					if (randNum <= 1) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX(),
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMaxZ() + 1,
								facing, distFromStart);
					} else if (randNum == 2) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMaxZ() - 3,
								EnumFacing.WEST, distFromStart);
					} else {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMaxZ() - 3,
								EnumFacing.EAST, distFromStart);
					}

					break;

				case WEST:
					if (randNum <= 1) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ(), facing,
								distFromStart);
					} else if (randNum == 2) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX(),
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ() - 1,
								EnumFacing.NORTH, distFromStart);
					} else {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMinX(),
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMaxZ() + 1,
								EnumFacing.SOUTH, distFromStart);
					}

					break;

				case EAST:
					if (randNum <= 1) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ(), facing,
								distFromStart);
					} else if (randNum == 2) {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() - 3,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMinZ() - 1,
								EnumFacing.NORTH, distFromStart);
					} else {
						generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() - 3,
								getBoundingBox().getMinY() - 1 + rand.nextInt(3), getBoundingBox().getMaxZ() + 1,
								EnumFacing.SOUTH, distFromStart);
					}

					break;

				default:
					throw new AssertionError();
				}
			}

			if (distFromStart < 8) {
				if (facing.getAxis() == EnumFacing.Axis.X) {
					for (int x = getBoundingBox().getMinX() + 3; x + 3 <= getBoundingBox().getMaxX(); x += 5) {
						// 0 <= north < 1 <= south < 2 <= none < 5
						int randNum2 = rand.nextInt(5);

						if (randNum2 == 0) {
							generateAndAddComponent(start, components, rand, x, getBoundingBox().getMinY(),
									getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, distFromStart + 1);
						} else if (randNum2 == 1) {
							generateAndAddComponent(start, components, rand, x, getBoundingBox().getMinY(),
									getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, distFromStart + 1);
						}
					}
				} else {
					for (int z = getBoundingBox().getMinZ() + 3; z + 3 <= getBoundingBox().getMaxZ(); z += 5) {
						// 0 <= west < 1 <= east < 2 <= none < 5
						int randNum2 = rand.nextInt(5);

						if (randNum2 == 0) {
							generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
									getBoundingBox().getMinY(), z, EnumFacing.WEST, distFromStart + 1);
						} else if (randNum2 == 1) {
							generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
									getBoundingBox().getMinY(), z, EnumFacing.EAST, distFromStart + 1);
						}
					}
				}
			}
		}

		@Override
		protected boolean generateChest(Storage3D world, AABB bounds, Random rand, int x, int y, int z) {
			int xOff = getXWithOffset(x, z);
			int yOff = getYWithOffset(y);
			int zOff = getZWithOffset(x, z);

			if (bounds.contains(x, y, z) && Blocks.isAir(world.get(xOff, yOff, zOff))
					&& !Blocks.isAir(world.get(xOff, yOff - 1, zOff))) {
				world.set(xOff, yOff, zOff, Blocks.RAIL);
				// minecart chest
				rand.nextLong();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			int end = sectionCount * 5 - 1;
			int planks = mineshaftType.planks;
			// clear space
			fillWithBlocks(world, bounds, 0, 0, 0, 2, 1, end, Blocks.AIR, Blocks.AIR, false);
			randomlyFill(world, bounds, rand, 0.8F, 0, 2, 0, 2, 2, end, Blocks.AIR, Blocks.AIR, false);

			// place lots of cobwebs if spiders present
			if (hasSpiders) {
				// Missing skylight value of 8 here.
				// Hopefully failing on this condition is so rare that this
				// is okay.
				// It would probably only affect chest generation
				randomlyFill(world, bounds, rand, 0.6F, 0, 0, 0, 2, 1, end, Blocks.WEB, Blocks.AIR, false);
			}

			// We need to calculate the skylight for the below cobwebs, no way
			// around it. Since the skylight is checked before the random chance
			// in this case, not checking the skylight would desync the RNG's
			// seed. TODO: this is slow, look for a faster method?
			Storage3D skylight = SkylightCalculator.calcSkylight(world, bounds.getMinX() - 8, bounds.getMinZ() - 8,
					bounds.getMaxX() + 8, bounds.getMaxZ() + 8);
			for (int section = 0; section < sectionCount; section++) {
				int z = 2 + section * 5;
				// support
				placeSupport(world, bounds, 0, 0, z, 2, 2, rand);
				// cobwebs
				placeCobWeb(world, skylight, bounds, rand, 0.1F, 0, 2, z - 1);
				placeCobWeb(world, skylight, bounds, rand, 0.1F, 2, 2, z - 1);
				placeCobWeb(world, skylight, bounds, rand, 0.1F, 0, 2, z + 1);
				placeCobWeb(world, skylight, bounds, rand, 0.1F, 2, 2, z + 1);
				placeCobWeb(world, skylight, bounds, rand, 0.05F, 0, 2, z - 2);
				placeCobWeb(world, skylight, bounds, rand, 0.05F, 2, 2, z - 2);
				placeCobWeb(world, skylight, bounds, rand, 0.05F, 0, 2, z + 2);
				placeCobWeb(world, skylight, bounds, rand, 0.05F, 2, 2, z + 2);

				// chest(s)
				if (rand.nextInt(100) == 0) {
					generateChest(world, bounds, rand, 2, 0, z - 1);
				}

				if (rand.nextInt(100) == 0) {
					generateChest(world, bounds, rand, 0, 0, z + 1);
				}

				// spawner
				if (hasSpiders && !hasPlacedSpawner) {
					int yOff = getYWithOffset(0);
					int spawnerZ = z - 1 + rand.nextInt(3);
					int xOff = getXWithOffset(1, spawnerZ);
					int zOff = getZWithOffset(1, spawnerZ);

					if (bounds.contains(xOff, yOff, zOff)) {
						// Another missing skylight condition here.
						// Hopefully that doesn't affect anything important.
						hasPlacedSpawner = true;
						world.set(xOff, yOff, zOff, Blocks.MOB_SPAWNER);
					}
				}
			}

			// wooden bridge over gaps in floor
			for (int x = 0; x <= 2; x++) {
				for (int z = 0; z <= end; z++) {
					int block = getBlock(world, x, -1, z, bounds);

					if (Blocks.isAir(block)) {
						// Another missing skylight condition here.
						// Hopefully that doesn't affect anything important.
						setBlock(world, planks, x, -1, z, bounds);
					}
				}
			}

			// rails
			if (hasRails) {
				for (int z = 0; z <= end; z++) {
					int blockBelow = getBlock(world, 1, -1, z, bounds);

					if (!Blocks.isAir(blockBelow) && Blocks.isOpaqueCube(blockBelow)) {
						// Another missing skylight condition here.
						// This chance is 0.9 if the skylight is > 8.
						// Hopefully this doesn't affect anything important,
						// since rand.nextFloat() is still called either
						// way.
						float chance = 0.7F;
						randomlyPlaceBlock(world, bounds, rand, chance, 1, 0, z, Blocks.RAIL);
					}
				}
			}

			return true;
		}

		private void placeSupport(Storage3D world, AABB bounds, int xMin, int yMin, int z, int yMax, int xMax,
				Random rand) {
			// check if we're actually supporting anything
			if (isCeilingAbove(world, bounds, xMin, xMax, yMax, z)) {
				int planks = mineshaftType.planks;
				int fence = mineshaftType.fence;
				// fences on sides
				fillWithBlocks(world, bounds, xMin, yMin, z, xMin, yMax - 1, z, fence, Blocks.AIR, false);
				fillWithBlocks(world, bounds, xMax, yMin, z, xMax, yMax - 1, z, fence, Blocks.AIR, false);

				if (rand.nextInt(4) == 0) {
					// broken support beam
					fillWithBlocks(world, bounds, xMin, yMax, z, xMin, yMax, z, planks, Blocks.AIR, false);
					fillWithBlocks(world, bounds, xMax, yMax, z, xMax, yMax, z, planks, Blocks.AIR, false);
				} else {
					// support beam
					fillWithBlocks(world, bounds, xMin, yMax, z, xMax, yMax, z, planks, Blocks.AIR, false);
					// torch(es)
					randomlyPlaceBlock(world, bounds, rand, 0.05F, xMin + 1, yMax, z - 1, Blocks.TORCH);
					randomlyPlaceBlock(world, bounds, rand, 0.05F, xMin + 1, yMax, z + 1, Blocks.TORCH);
				}
			}
		}

		private void placeCobWeb(Storage3D world, Storage3D skylight, AABB bounds, Random rand, float chance, int x,
				int y, int z) {
			int xOff = getXWithOffset(x, z);
			int yOff = getYWithOffset(y);
			int zOff = getZWithOffset(x, z);
			if (bounds.contains(xOff, yOff, zOff)) {
				if (skylight.get(xOff, yOff, zOff) < 8) {
					randomlyPlaceBlock(world, bounds, rand, chance, x, y, z, Blocks.WEB);
				}
			}
		}
	}

	public static class Crossing extends MineshaftComponent {
		private boolean isMultipleFloors;

		public Crossing(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing, Type mineshaftType) {
			super(distanceFromStart, mineshaftType);
			setFacing(facing);
			setBoundingBox(bounds);
			isMultipleFloors = bounds.getYSize() > 3;
		}

		public static AABB findCrossing(List<Component> components, Random rand, int x, int y, int z,
				EnumFacing facing) {
			int minX = x;
			int minY = y;
			int minZ = z;
			int maxX = x;
			int maxY = y + 2;
			int maxZ = z;

			if (rand.nextInt(4) == 0) {
				// take into a tall crossing
				maxY += 4;
			}

			switch (facing) {
			case NORTH:
			default:
				minX = x - 1;
				maxX = x + 3;
				minZ = z - 4;
				break;

			case SOUTH:
				minX = x - 1;
				maxX = x + 3;
				maxZ = z + 3 + 1;
				break;

			case WEST:
				minX = x - 4;
				minZ = z - 1;
				maxZ = z + 3;
				break;

			case EAST:
				maxX = x + 3 + 1;
				minZ = z - 1;
				maxZ = z + 3;
			}

			AABB bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
			return Component.findIntersecting(components, bounds) != null ? null : bounds;
		}

		@Override
		public void addMoreComponents(Component start, List<Component> components, Random rand) {
			int distFromStart = getDistanceFromStart();

			// add components in every direction on bottom floor
			switch (getFacing()) {
			case NORTH:
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() + 1, EnumFacing.WEST, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() + 1, EnumFacing.EAST, distFromStart);
				break;

			case SOUTH:
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() + 1, EnumFacing.WEST, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() + 1, EnumFacing.EAST, distFromStart);
				break;

			case WEST:
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() + 1, EnumFacing.WEST, distFromStart);
				break;

			case EAST:
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, distFromStart);
				generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
						getBoundingBox().getMinY(), getBoundingBox().getMinZ() + 1, EnumFacing.EAST, distFromStart);
				break;

			default:
				throw new AssertionError();
			}

			// randomly add components on top floor
			if (isMultipleFloors) {
				if (rand.nextBoolean()) {
					generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
							getBoundingBox().getMinY() + 3 + 1, getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
							distFromStart);
				}

				if (rand.nextBoolean()) {
					generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY() + 3 + 1, getBoundingBox().getMinZ() + 1, EnumFacing.WEST,
							distFromStart);
				}

				if (rand.nextBoolean()) {
					generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY() + 3 + 1, getBoundingBox().getMinZ() + 1, EnumFacing.EAST,
							distFromStart);
				}

				if (rand.nextBoolean()) {
					generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() + 1,
							getBoundingBox().getMinY() + 3 + 1, getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
							distFromStart);
				}
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			int planks = mineshaftType.planks;

			// clear space
			if (isMultipleFloors) {
				fillWithBlocks(world, bounds, getBoundingBox().getMinX() + 1, getBoundingBox().getMinY(),
						getBoundingBox().getMinZ(), getBoundingBox().getMaxX() - 1, getBoundingBox().getMinY() + 3 - 1,
						getBoundingBox().getMaxZ(), Blocks.AIR, Blocks.AIR, false);
				fillWithBlocks(world, bounds, getBoundingBox().getMinX(), getBoundingBox().getMinY(),
						getBoundingBox().getMinZ() + 1, getBoundingBox().getMaxX(), getBoundingBox().getMinY() + 3 - 1,
						getBoundingBox().getMaxZ() - 1, Blocks.AIR, Blocks.AIR, false);
				fillWithBlocks(world, bounds, getBoundingBox().getMinX() + 1, getBoundingBox().getMaxY() - 2,
						getBoundingBox().getMinZ(), getBoundingBox().getMaxX() - 1, getBoundingBox().getMaxY(),
						getBoundingBox().getMaxZ(), Blocks.AIR, Blocks.AIR, false);
				fillWithBlocks(world, bounds, getBoundingBox().getMinX(), getBoundingBox().getMaxY() - 2,
						getBoundingBox().getMinZ() + 1, getBoundingBox().getMaxX(), getBoundingBox().getMaxY(),
						getBoundingBox().getMaxZ() - 1, Blocks.AIR, Blocks.AIR, false);
				fillWithBlocks(world, bounds, getBoundingBox().getMinX() + 1, getBoundingBox().getMinY() + 3,
						getBoundingBox().getMinZ() + 1, getBoundingBox().getMaxX() - 1, getBoundingBox().getMinY() + 3,
						getBoundingBox().getMaxZ() - 1, Blocks.AIR, Blocks.AIR, false);
			} else {
				fillWithBlocks(world, bounds, getBoundingBox().getMinX() + 1, getBoundingBox().getMinY(),
						getBoundingBox().getMinZ(), getBoundingBox().getMaxX() - 1, getBoundingBox().getMaxY(),
						getBoundingBox().getMaxZ(), Blocks.AIR, Blocks.AIR, false);
				fillWithBlocks(world, bounds, getBoundingBox().getMinX(), getBoundingBox().getMinY(),
						getBoundingBox().getMinZ() + 1, getBoundingBox().getMaxX(), getBoundingBox().getMaxY(),
						getBoundingBox().getMaxZ() - 1, Blocks.AIR, Blocks.AIR, false);
			}

			// pillars
			placeSupportPillar(world, bounds, getBoundingBox().getMinX() + 1, getBoundingBox().getMinY(),
					getBoundingBox().getMinZ() + 1, getBoundingBox().getMaxY());
			placeSupportPillar(world, bounds, getBoundingBox().getMinX() + 1, getBoundingBox().getMinY(),
					getBoundingBox().getMaxZ() - 1, getBoundingBox().getMaxY());
			placeSupportPillar(world, bounds, getBoundingBox().getMaxX() - 1, getBoundingBox().getMinY(),
					getBoundingBox().getMinZ() + 1, getBoundingBox().getMaxY());
			placeSupportPillar(world, bounds, getBoundingBox().getMaxX() - 1, getBoundingBox().getMinY(),
					getBoundingBox().getMaxZ() - 1, getBoundingBox().getMaxY());

			// wooden bridge over gaps in floor
			for (int x = getBoundingBox().getMinX(); x <= getBoundingBox().getMaxX(); ++x) {
				for (int z = getBoundingBox().getMinZ(); z <= getBoundingBox().getMaxZ(); ++z) {
					if (Blocks.isAir(getBlock(world, x, getBoundingBox().getMinY() - 1, z, bounds))) {
						// Missing sky brightness test here.
						// Hopefully it's okay.
						setBlock(world, planks, x, getBoundingBox().getMinY() - 1, z, bounds);
					}
				}
			}

			return true;
		}

		private void placeSupportPillar(Storage3D world, AABB bounds, int x, int yMin, int z, int yMax) {
			if (!Blocks.isAir(getBlock(world, x, yMax + 1, z, bounds))) {
				fillWithBlocks(world, bounds, x, yMin, z, x, yMax, z, mineshaftType.planks, Blocks.AIR, false);
			}
		}
	}

	abstract static class MineshaftComponent extends Component {
		protected Type mineshaftType;

		public MineshaftComponent(int distancceFromStart, Type mineshaftType) {
			super(distancceFromStart);
			this.mineshaftType = mineshaftType;
		}

		protected boolean isCeilingAbove(Storage3D world, AABB bounds, int minX, int maxX, int y, int z) {
			for (int x = minX; x <= maxX; x++) {
				if (Blocks.isAir(getBlock(world, x, y + 1, z, bounds))) {
					return false;
				}
			}

			return true;
		}
	}

	public static class DirtRoom extends MineshaftComponent {
		public final List<AABB> connectedRooms = new ArrayList<>();

		public DirtRoom(int distanceFromStart, Random rand, int x, int z, Type mineshaftType) {
			super(distanceFromStart, mineshaftType);
			this.mineshaftType = mineshaftType;
			setBoundingBox(new AABB(x, 50, z, x + 7 + rand.nextInt(6), 54 + rand.nextInt(6), z + 7 + rand.nextInt(6)));
		}

		@Override
		public void addMoreComponents(Component start, List<Component> components, Random rand) {
			int distFromStart = getDistanceFromStart();
			int corridorYRange = getBoundingBox().getYSize() - 3 - 1;

			if (corridorYRange <= 0) {
				corridorYRange = 1;
			}

			int x;

			// Add components north
			for (x = 0; x < getBoundingBox().getXSize(); x += 4) {
				x += rand.nextInt(getBoundingBox().getXSize());

				if (x + 3 > getBoundingBox().getXSize()) {
					break;
				}

				MineshaftComponent component = generateAndAddComponent(start, components, rand,
						getBoundingBox().getMinX() + x, getBoundingBox().getMinY() + rand.nextInt(corridorYRange) + 1,
						getBoundingBox().getMinZ() - 1, EnumFacing.NORTH, distFromStart);

				if (component != null) {
					AABB bounds = component.getBoundingBox();
					connectedRooms.add(new AABB(bounds.getMinX(), bounds.getMinY(), getBoundingBox().getMinZ(),
							bounds.getMaxX(), bounds.getMaxY(), getBoundingBox().getMinZ() + 1));
				}
			}

			// Add components south
			for (x = 0; x < getBoundingBox().getXSize(); x += 4) {
				x += rand.nextInt(getBoundingBox().getXSize());

				if (x + 3 > getBoundingBox().getXSize()) {
					break;
				}

				MineshaftComponent component = generateAndAddComponent(start, components, rand,
						getBoundingBox().getMinX() + x, getBoundingBox().getMinY() + rand.nextInt(corridorYRange) + 1,
						getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH, distFromStart);

				if (component != null) {
					AABB bounds = component.getBoundingBox();
					connectedRooms.add(new AABB(bounds.getMinX(), bounds.getMinY(), getBoundingBox().getMaxZ() - 1,
							bounds.getMaxX(), bounds.getMaxY(), getBoundingBox().getMaxZ()));
				}
			}

			int z;

			// Add components west
			for (z = 0; z < getBoundingBox().getZSize(); z += 4) {
				z += rand.nextInt(getBoundingBox().getZSize());

				if (z + 3 > getBoundingBox().getZSize()) {
					break;
				}

				MineshaftComponent component = generateAndAddComponent(start, components, rand,
						getBoundingBox().getMinX() - 1, getBoundingBox().getMinY() + rand.nextInt(corridorYRange) + 1,
						getBoundingBox().getMinZ() + z, EnumFacing.WEST, distFromStart);

				if (component != null) {
					AABB bounds = component.getBoundingBox();
					connectedRooms.add(new AABB(getBoundingBox().getMinX(), bounds.getMinY(), bounds.getMinZ(),
							getBoundingBox().getMinX() + 1, bounds.getMaxY(), bounds.getMaxZ()));
				}
			}

			// Add components east
			for (z = 0; z < getBoundingBox().getZSize(); z += 4) {
				z += rand.nextInt(getBoundingBox().getZSize());

				if (z + 3 > getBoundingBox().getZSize()) {
					break;
				}

				MineshaftComponent component = generateAndAddComponent(start, components, rand,
						getBoundingBox().getMaxX() + 1, getBoundingBox().getMinY() + rand.nextInt(corridorYRange) + 1,
						getBoundingBox().getMinZ() + z, EnumFacing.EAST, distFromStart);

				if (component != null) {
					AABB bounds = component.getBoundingBox();
					connectedRooms.add(new AABB(getBoundingBox().getMaxX() - 1, bounds.getMinY(), bounds.getMinZ(),
							getBoundingBox().getMaxX(), bounds.getMaxY(), bounds.getMaxZ()));
				}
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// dirt floor
			fillWithBlocks(world, bounds, getBoundingBox().getMinX(), getBoundingBox().getMinY(),
					getBoundingBox().getMinZ(), getBoundingBox().getMaxX(), getBoundingBox().getMinY(),
					getBoundingBox().getMaxZ(), Blocks.DIRT, Blocks.AIR, true);
			// clear space
			fillWithBlocks(world, bounds, getBoundingBox().getMinX(), getBoundingBox().getMinY() + 1,
					getBoundingBox().getMinZ(), getBoundingBox().getMaxX(),
					Math.min(getBoundingBox().getMinY() + 3, getBoundingBox().getMaxY()), getBoundingBox().getMaxZ(),
					Blocks.AIR, Blocks.AIR, false);

			// add gaps for connected corridors
			for (AABB connectedRoom : connectedRooms) {
				fillWithBlocks(world, bounds, connectedRoom.getMinX(), connectedRoom.getMaxY() - 2,
						connectedRoom.getMinZ(), connectedRoom.getMaxX(), connectedRoom.getMaxY(),
						connectedRoom.getMaxZ(), Blocks.AIR, Blocks.AIR, false);
			}

			fillDome(world, bounds, getBoundingBox().getMinX(), getBoundingBox().getMinY() + 4,
					getBoundingBox().getMinZ(), getBoundingBox().getMaxX(), getBoundingBox().getMaxY(),
					getBoundingBox().getMaxZ(), Blocks.AIR, false);
			return true;
		}
	}

	public static class Stairs extends MineshaftComponent {
		public Stairs(int distanceFromStart, Random rand, AABB bounds, EnumFacing facing, Type mineshaftType) {
			super(distanceFromStart, mineshaftType);
			setFacing(facing);
			setBoundingBox(bounds);
		}

		public static AABB findStairs(List<Component> components, Random rand, int x, int y, int z, EnumFacing facing) {
			int minX = x;
			int minZ = z;
			int maxX = x;
			int maxZ = z;

			switch (facing) {
			case NORTH:
			default:
				maxX = x + 2;
				minZ = z - 8;
				break;

			case SOUTH:
				maxX = x + 2;
				maxZ = z + 8;
				break;

			case WEST:
				minX = x - 8;
				maxZ = z + 2;
				break;

			case EAST:
				maxX = x + 8;
				maxZ = z + 2;
			}

			AABB bounds = new AABB(minX, y - 5, minZ, maxX, y + 2, maxZ);
			return Component.findIntersecting(components, bounds) != null ? null : bounds;
		}

		@Override
		public void addMoreComponents(Component start, List<Component> components, Random rand) {
			int distanceFromStart = getDistanceFromStart();

			EnumFacing facing = getFacing();
			if (facing != null) {
				switch (facing) {
				case NORTH:
					generateAndAddComponent(start, components, rand, getBoundingBox().getMinX(),
							getBoundingBox().getMinY(), getBoundingBox().getMinZ() - 1, EnumFacing.NORTH,
							distanceFromStart);
					break;

				case SOUTH:
					generateAndAddComponent(start, components, rand, getBoundingBox().getMinX(),
							getBoundingBox().getMinY(), getBoundingBox().getMaxZ() + 1, EnumFacing.SOUTH,
							distanceFromStart);
					break;

				case WEST:
					generateAndAddComponent(start, components, rand, getBoundingBox().getMinX() - 1,
							getBoundingBox().getMinY(), getBoundingBox().getMinZ(), EnumFacing.WEST, distanceFromStart);
					break;

				case EAST:
					generateAndAddComponent(start, components, rand, getBoundingBox().getMaxX() + 1,
							getBoundingBox().getMinY(), getBoundingBox().getMinZ(), EnumFacing.EAST, distanceFromStart);
					break;

				default:
					throw new AssertionError();
				}
			}
		}

		@Override
		public boolean placeInWorld(Storage3D world, Random rand, AABB bounds) {
			if (isLiquidInWalls(world, bounds)) {
				return false;
			}

			// entrance and exit
			fillWithBlocks(world, bounds, 0, 5, 0, 2, 7, 1, Blocks.AIR, Blocks.AIR, false);
			fillWithBlocks(world, bounds, 0, 0, 7, 2, 2, 8, Blocks.AIR, Blocks.AIR, false);

			// stairs
			for (int i = 0; i < 5; i++) {
				fillWithBlocks(world, bounds, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, Blocks.AIR,
						Blocks.AIR, false);
			}

			return true;
		}
	}

	public static enum Type {
		NORMAL(Blocks.PLANKS, Blocks.OAK_FENCE), MESA(Blocks.PLANKS, Blocks.DARK_OAK_FENCE);

		public final int planks;
		public final int fence;

		private Type(int planks, int fence) {
			this.planks = planks;
			this.fence = fence;
		}
	}
}
