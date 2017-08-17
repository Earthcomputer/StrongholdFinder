package seedfinder;

public final class ChunkPos {

	private int x;
	private int z;

	public ChunkPos(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + z + ")";
	}

	@Override
	public int hashCode() {
		return x + 31 * z;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof ChunkPos && equals((ChunkPos) other);
	}

	public boolean equals(ChunkPos other) {
		return x == other.x && z == other.z;
	}

}
