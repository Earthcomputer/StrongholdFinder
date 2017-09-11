package seedfinder.nbt;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NBTList extends NBTBase implements List<NBTBase> {

	private int elementId;
	private ArrayList<NBTBase> delegate = new ArrayList<>();

	private static IllegalArgumentException invalidType() {
		return new IllegalArgumentException("Invalid tag type added");
	}

	public NBTCompound getCompound(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && elementId == COMPOUND ? (NBTCompound) nbt : new NBTCompound();
	}

	public NBTList getList(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && elementId == LIST ? (NBTList) nbt : new NBTList();
	}

	public byte getByte(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getByte() : 0;
	}

	public short getShort(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getShort() : 0;
	}

	public int getInt(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getInt() : 0;
	}

	public long getLong(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getLong() : 0;
	}

	public float getFloat(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getFloat() : 0;
	}

	public double getDouble(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && nbt.isNumber() ? ((NBTNumber) nbt).getDouble() : 0;
	}

	public String getString(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && elementId == STRING ? ((NBTString) nbt).getString() : "";
	}

	public byte[] getByteArray(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && elementId == BYTE_ARRAY ? ((NBTByteArray) nbt).getByteArray() : new byte[0];
	}

	public int[] getIntArray(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && elementId == INT_ARRAY ? ((NBTIntArray) nbt).getIntArray() : new int[0];
	}

	public long[] getLongArray(int index) {
		NBTBase nbt = getNoBounds(index);
		return nbt != null && elementId == LONG_ARRAY ? ((NBTLongArray) nbt).getLongArray() : new long[0];
	}

	private NBTBase getNoBounds(int index) {
		return index < 0 || index >= size() ? null : get(index);
	}

	public void forEachCompound(Consumer<? super NBTCompound> action) {
		if (elementId == COMPOUND) {
			forEach(nbt -> action.accept((NBTCompound) nbt));
		} else {
			forEachDummy(action, NBTCompound::new);
		}
	}

	public void forEachList(Consumer<? super NBTList> action) {
		if (elementId == LIST) {
			forEach(nbt -> action.accept((NBTList) nbt));
		} else {
			forEachDummy(action, NBTList::new);
		}
	}

	public void forEachInt(IntConsumer action) {
		if (!isEmpty() && get(0).isNumber()) {
			forEach(nbt -> action.accept(((NBTNumber) nbt).getInt()));
		} else {
			for (int i = 0, e = size(); i < e; i++) {
				action.accept(0);
			}
		}
	}

	public void forEachLong(LongConsumer action) {
		if (!isEmpty() && get(0).isNumber()) {
			forEach(nbt -> action.accept(((NBTNumber) nbt).getLong()));
		} else {
			for (int i = 0, e = size(); i < e; i++) {
				action.accept(0);
			}
		}
	}

	public void forEachDouble(DoubleConsumer action) {
		if (!isEmpty() && get(0).isNumber()) {
			forEach(nbt -> action.accept(((NBTNumber) nbt).getDouble()));
		} else {
			for (int i = 0, e = size(); i < e; i++) {
				action.accept(0);
			}
		}
	}

	public void forEachString(Consumer<? super String> action) {
		if (elementId == STRING) {
			forEach(nbt -> action.accept(((NBTString) nbt).getString()));
		} else {
			forEachDummy(action, () -> "");
		}
	}

	public void forEachByteArray(Consumer<byte[]> action) {
		if (elementId == BYTE_ARRAY) {
			forEach(nbt -> action.accept(((NBTByteArray) nbt).getByteArray()));
		} else {
			byte[] emptyByteArray = new byte[0];
			forEachDummy(action, () -> emptyByteArray);
		}
	}

	public void forEachIntArray(Consumer<int[]> action) {
		if (elementId == INT_ARRAY) {
			forEach(nbt -> action.accept(((NBTIntArray) nbt).getIntArray()));
		} else {
			int[] emptyIntArray = new int[0];
			forEachDummy(action, () -> emptyIntArray);
		}
	}

	public void forEachLongArray(Consumer<long[]> action) {
		if (elementId == LONG_ARRAY) {
			forEach(nbt -> action.accept(((NBTLongArray) nbt).getLongArray()));
		} else {
			long[] emptyLongArray = new long[0];
			forEachDummy(action, () -> emptyLongArray);
		}
	}

	private <T> void forEachDummy(Consumer<? super T> action, Supplier<? extends T> supplier) {
		for (int i = 0, e = size(); i < e; i++) {
			action.accept(supplier.get());
		}
	}

	@Override
	public void forEach(Consumer<? super NBTBase> action) {
		delegate.forEach(action);
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public Iterator<NBTBase> iterator() {
		return delegate.iterator();
	}

	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}

	@Override
	public boolean add(NBTBase e) {
		if (isEmpty()) {
			elementId = e.getId();
		} else if (e.getId() != elementId) {
			throw invalidType();
		}
		return delegate.add(e);

	}

	@Override
	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends NBTBase> c) {
		if (!c.isEmpty()) {
			if (isEmpty()) {
				elementId = c.iterator().next().getId();
			}
			if (c.stream().anyMatch(it -> it.getId() != elementId)) {
				throw invalidType();
			}
		}
		return delegate.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends NBTBase> c) {
		if (!c.isEmpty()) {
			if (isEmpty()) {
				elementId = c.iterator().next().getId();
			}
			if (c.stream().anyMatch(it -> it.getId() != elementId)) {
				throw invalidType();
			}
		}
		return delegate.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}

	@Override
	public void replaceAll(UnaryOperator<NBTBase> operator) {
		if (!isEmpty()) {
			ArrayList<NBTBase> newDelegate = stream().map(operator).collect(Collectors.toCollection(ArrayList::new));
			int newElementId = newDelegate.get(0).getId();
			if (newDelegate.stream().anyMatch(it -> it.getId() != newElementId)) {
				throw invalidType();
			}
			delegate = newDelegate;
			elementId = newElementId;
		}
	}

	@Override
	public boolean removeIf(Predicate<? super NBTBase> filter) {
		return delegate.removeIf(filter);
	}

	@Override
	public void sort(Comparator<? super NBTBase> c) {
		delegate.sort(c);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public NBTBase get(int index) {
		return delegate.get(index);
	}

	@Override
	public NBTBase set(int index, NBTBase element) {
		if (!isEmpty() && element.getId() != elementId) {
			throw invalidType();
		}
		return delegate.set(index, element);
	}

	@Override
	public void add(int index, NBTBase element) {
		if (!isEmpty() && element.getId() != elementId) {
			throw invalidType();
		}
		delegate.add(index, element);
	}

	@Override
	public Stream<NBTBase> stream() {
		return delegate.stream();
	}

	@Override
	public NBTBase remove(int index) {
		return delegate.remove(index);
	}

	@Override
	public Stream<NBTBase> parallelStream() {
		return delegate.parallelStream();
	}

	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	@Override
	public ListIterator<NBTBase> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<NBTBase> listIterator(int index) {
		return new ListIterator<NBTBase>() {
			private int idx = index;
			private int lastRet = -1;

			@Override
			public boolean hasNext() {
				return idx < size();
			}

			@Override
			public NBTBase next() {
				try {
					NBTBase next = get(idx);
					lastRet = idx;
					idx++;
					return next;
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}

			@Override
			public boolean hasPrevious() {
				return idx > 0;
			}

			@Override
			public NBTBase previous() {
				try {
					int i = idx - 1;
					NBTBase prev = get(i);
					lastRet = i;
					idx = i;
					return prev;
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}

			@Override
			public int nextIndex() {
				return idx;
			}

			@Override
			public int previousIndex() {
				return idx - 1;
			}

			@Override
			public void remove() {
				if (lastRet == -1) {
					throw new IllegalStateException();
				}

				try {
					NBTList.this.remove(lastRet);
					if (lastRet < idx) {
						idx--;
					}
					lastRet = -1;
				} catch (IndexOutOfBoundsException e) {
					throw new ConcurrentModificationException();
				}
			}

			@Override
			public void set(NBTBase e) {
				if (lastRet == -1) {
					throw new IllegalStateException();
				}

				try {
					NBTList.this.set(lastRet, e);
				} catch (IndexOutOfBoundsException ex) {
					throw new ConcurrentModificationException();
				}
			}

			@Override
			public void add(NBTBase e) {
				try {
					NBTList.this.add(idx, e);
					lastRet = -1;
					idx++;
				} catch (IndexOutOfBoundsException ex) {
					throw new ConcurrentModificationException();
				}
			}
		};
	}

	@Override
	public List<NBTBase> subList(int fromIndex, int toIndex) {
		return new ArrayList<>(this).subList(fromIndex, toIndex);
	}

	@Override
	public void read(DataInputStream dataInput) throws IOException {
		int id = dataInput.readByte();
		int size = dataInput.readInt();

		clear();
		delegate.ensureCapacity(size);

		for (int i = 0; i < size; i++) {
			NBTBase tag = createById(id);
			tag.read(dataInput);
			add(tag);
		}
	}

	@Override
	public int getId() {
		return LIST;
	}

}
