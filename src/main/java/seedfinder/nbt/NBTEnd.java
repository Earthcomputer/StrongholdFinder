package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTEnd extends NBTBase {

	@Override
	public void read(DataInputStream dataInput) throws IOException {
	}

	@Override
	public int getId() {
		return END;
	}

}
