package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NBTCompound extends NBTBase implements Map<String, NBTBase> {

	private Map<String, NBTBase> delegate = new HashMap<>();

	public NBTCompound getCompound(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.getId() == COMPOUND ? (NBTCompound) nbt : new NBTCompound();
	}

	public NBTList getList(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.getId() == LIST ? (NBTList) nbt : new NBTList();
	}

	public byte getByte(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getByte() : 0;
	}

	public short getShort(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getShort() : 0;
	}

	public int getInt(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getInt() : 0;
	}

	public long getLong(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getLong() : 0;
	}

	public float getFloat(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getFloat() : 0;
	}

	public double getDouble(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getDouble() : 0;
	}

	public String getString(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.getId() == STRING ? ((NBTString) nbt).getString() : "";
	}

	public byte[] getByteArray(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.getId() == BYTE_ARRAY ? ((NBTByteArray) nbt).getByteArray() : new byte[0];
	}

	public int[] getIntArray(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.getId() == INT_ARRAY ? ((NBTIntArray) nbt).getIntArray() : new int[0];
	}

	public long[] getLongArray(String key) {
		NBTBase nbt = get(key);
		return nbt != null && nbt.getId() == LONG_ARRAY ? ((NBTLongArray) nbt).getLongArray() : new long[0];
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public NBTBase compute(String key,
			BiFunction<? super String, ? super NBTBase, ? extends NBTBase> remappingFunction) {
		return delegate.compute(key, remappingFunction);
	}

	@Override
	public NBTBase computeIfAbsent(String key, Function<? super String, ? extends NBTBase> mappingFunction) {
		return delegate.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public NBTBase computeIfPresent(String key,
			BiFunction<? super String, ? super NBTBase, ? extends NBTBase> remappingFunction) {
		return delegate.computeIfPresent(key, remappingFunction);
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public Set<Entry<String, NBTBase>> entrySet() {
		return delegate.entrySet();
	}

	@Override
	public boolean equals(Object other) {
		return delegate.equals(other);
	}

	@Override
	public void forEach(BiConsumer<? super String, ? super NBTBase> block) {
		delegate.forEach(block);
	}

	@Override
	public NBTBase get(Object key) {
		return delegate.get(key);
	}

	@Override
	public NBTBase getOrDefault(Object key, NBTBase _default) {
		return delegate.getOrDefault(key, _default);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return delegate.keySet();
	}

	@Override
	public NBTBase merge(String key, NBTBase value,
			BiFunction<? super NBTBase, ? super NBTBase, ? extends NBTBase> remappingFunction) {
		return delegate.merge(key, value, remappingFunction);
	}

	@Override
	public NBTBase put(String key, NBTBase value) {
		return delegate.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends NBTBase> map) {
		delegate.putAll(map);
	}

	@Override
	public NBTBase putIfAbsent(String key, NBTBase value) {
		return delegate.putIfAbsent(key, value);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return delegate.remove(key, value);
	}

	@Override
	public NBTBase remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public boolean replace(String key, NBTBase oldValue, NBTBase newValue) {
		return delegate.replace(key, oldValue, newValue);
	}

	@Override
	public NBTBase replace(String key, NBTBase newValue) {
		return delegate.replace(key, newValue);
	}

	@Override
	public void replaceAll(BiFunction<? super String, ? super NBTBase, ? extends NBTBase> function) {
		delegate.replaceAll(function);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public Collection<NBTBase> values() {
		return delegate.values();
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		clear();

		byte id;
		while ((id = dataInput.readByte()) != 0) {
			String key = dataInput.readUTF();
			NBTBase value = NBTBase.createById(id);
			value.read(dataInput);
			put(key, value);
		}
	}

	@Override
	public int getId() {
		return COMPOUND;
	}

}
