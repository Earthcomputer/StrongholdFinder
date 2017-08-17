package seedfinder;

public final class AABB {

	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	public AABB(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMinZ() {
		return minZ;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getMaxZ() {
		return maxZ;
	}

	public AABB getOffset(int x, int y, int z) {
		return new AABB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
	}

	public static AABB max(AABB a, AABB b) {
		return new AABB(Math.min(a.minX, b.minX), Math.min(a.minY, b.minY), Math.min(a.minZ, b.minZ),
				Math.max(a.maxX, b.maxX), Math.max(a.maxY, b.maxY), Math.max(a.maxZ, b.maxZ));
	}

	public static AABB createComponentBoundingBox(int structureMinX, int structureMinY, int structureMinZ, int xMin,
			int yMin, int zMin, int xMax, int yMax, int zMax, EnumFacing facing) {
		switch (facing) {
		case NORTH:
			return new AABB(structureMinX + xMin, structureMinY + yMin, structureMinZ - zMax + 1 + zMin,
					structureMinX + xMax - 1 + xMin, structureMinY + yMax - 1 + yMin, structureMinZ + zMin);

		case SOUTH:
			return new AABB(structureMinX + xMin, structureMinY + yMin, structureMinZ + zMin,
					structureMinX + xMax - 1 + xMin, structureMinY + yMax - 1 + yMin, structureMinZ + zMax - 1 + zMin);

		case WEST:
			return new AABB(structureMinX - zMax + 1 + zMin, structureMinY + yMin, structureMinZ + xMin,
					structureMinX + zMin, structureMinY + yMax - 1 + yMin, structureMinZ + xMax - 1 + xMin);

		case EAST:
			return new AABB(structureMinX + zMin, structureMinY + yMin, structureMinZ + xMin,
					structureMinX + zMax - 1 + zMin, structureMinY + yMax - 1 + yMin, structureMinZ + xMax - 1 + xMin);

		default:
			return new AABB(structureMinX + xMin, structureMinY + yMin, structureMinZ + zMin,
					structureMinX + xMax - 1 + xMin, structureMinY + yMax - 1 + yMin, structureMinZ + zMax - 1 + zMin);
		}
	}

	public boolean intersectsWith(AABB other) {
		return this.maxX >= other.minX && this.minX <= other.maxX && this.maxZ >= other.minZ && this.minZ <= other.maxZ
				&& this.maxY >= other.minY && this.minY <= other.maxY;
	}

	public boolean intersectsWith(int minX, int minZ, int maxX, int maxZ) {
		return this.maxX >= minX && this.minX <= maxX && this.maxZ >= minZ && this.minZ <= maxZ;
	}

	public boolean contains(int x, int y, int z) {
		return x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY && y <= maxY;
	}

	public boolean contains(BlockPos pos) {
		return pos.getX() >= this.minX && pos.getX() <= this.maxX && pos.getZ() >= this.minZ && pos.getZ() <= this.maxZ
				&& pos.getY() >= this.minY && pos.getY() <= this.maxY;
	}

	public int getXSize() {
		return this.maxX - this.minX + 1;
	}

	public int getYSize() {
		return this.maxY - this.minY + 1;
	}

	public int getZSize() {
		return this.maxZ - this.minZ + 1;
	}

	@Override
	public String toString() {
		return "from(" + minX + ", " + minY + ", " + minZ + ") to(" + maxX + ", " + maxY + ", " + maxZ + ")";
	}

}
