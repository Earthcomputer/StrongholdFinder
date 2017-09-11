package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTShort extends NBTNumber {

	private short value;

	NBTShort() {
	}

	public NBTShort(short value) {
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
		value = dataInput.readShort();
	}

	@Override
	public int getId() {
		return SHORT;
	}

}
