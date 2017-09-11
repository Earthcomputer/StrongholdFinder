package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTIntArray extends NBTBase {

	private int[] value;

	NBTIntArray() {
	}

	public NBTIntArray(int[] value) {
		this.value = value;
	}

	public int[] getIntArray() {
		return value;
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		int size = dataInput.readInt();
		value = new int[size];
		for (int i = 0; i < size; i++) {
			value[i] = dataInput.readInt();
		}
	}

	@Override
	public int getId() {
		return INT_ARRAY;
	}

}
