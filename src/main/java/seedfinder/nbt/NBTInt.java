package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTInt extends NBTNumber {

	private int value;

	NBTInt() {
	}

	public NBTInt(int value) {
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
		value = dataInput.readInt();
	}

	@Override
	public int getId() {
		return INT;
	}

}
