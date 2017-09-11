package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTFloat extends NBTNumber {

	private float value;

	NBTFloat() {
	}

	public NBTFloat(float value) {
		this.value = value;
	}

	@Override
	public long getLong() {
		return (long) value;
	}

	@Override
	public double getDouble() {
		return value;
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		value = dataInput.readFloat();
	}

	@Override
	public int getId() {
		return FLOAT;
	}

}
