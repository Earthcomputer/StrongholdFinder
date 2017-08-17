package seedfinder;

import java.util.Arrays;

/**
 * An expandable 2D area of ints. Automatically expands as new ints are added.
 */
public class Storage2D {

	private int minX;
	private int minY;
	private int width;
	private int height;
	/**
	 * Values are laid out across and then down. Therefore, the next value in
	 * the array after the value corresponding to (x, y) is likely to be the one
	 * corresponding to (x + 1, y); or, wrapping around to (minX, y + 1).
	 */
	private int[] values = new int[0];
	private final int _default;

	/**
	 * Constructs a new 2D area of ints with the given default value.
	 */
	public Storage2D(int _default) {
		this._default = _default;
	}

	/**
	 * Constructs a 2D area of ints, from a raw int array of the kind you would
	 * expect from GenLayer.
	 */
	public static Storage2D withData(int[] data, int width, int minX, int minY, int _default) {
		int height = data.length / width;
		if (width * height != data.length) {
			throw new IllegalArgumentException("width is not a factor of data.length");
		}

		Storage2D storage = new Storage2D(_default);
		storage.minX = minX;
		storage.minY = minY;
		storage.values = data;
		storage.width = width;
		storage.height = height;
		return storage;
	}

	/**
	 * Gets the value at (x, y), or the default value if no value has been set.
	 */
	public int get(int x, int y) {
		if (x < minX || y < minY) {
			return _default;
		}
		if (x >= minX + width || y >= minY + height) {
			return _default;
		}

		return values[x - minX + (y - minY) * width];
	}

	/**
	 * Sets the value at (x, y) to the given value. Expands the internal array
	 * if it currently doesn't cover the given coordinates.
	 */
	public void set(int x, int y, int value) {
		ensureAllocated(x, y);
		values[x - minX + (y - minY) * width] = value;
	}

	/**
	 * Fills the area bounded by the rectangle with opposite corners (minX,
	 * minY) and (maxX + 1, maxY + 1) with the given value.
	 */
	public void fill(int minX, int minY, int maxX, int maxY, int value) {
		ensureAllocated(minX, minY, maxX, maxY);

		int widthToFill = maxX - minX + 1;
		int index = minX - this.minX + (minY - this.minY) * width;
		for (int y = minY; y <= maxY; y++) {
			Arrays.fill(values, index, index + widthToFill, value);
			index += width;
		}
	}

	/**
	 * Translates all the values in this 2D area by the vector (dx, dy).
	 */
	public void moveAll(int dx, int dy) {
		minX += dx;
		minY += dy;
	}

	/**
	 * Sets all the values in this 2D area to the default value, without
	 * de-allocating.
	 */
	public void clear() {
		Arrays.fill(values, _default);
	}

	/**
	 * Sets all the values in this 2D area to the default value by
	 * de-allocating.
	 */
	public void erase() {
		values = new int[0];
		width = 0;
		height = 0;
	}

	/**
	 * Sets all the values in this 2D area to the default value by
	 * de-allocating, and then allocates the area bounded by the rectangle with
	 * opposite corners (minX, minY) and (maxX + 1, maxY + 1). Also doesn't
	 * reallocate if the area is the same size.
	 */
	public void eraseAndAllocate(int minX, int minY, int maxX, int maxY) {
		if (maxX < minX) {
			throw new IllegalArgumentException("maxX < minX");
		}
		if (maxY < minY) {
			throw new IllegalArgumentException("maxY < minY");
		}

		width = maxX - minX + 1;
		height = maxY - minY + 1;

		int newLength = width * height;
		// allocate a new array if the lengths don't match
		if (values.length != newLength) {
			values = new int[newLength];
		}
		// set all values to the default
		Arrays.fill(values, _default);

		this.minX = minX;
		this.minY = minY;
	}

	/**
	 * Ensures all the points in the area bounded by the rectangle with opposite
	 * corners (minX, minY) and (maxX + 1, maxY + 1) are allocated so that they
	 * do not need to be re-allocated during successive
	 * {@link #set(int, int, int)} calls.
	 */
	public void ensureAllocated(int minX, int minY, int maxX, int maxY) {
		if (minX > maxX) {
			throw new IllegalArgumentException("minX > maxX");
		}
		if (minY > maxY) {
			throw new IllegalArgumentException("minY > maxY");
		}

		if (width == 0) {
			// we don't have to care about pre-existing values.
			width = maxX - minX + 1;
			height = maxY - minY + 1;
			values = new int[width * height];
			Arrays.fill(values, _default);
			this.minX = minX;
			this.minY = minY;
		} else {
			// if both corners are allocated, then the area in-between must also
			// be allocated.
			ensureAllocated(minX, minY);
			ensureAllocated(maxX, maxY);
		}
	}

