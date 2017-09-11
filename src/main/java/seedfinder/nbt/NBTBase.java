package seedfinder.nbt;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public abstract class NBTBase {

	public static final int END = 0;
	public static final int BYTE = 1;
	public static final int SHORT = 2;
	public static final int INT = 3;
	public static final int LONG = 4;
	public static final int FLOAT = 5;
	public static final int DOUBLE = 6;
	public static final int BYTE_ARRAY = 7;
	public static final int STRING = 8;
	public static final int LIST = 9;
	public static final int COMPOUND = 10;
	public static final int INT_ARRAY = 11;
	public static final int LONG_ARRAY = 12;

	public static NBTBase createById(int id) {
		switch (id) {
		case END:
			return new NBTEnd();
		case BYTE:
			return new NBTByte();
		case SHORT:
			return new NBTShort();
		case INT:
			return new NBTInt();
		case LONG:
			return new NBTLong();
		case FLOAT:
			return new NBTFloat();
		case DOUBLE:
			return new NBTDouble();
		case BYTE_ARRAY:
			return new NBTByteArray();
		case STRING:
			return new NBTString();
		case LIST:
			return new NBTList();
		case COMPOUND:
			return new NBTCompound();
		case INT_ARRAY:
			return new NBTIntArray();
		case LONG_ARRAY:
			return new NBTLongArray();
		default:
			return null;
		}
	}

	public static NBTCompound readCompressed(InputStream input) throws IOException {
		return read(new BufferedInputStream(new GZIPInputStream(input)));
	}

	public static NBTCompound read(InputStream input) throws IOException {
		DataInputStream dataInput = new DataInputStream(input);

		byte id = dataInput.readByte();
		if (id != COMPOUND) {
			throw new IOException("Root not a compound tag");
		}

		dataInput.readUTF();

		NBTCompound tag = (NBTCompound) createById(id);

		try {
			tag.read(dataInput);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException("An exception occurred while reading NBT", e);
		}

		return tag;
	}

	public abstract void read(DataInputStream dataInput) throws IOException;

	public abstract int getId();

	public boolean isNumber() {
		return false;
	}

}
