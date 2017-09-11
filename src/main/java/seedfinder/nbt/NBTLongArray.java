package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTLongArray extends NBTBase {

	private long[] value;

	NBTLongArray() {
	}

	public NBTLongArray(long[] value) {
		this.value = value;
	}

	public long[] getLongArray() {
		return value;
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		int size = dataInput.readInt();
		value = new long[size];
		for (int i = 0; i < size; i++) {
			value[i] = dataInput.readLong();
		}
	}

	@Override
	public int getId() {
		return LONG_ARRAY;
	}

}
