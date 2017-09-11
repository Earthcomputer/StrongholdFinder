package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTByte extends NBTNumber {

	private byte value;

	NBTByte() {
	}

	public NBTByte(byte value) {
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
		value = dataInput.readByte();
	}

	@Override
	public int getId() {
		return BYTE;
	}

}
