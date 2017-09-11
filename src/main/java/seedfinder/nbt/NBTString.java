package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTString extends NBTBase {

	private String value;

	NBTString() {
	}

	public NBTString(String value) {
		this.value = value;
	}

	public String getString() {
		return value;
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		value = dataInput.readUTF();
	}

	@Override
	public int getId() {
		return STRING;
	}

}
