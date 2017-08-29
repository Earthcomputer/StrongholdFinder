package seedfinder.structure;

import java.util.List;
import java.util.Random;

import seedfinder.AABB;
import seedfinder.Blocks;
import seedfinder.EnumFacing;
import seedfinder.Storage3D;

/**
 * A structure component is a 'piece' of a large structure, e.g. a room
 */
public abstract class Component {

	private AABB boundingBox;
	private EnumFacing facing;
	private int distanceFromStart;

	public Component() {
	}

	public Component(int distanceFromStart) {
		this.distanceFromStart = distanceFromStart;
	}

	public AABB getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(AABB boundingBox) {
		this.boundingBox = boundingBox;
	}

	public EnumFacing getFacing() {
		return facing;
	}

	public void setFacing(EnumFacing facing) {
		this.facing = facing;
	}

	public int getDistanceFromStart() {
		return distanceFromStart;
	}

	/**
	 * Called to add more components leading from this one
	 */
	public void addMoreComponents(Component startComponent, List<Component> components, Random rand) {
	}

	/**
	 * Builds this component in the world
	 */
	public abstract boolean placeInWorld(Storage3D world, Random rand, AABB bounds);

	/**
	 * Finds the first component in the list of components given whose bounding
	 * box intersects the given bounding box, or <tt>null</tt> if no match was
	 * found
	 */
	public static Component findIntersecting(List<Component> components, AABB bounds) {
		return components.stream()
				.filter(it -> it.getBoundingBox() != null && it.getBoundingBox().intersectsWith(bounds)).findFirst()
				.orElse(null);
	}