	/**
	 * Ensures the point (x, y) is allocated so it would not have to be
	 * re-allocated during a {@link #set(int, int, int)} call.
	 */
	public void ensureAllocated(int x, int y) {
		if (width == 0) {
			// we don't have to care about pre-existing values
			values = new int[] { _default };
			minX = x;
			minY = y;
			width = 1;
			height = 1;
			return;
		}

		// Check if out of bounds on the x-axis
		if (x < minX) {
			// We need to add extra columns on the left
			int colsToAdd = minX - x;
			int[] newValues = new int[(width + colsToAdd) * height];

			int newIndex = 0, index = 0;
			for (int row = 0; row < height; row++) {
				for (int i = 0; i < colsToAdd; i++) {
					newValues[newIndex++] = _default;
				}
				System.arraycopy(values, index, newValues, newIndex, width);

				index += width;
				newIndex += width;
			}

			minX = x;
			width += colsToAdd;
			values = newValues;
		} else if (x >= minX + width) {
			// We need to add extra columns on the right
			int colsToAdd = x - (minX + width) + 1;
			int[] newValues = new int[(width + colsToAdd) * height];

			int newIndex = 0, index = 0;
			for (int row = 0; row < height; row++) {
				System.arraycopy(values, index, newValues, newIndex, width);

				index += width;
				newIndex += width;

				for (int i = 0; i < colsToAdd; i++) {
					newValues[newIndex++] = _default;
				}
			}

			width += colsToAdd;
			values = newValues;
		}

		// Check if out of bounds on the y-axis
		if (y < minY) {
			// We need to add extra rows on the top
			int rowsToAdd = minY - y;
			int slotsToAdd = rowsToAdd * width;
			int[] newValues = new int[values.length + slotsToAdd];

			for (int i = 0; i < slotsToAdd; i++) {
				newValues[i] = _default;
			}
			System.arraycopy(values, 0, newValues, slotsToAdd, values.length);

			minY = y;
			height += rowsToAdd;
			values = newValues;
		} else if (y >= minY + height) {
			// We need to add extra rows on the bottom
			int rowsToAdd = y - (minY + height) + 1;
			int[] newValues = new int[width * (height + rowsToAdd)];

			System.arraycopy(values, 0, newValues, 0, values.length);
			for (int i = values.length; i < newValues.length; i++) {
				newValues[i] = _default;
			}

			height += rowsToAdd;
			values = newValues;
		}
	}

	/**
	 * Removes unnecessary rows and columns only holding the default value from
	 * the internal array
	 */
	public void prune() {
		int rowsOffTop = 0, rowsOffBottom = 0, colsOffLeft = 0, colsOffRight = 0;
		int index;
		int[] newValues;

		// Get rows to prune off top
		for (index = 0; index < values.length; index++) {
			if (values[index] != _default) {
				break;
			}
		}
		rowsOffTop = index / width;

		// Check if we can prune so much that we're erasing everything
		if (rowsOffTop == height) {
			erase();
			return;
		}

		// Get rows to prune off bottom
		for (index = values.length - 1; index >= 0; index--) {
			if (values[index] != _default) {
				break;
			}
		}
		rowsOffBottom = (values.length - (index + 1)) / width;

		// Prune rows
		if (rowsOffTop != 0 || rowsOffBottom != 0) {
			int newHeight = height - rowsOffTop - rowsOffBottom;
			newValues = new int[width * newHeight];

			System.arraycopy(values, rowsOffTop * width, newValues, 0, newValues.length);

			minY += rowsOffTop;
			height = newHeight;
			values = newValues;
		}

		// Get columns to prune off left
		index = 0;
		while (true) {
			if (values[index] != _default) {
				break;
			}
			index += width;
			if (index >= values.length) {
				index -= values.length - 1;
			}
		}
		colsOffLeft = index % width;

		// Get columns to prune off right
		index = width - 1;
		while (true) {
			if (values[index] != _default) {
				break;
			}
			index += width;
			if (index >= values.length) {
				index -= values.length + 1;
			}
		}
		colsOffRight = (values.length - (index + 1)) % width;

		// Prune columns
		if (colsOffLeft != 0 || colsOffRight != 0) {
			int newWidth = width - colsOffLeft - colsOffRight;
			newValues = new int[newWidth * height];

			for (int row = 0; row < height; row++) {
				System.arraycopy(values, colsOffLeft + row * width, newValues, row * newWidth, newWidth);
			}

			minX += colsOffLeft;
			width = newWidth;
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

		StringBuilder str = new StringBuilder((maxLen + 1) * strVals.length + height);
		for (int dy = 0; dy < height; dy++) {
			for (int dx = 0; dx < width; dx++) {
				String strVal = strVals[dx + dy * width];
				for (int i = strVal.length(); i <= maxLen; i++) {
					str.append(" ");
				}
				str.append(strVal);
			}
			str.append("\n");
		}
		return String.format("minX: %d, minY: %d\n%s", minX, minY, str);
	}

}
