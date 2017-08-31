package seedfinder.util;

import java.util.Arrays;

/**
 * An expandable 3D volume of ints. Automatically expands as new ints are added.
 */
public class Storage3D {

	private int minX;
	private int minY;
	private int minZ;
	private int xSize;
	private int ySize;
	private int zSize;
	/**
	 * Values are laid out in the x-direction, then the y-direction, then the
	 * z-direction. Therefore, the next value in the array after the value
	 * corresponding to (x, y, z) is most likely (x + 1, y, z); or, wrapping
	 * around to (minX, y + 1, z); or, wrapping around to (minX, minY, z + 1).
	 */
	private int[] values = new int[0];
	private final int _default;

	/**
	 * Creates a new 3D volume of ints with the given default value.
	 */
	public Storage3D(int _default) {
		this._default = _default;
	}

	/**
	 * Gets the value at (x, y, z), or the default value if no value has been
	 * set
	 */
	public int get(int x, int y, int z) {
		if (x < minX || y < minY || z < minZ) {
			return _default;
		}
		if (x >= minX + xSize || y >= minY + ySize || z >= minZ + zSize) {
			return _default;
		}

		return values[x - minX + (y - minY + (z - minZ) * ySize) * xSize];
	}

	/**
	 * Sets the value at (x, y, z) to the given value. Expands the internal
	 * array if it currently doesn't cover the given coordinates.
	 */
	public void set(int x, int y, int z, int value) {
		ensureAllocated(x, y, z);
		values[x - minX + (y - minY + (z - minZ) * ySize) * xSize] = value;
	}

