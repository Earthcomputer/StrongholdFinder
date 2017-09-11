package seedfinder.nbt;

public abstract class NBTNumber extends NBTBase {

	public abstract long getLong();

	public int getInt() {
		return (int) getLong();
	}

	public short getShort() {
		return (short) getLong();
	}

	public byte getByte() {
		return (byte) getLong();
	}

	public float getFloat() {
		return (float) getDouble();
	}

	public abstract double getDouble();

	@Override
	public boolean isNumber() {
		return true;
	}

}
