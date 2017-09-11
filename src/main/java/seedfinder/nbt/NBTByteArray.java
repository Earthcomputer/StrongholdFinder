package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTByteArray extends NBTBase {

	private byte[] value;

	NBTByteArray() {
	}

	public NBTByteArray(byte[] value) {
		this.value = value;
	}

	public byte[] getByteArray() {
		return value;
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		int size = dataInput.readInt();
		value = new byte[size];
		dataInput.readFully(value);
	}

	@Override
	public int getId() {
		return BYTE_ARRAY;
	}

}
