package seedfinder.util;

public final class BlockPos {

	private int x;
	private int y;
	private int z;

	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public BlockPos down() {
		return new BlockPos(x, y - 1, z);
	}

	public BlockPos up() {
		return new BlockPos(x, y + 1, z);
	}

	public BlockPos north() {
		return new BlockPos(x, y, z - 1);
	}

	public BlockPos south() {
		return new BlockPos(x, y, z + 1);
	}

	public BlockPos west() {
		return new BlockPos(x - 1, y, z);
	}

	public BlockPos east() {
		return new BlockPos(x + 1, y, z);
	}

	public BlockPos down(int distance) {
		return new BlockPos(x, y - distance, z);
	}

	public BlockPos up(int distance) {
		return new BlockPos(x, y + distance, z);
	}

	public BlockPos north(int distance) {
		return new BlockPos(x, y, z - distance);
	}

	public BlockPos south(int distance) {
		return new BlockPos(x, y, z + distance);
	}

	public BlockPos west(int distance) {
		return new BlockPos(x - distance, y, z);
	}

	public BlockPos east(int distance) {
		return new BlockPos(x + distance, y, z);
	}

	public BlockPos offset(EnumFacing direction) {
		return new BlockPos(x + direction.getXOffset(), y + direction.getYOffset(), z + direction.getZOffset());
	}

	public BlockPos offset(EnumFacing direction, int distance) {
		return new BlockPos(x + direction.getXOffset() * distance, y + direction.getYOffset() * distance,
				z + direction.getZOffset() * distance);
	}

	public BlockPos add(BlockPos other) {
		return add(other.x, other.y, other.z);
	}

	public BlockPos add(int x, int y, int z) {
		return new BlockPos(this.x + x, this.y + y, this.z + z);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	@Override
	public int hashCode() {
		int hash = x;
		hash = 31 * hash + y;
		hash = 31 * hash + z;
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BlockPos && equals((BlockPos) other);
	}

	public boolean equals(BlockPos other) {
		return x == other.x && y == other.y && z == other.z;
	}

}
