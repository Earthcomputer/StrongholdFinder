package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTLong extends NBTNumber {

	private long value;

	NBTLong() {
	}

	public NBTLong(long value) {
		this.value = value;
	}

	@Override
	public long getLong() {
		return value;
	}

	@Override
	public double getDouble() {
		return value;
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		value = dataInput.readLong();
	}

	@Override
	public int getId() {
		return LONG;
	}

}
