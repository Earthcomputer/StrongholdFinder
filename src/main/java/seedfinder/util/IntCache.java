package seedfinder.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches int arrays so they don't have to be re-allocated again later. Mainly
 * used by GenLayer. It caches "small" arrays (of size <= 256) and "large"
 * arrays.
 */
public class IntCache {

	// The size to allocate for large arrays
	private static int largeArraySize = 256;
	private static final List<int[]> freeSmallArrays = new ArrayList<>();
	private static final List<int[]> usedSmallArrays = new ArrayList<>();
	private static final List<int[]> freeLargeArrays = new ArrayList<>();
	private static final List<int[]> usedLargeArrays = new ArrayList<>();

	/**
	 * Gets an int array of at least the given size. The returned int array may
	 * be either newly created or re-used.
	 */
	public static synchronized int[] get(int size) {
		// If small array needed
		if (size <= 256) {
			if (freeSmallArrays.isEmpty()) {
				// create a new small array
				int[] arr = new int[256];
				usedSmallArrays.add(arr);
				return arr;
			} else {
				// re-use a free small array
				int[] arr = freeSmallArrays.remove(freeSmallArrays.size() - 1);
				usedSmallArrays.add(arr);
				return arr;
			}
		} else if (size > largeArraySize) {
			// Increase the size of a large array, since we need a larger one.
			// Forget about all the existing large arrays, they can be garbage
			// collected later.
			largeArraySize = size;
			freeLargeArrays.clear();
			usedLargeArrays.clear();
			int[] arr = new int[size];
			usedLargeArrays.add(arr);
			return arr;
		} else if (freeLargeArrays.isEmpty()) {
			// create a new large array
			int[] arr = new int[largeArraySize];
			usedLargeArrays.add(arr);
			return arr;
		} else {
			// re-use a free large array
			int[] arr = freeLargeArrays.remove(freeLargeArrays.size() - 1);
			usedLargeArrays.add(arr);
			return arr;
		}
	}

	/**
	 * Marks used int arrays as free so they can be re-used by the next call of
	 * {@link #get(int)}
	 */
	public static synchronized void free() {
		if (!freeLargeArrays.isEmpty()) {
			freeLargeArrays.remove(freeLargeArrays.size() - 1);
		}
		if (!freeSmallArrays.isEmpty()) {
			freeSmallArrays.remove(freeSmallArrays.size() - 1);
		}

		freeLargeArrays.addAll(usedLargeArrays);
		freeSmallArrays.addAll(usedSmallArrays);
		usedLargeArrays.clear();
		usedSmallArrays.clear();
	}

}