	/**
	 * Determines whether there is a liquid in the walls of the given bounding
	 * box
	 */
	public boolean isLiquidInWalls(Storage3D world, AABB bounds) {
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

	/**
	 * Gets an x-position, offset from this component's position and
	 * orientation.
	 */
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

	/**
	 * Gets a y-position, offset from this component's position and orientation.
	 */
	protected int getYWithOffset(int y) {
		return this.getFacing() == null ? y : y + this.boundingBox.getMinY();
	}

	/**
	 * Gets a z-position, offset from this component's position and orientation.
	 */
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

	/**
	 * Gets a block in the world, relative to this component's position, or air
	 * if the block is not inside the given bounding box
	 */
	protected int getBlock(Storage3D world, int x, int y, int z, AABB boundingBox) {
		int xOff = getXWithOffset(x, z);
		int yOff = getYWithOffset(y);
		int zOff = getZWithOffset(x, z);
		if (!boundingBox.contains(xOff, yOff, zOff)) {
			return Blocks.AIR;
		} else {
			return world.get(xOff, yOff, zOff);
		}
	}

	/**
	 * Sets a block in the world, relative to this component's position and
	 * orientation, if the block is inside the given bounding box.
	 */
	protected void setBlock(Storage3D world, int block, int x, int y, int z, AABB bounds) {
		int offX = getXWithOffset(x, z);
		int offY = getYWithOffset(y);
		int offZ = getZWithOffset(x, z);
		if (bounds.contains(offX, offY, offZ)) {
			world.set(offX, offY, offZ, block);
		}
	}

	/**
	 * Fills the given area with blocks.
	 */
	protected void fillWithBlocks(Storage3D world, AABB bounds, int xMin, int yMin, int zMin, int xMax, int yMax,
			int zMax, int boundaryBlock, int insideBlock, boolean preserveAir) {
		for (int y = yMin; y <= yMax; y++) {
			for (int x = xMin; x <= xMax; x++) {
				for (int z = zMin; z <= zMax; z++) {
					if (!preserveAir || !Blocks.isAir(this.getBlock(world, x, y, z, bounds))) {
						if (y != yMin && y != yMax && x != xMin && x != xMax && z != zMin && z != zMax) {
							this.setBlock(world, insideBlock, x, y, z, bounds);
						} else {
							this.setBlock(world, boundaryBlock, x, y, z, bounds);
						}
					}
				}
			}
		}
	}

	/**
	 * Fills the given area with randomized blocks generated by the given block
	 * selector
	 */
	protected void fillWithRandomizedBlocks(Storage3D world, AABB bounds, int minX, int minY, int minZ, int maxX,
			int maxY, int maxZ, boolean preserveAir, Random rand, BlockSelector blockSelector) {
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (!preserveAir || !Blocks.isAir(this.getBlock(world, x, y, z, bounds))) {
						blockSelector.selectBlocks(rand, x, y, z,
								y == minY || y == maxY || x == minX || x == maxX || z == minZ || z == maxZ);
						this.setBlock(world, blockSelector.getBlockState(), x, y, z, bounds);
					}
				}
			}
		}
	}

	/**
	 * Fills a dome with blocks
	 */
	protected void fillDome(Storage3D world, AABB bounds, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
			int block, boolean preserveAir) {
		float xSize = maxX - minX + 1;
		float ySize = maxY - minY + 1;
		float zSize = maxZ - minZ + 1;
		float xRadius = minX + xSize / 2f;
		float zRadius = minZ + zSize / 2f;

		for (int y = minY; y <= maxY; y++) {
			float normDy = (y - minY) / ySize;

			for (int x = minX; x <= maxX; x++) {
				float normDx = (x - xRadius) / (xSize * 0.5f);

				for (int z = minZ; z <= maxZ; z++) {
					float normDz = (z - zRadius) / (zSize * 0.5f);

					if (!preserveAir || !Blocks.isAir(getBlock(world, x, y, z, bounds))) {
						float normDistSq = normDx * normDx + normDy * normDy + normDz * normDz;

						if (normDistSq <= 1.05f) {
							setBlock(world, block, x, y, z, bounds);
						}
					}
				}
			}
		}
	}

	/**
	 * Clears a column of blocks from the current position upwards, replacing it
	 * with air. Stops once air is reached.
	 */
	protected void clearAbove(Storage3D world, int x, int y, int z, AABB bounds) {
		int offX = getXWithOffset(x, z);
		int offY = getYWithOffset(y);
		int offZ = getZWithOffset(x, z);

		if (bounds.contains(offX, offY, offZ)) {
			while (!Blocks.isAir(world.get(offX, offY, offZ)) && offY < 255) {
				world.set(offX, offY, offZ, Blocks.AIR);
				offY++;
			}
		}
	}

	/**
	 * Replaces a column of blocks from the current position downwards,
	 * replacing it with a given substitute block. Stops once a solid block is
	 * reached.
	 */
	protected void fillBelow(Storage3D world, int substitute, int x, int y, int z, AABB bounds) {
		int offX = getXWithOffset(x, z);
		int offY = getYWithOffset(y);
		int offZ = getZWithOffset(x, z);

		if (bounds.contains(offX, offY, offZ)) {
			while ((Blocks.isAir(world.get(offX, offY, offZ)) || Blocks.isLiquid(world.get(offX, offY, offZ)))
					&& offY > 1) {
				world.set(offX, offY, offZ, substitute);
				offY--;
			}
		}
	}

	/**
	 * Returns the block above the top solid block
	 */
	protected int getTopSolidOrLiquidBlock(Storage3D world, int x, int z) {
		int y;
		for (y = 255; y >= 0; y--) {
			int block = world.get(x, y, z);
			if (Blocks.blocksMovement(block) && block != Blocks.LEAVES && block != Blocks.LEAVES2) {
				break;
			}
		}
		return y + 1;
	}

	/**
	 * Places a chest in the world at the given coordinates relative to this
	 * component's position and orientation
	 */
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

	protected void randomlyFill(Storage3D world, AABB bounds, Random rand, float chance, int xMin, int yMin,
			int zMin, int xMax, int yMax, int zMax, int wallBlock, int centerBlock, boolean requireNonAir) {
		for (int y = yMin; y <= yMax; ++y) {
			for (int x = xMin; x <= xMax; ++x) {
				for (int z = zMin; z <= zMax; ++z) {
					if (rand.nextFloat() <= chance
							&& (!requireNonAir || !Blocks.isAir(getBlock(world, x, y, z, bounds)))) {
						if (y == yMin || y == yMax || x == xMin || x == xMax || z == zMin || z == zMax) {
							setBlock(world, wallBlock, x, y, z, bounds);
						} else {
							setBlock(world, centerBlock, x, y, z, bounds);
						}
					}
				}
			}
		}
	}

	/**
	 * Places a block with a chance
	 */
	protected void randomlyPlaceBlock(Storage3D world, AABB bounds, Random rand, float chance, int x, int y, int z,
			int block) {
		if (rand.nextFloat() < chance) {
			setBlock(world, block, x, y, z, bounds);
		}
	}

	/**
	 * Generates blocks randomly
	 */
	public abstract static class BlockSelector {
		public abstract void selectBlocks(Random rand, int x, int y, int z, boolean wall);

		public abstract int getBlockState();
	}

}