	/**
	 * Fills the area bounded by the cuboid with opposite corners (minX, minY,
	 * minZ) and (maxX + 1, maxY + 1, maxZ + 1) with the given value.
	 */
	public void fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int value) {
		ensureAllocated(minX, minY, minZ, maxX, maxY, maxZ);

		int xSizeToFill = maxX - minX + 1;
		for (int y = minY; y <= maxY; y++) {
			for (int z = minZ; z <= maxZ; z++) {
				int index = minX - this.minX + (y - this.minY + (z - this.minZ) * ySize) * xSize;
				Arrays.fill(values, index, index + xSizeToFill, value);
			}
		}
	}

	/**
	 * Translates all the values in this 3D volume by the vector (dx, dy, dz)
	 */
	public void moveAll(int dx, int dy, int dz) {
		minX += dx;
		minY += dy;
		minZ += dz;
	}

	/**
	 * Sets all the values in this 3D volume to the default value, without
	 * de-allocating.
	 */
	public void clear() {
		Arrays.fill(values, _default);
	}

	/**
	 * Sets all the values in this 3D volume to the default value by
	 * de-allocating.
	 */
	public void erase() {
		values = new int[0];
		xSize = 0;
		ySize = 0;
		zSize = 0;
	}

	/**
	 * Sets all the values in this 3D volume to the default value by
	 * de-allocating, and then allocates the area bounded by the cuboid with
	 * opposite corners (minX, minY, minZ) and (maxX + 1, maxY + 1, maxZ + 1).
	 * Also doesn't reallocate if the volume is the same size.
	 */
	public void eraseAndAllocate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (maxX < minX) {
			throw new IllegalArgumentException("maxX < minX");
		}
		if (maxY < minY) {
			throw new IllegalArgumentException("maxY < minY");
		}
		if (maxZ < minZ) {
			throw new IllegalArgumentException("maxZ < minZ");
		}

		xSize = maxX - minX + 1;
		ySize = maxY - minY + 1;
		zSize = maxZ - minZ + 1;

		int newLength = xSize * ySize * zSize;
		// allocate a new array if the lengths don't match
		if (values.length != newLength) {
			values = new int[newLength];
		}
		// set all the values to the default
		Arrays.fill(values, _default);

		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
	}

	/**
	 * Sets all the values in this 3D volume to the default value without
	 * de-allocating, then moves all the allocated volume to the specified
	 * volume, expanding the internal array if it is too small.
	 */
	public void reallocate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (maxX < minX) {
			throw new IllegalArgumentException("maxX < minX");
		}
		if (maxY < minY) {
			throw new IllegalArgumentException("maxY < minY");
		}
		if (maxZ < minZ) {
			throw new IllegalArgumentException("maxZ < minZ");
		}

		xSize = maxX - minX + 1;
		ySize = maxY - minY + 1;
		zSize = maxZ - minZ + 1;

		int minLength = xSize * ySize * zSize;
		// allocate a new array if the current length is too short
		if (values.length < minLength) {
			values = new int[minLength];
		}
		// set all values to the default
		Arrays.fill(values, 0, minLength, _default);

		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
	}

	/**
	 * Ensures all the points in the volume bounded by the cuboid with opposite
	 * corners (minX, minY, minZ) and (maxX + 1, maxY + 1, maxZ + 1) are
	 * allocated so that they do not need to be reallocated during successive
	 * {@link #set(int, int, int, int)} calls.
	 */
	public void ensureAllocated(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (minX > maxX) {
			throw new IllegalArgumentException("minX > maxX");
		}
		if (minY > maxY) {
			throw new IllegalArgumentException("minY > maxY");
		}
		if (minZ > maxZ) {
			throw new IllegalArgumentException("minZ > maxZ");
		}

		if (xSize == 0) {
			// we don't have to care about pre-existing values
			xSize = maxX - minX + 1;
			ySize = maxY - minY + 1;
			zSize = maxZ - minZ + 1;
			int minLength = xSize * ySize * zSize;
			if (values.length < minLength) {
				values = new int[minLength];
			}
			Arrays.fill(values, 0, minLength, _default);
			this.minX = minX;
			this.minY = minY;
			this.minZ = minZ;
		} else {
			// if both corners are allocated, then the volume in-between must
			// also be allocated.
			ensureAllocated(minX, minY, minZ);
			ensureAllocated(maxX, maxY, maxZ);
		}
	}

	/**
	 * Ensures the point (x, y, z) is allocated so it would not have to be
	 * reallocated during a {@link #set(int, int, int, int)} call.
	 */
	public void ensureAllocated(int x, int y, int z) {
		if (xSize == 0) {
			// we don't have to care about pre-existing values
			minX = x;
			minY = y;
			minZ = z;
			if (values.length == 0) {
				values = new int[1];
			}
			values[0] = _default;
			xSize = 1;
			ySize = 1;
			zSize = 1;
			return;
		}

		// Check if out of bounds on the x-axis
		if (x < minX) {
			// We need to add extra x-oriented planes on the left
			int linesToAdd = minX - x;
			int[] newValues = new int[(xSize + linesToAdd) * ySize * zSize];

			int newIndex = 0, index = 0;
			for (int i = 0, e = ySize * zSize; i < e; i++) {
				for (int j = 0; j < linesToAdd; j++) {
					newValues[newIndex++] = _default;
				}
				System.arraycopy(values, index, newValues, newIndex, xSize);

				index += xSize;
				newIndex += xSize;
			}

			minX = x;
			xSize += linesToAdd;
			values = newValues;
		} else if (x >= minX + xSize) {
			// We need to add extra x-oriented planes on the right
			int linesToAdd = x - (minX + xSize) + 1;
			int[] newValues = new int[(xSize + linesToAdd) * ySize * zSize];

			int newIndex = 0, index = 0;
			for (int i = 0, e = ySize * zSize; i < e; i++) {
				System.arraycopy(values, index, newValues, newIndex, xSize);

				index += xSize;
				newIndex += xSize;

				for (int j = 0; j < linesToAdd; j++) {
					newValues[newIndex++] = _default;
				}
			}

			xSize += linesToAdd;
			values = newValues;
		}

		// Check if out of bounds on the y-axis
		if (y < minY) {
			// We need to add extra y-oriented planes on the bottom
			int linesToAdd = minY - y;
			int xTimesY = xSize * ySize;
			int[] newValues = new int[xSize * (ySize + linesToAdd) * zSize];

			int newIndex = 0, index = 0;
			for (int i = 0; i < zSize; i++) {
				for (int j = 0, e = linesToAdd * xSize; j < e; j++) {
					newValues[newIndex++] = _default;
				}
				System.arraycopy(values, index, newValues, newIndex, xTimesY);

				index += xTimesY;
				newIndex += xTimesY;
			}

			minY = y;
			ySize += linesToAdd;
			values = newValues;
		} else if (y >= minY + ySize) {
			// We need to add extra y-oriented planes on the top
			int linesToAdd = y - (minY + ySize) + 1;
			int xTimesY = xSize * ySize;
			int[] newValues = new int[xSize * (ySize + linesToAdd) * zSize];

			int newIndex = 0, index = 0;
			for (int i = 0; i < zSize; i++) {
				System.arraycopy(values, index, newValues, newIndex, xTimesY);

				index += xTimesY;
				newIndex += xTimesY;

				for (int j = 0, e = linesToAdd * xSize; j < e; j++) {
					newValues[newIndex++] = _default;
				}
			}

			ySize += linesToAdd;
			values = newValues;
		}

		// Check if out of bounds on the z-axis
		if (z < minZ) {
			// We need to add extra z-oriented planes on the front
			int linesToAdd = minZ - z;
			int slotsToAdd = linesToAdd * xSize * ySize;
			int oldLength = xSize * ySize * zSize;
			int[] newValues = new int[oldLength + slotsToAdd];

			for (int i = 0; i < slotsToAdd; i++) {
				newValues[i] = _default;
			}
			System.arraycopy(values, 0, newValues, slotsToAdd, oldLength);

			minZ = z;
			zSize += linesToAdd;
			values = newValues;
		} else if (z >= minZ + zSize) {
			// We need to add extra z-oriented planes on the back
			int linesToAdd = z - (minZ + zSize) + 1;
			int[] newValues = new int[xSize * ySize * (zSize + linesToAdd)];
			int oldLength = xSize * ySize * zSize;

			System.arraycopy(values, 0, newValues, 0, oldLength);
			for (int i = oldLength; i < newValues.length; i++) {
				newValues[i] = _default;
			}

			zSize += linesToAdd;
			values = newValues;
		}
	}

	/**
	 * Removes unnecessary planes on the outside of the allocated volume only
	 * holding the default value from the internal array.
	 */
	public void prune() {
		int linesOffLowX = 0, linesOffHighX = 0, linesOffLowY = 0, linesOffHighY = 0, linesOffLowZ = 0,
				linesOffHighZ = 0;
		int xTimesY = xSize * ySize;
		int index;
		int[] newValues;
		// Remove any extra allocation inside values
		if (values.length != xTimesY * zSize) {
			newValues = new int[xTimesY * zSize];
			System.arraycopy(values, 0, newValues, 0, newValues.length);
			values = newValues;
		}
		/*
		 * For the rest of this method we can assume that values.length == xSize
		 * * ySize * zSize
		 */

		// Get planes to prune off front
		for (index = 0; index < values.length; index++) {
			if (values[index] != _default) {
				break;
			}
		}
		linesOffLowZ = index / xTimesY;

		// Check if we can prune so much that we're erasing everything
		if (linesOffLowZ == zSize) {
			erase();
			return;
		}

		// Get planes to prune off back
		for (index = values.length - 1; index >= 0; index--) {
			if (values[index] != _default) {
				break;
			}
		}
		linesOffHighZ = (values.length - (index + 1)) / xTimesY;

		// Prune z-oriented planes
		if (linesOffLowZ != 0 || linesOffHighZ != 0) {
			int newZSize = zSize - linesOffLowZ - linesOffHighZ;
			newValues = new int[xTimesY * newZSize];

			System.arraycopy(values, linesOffLowZ * xTimesY, newValues, 0, newValues.length);

			minZ += linesOffLowZ;
			zSize = newZSize;
			values = newValues;
		}

		// Get planes to prune off bottom
		index = 0;
		outside: while (true) {
			for (int i = 0; i < xSize; i++) {
				if (values[index++] != _default) {
					break outside;
				}
			}
			index += xTimesY - xSize;
			if (index >= values.length) {
				linesOffLowY++;
				index -= values.length - xSize;
			}
		}

		// Get planes to prune off top
		index = xTimesY - xSize;
		outside: while (true) {
			for (int i = 0; i < xSize; i++) {
				if (values[index++] != _default) {
					break outside;
				}
			}
			index += xTimesY - xSize;
			if (index >= values.length) {
				linesOffHighY++;
				index -= values.length + xSize;
			}
		}

		// Prune y-oriented planes
		if (linesOffLowY != 0 || linesOffHighY != 0) {
			int newYSize = ySize - linesOffLowY - linesOffHighY;
			int newXTimesY = xSize * newYSize;
			newValues = new int[newXTimesY * zSize];

			int oldIndex = linesOffLowY * xSize;
			for (int i = 0; i < zSize; i++) {
				System.arraycopy(values, oldIndex, newValues, i * newXTimesY, newXTimesY);
				oldIndex += xTimesY;
			}

			minY += linesOffLowY;
			ySize = newYSize;
			values = newValues;
		}

		// Get planes to prune off left
		index = 0;
		outside: while (true) {
			for (; index < values.length; index += xSize) {
				if (values[index] != _default) {
					break outside;
				}
			}
			index -= values.length - 1;
			linesOffLowX++;
		}

		// Get planes to prune off right
		index = xSize - 1;
		outside: while (true) {
			for (; index < values.length; index += xSize) {
				if (values[index] != _default) {
					break outside;
				}
			}
			index -= values.length + 1;
			linesOffHighX++;
		}

		// Prune x-oriented planes
		if (linesOffLowX != 0 || linesOffHighX != 0) {
			int newXSize = xSize - linesOffLowX - linesOffHighX;
			newValues = new int[newXSize * ySize * zSize];

			int oldIndex = linesOffLowX;
			for (int i = 0, e = ySize * zSize; i < e; i++) {
				System.arraycopy(values, oldIndex, newValues, i * newXSize, newXSize);
				oldIndex += xSize;
			}

			minX += linesOffLowX;
			xSize = newXSize;
			values = newValues;
		}
	}

	@Override
	public String toString() {
		if (values.length == 0) {
			return "[empty]";
		}

		String[] strVals = Arrays.stream(values).mapToObj(Integer::toString).toArray(String[]::new);
		int maxLen = Arrays.stream(strVals).mapToInt(String::length).max().getAsInt();

		StringBuilder str = new StringBuilder((maxLen + 1) * strVals.length + (ySize + 1) * zSize);

		int index = 0;
		for (int dz = 0; dz < zSize; dz++) {
			for (int dy = 0; dy < ySize; dy++) {
				for (int dx = 0; dx < xSize; dx++) {
					String strVal = strVals[index++];
					for (int i = strVal.length(); i <= maxLen; i++) {
						str.append(" ");
					}
					str.append(strVal);
				}
				str.append("\n");
			}
			str.append("\n");
		}

		return String.format("minX: %d, minY: %d, minZ: %d\n\n%s", minX, minY, minZ, str);
	}

}
