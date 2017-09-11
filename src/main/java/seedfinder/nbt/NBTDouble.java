package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTDouble extends NBTNumber {

	private double value;

	NBTDouble() {
	}

	public NBTDouble(double value) {
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
		value = dataInput.readDouble();
	}

	@Override
	public int getId() {
		return DOUBLE;
	}

}
