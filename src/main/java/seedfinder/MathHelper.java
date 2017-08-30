package seedfinder;

import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * Helper math functions, mainly here to ensure exact equivalence with the
 * Minecraft world generator.
 */
public class MathHelper {

	/**
	 * The sin table stores values corresponding to the sin of all inputs
	 * <tt>n</tt> where <tt>0 <= n < 2pi</tt>. To compute the sin of <tt>n</tt>
	 * in this range, access this table at index <tt>65536n / 2pi</tt>.
	 */
	private static final float[] SIN_TABLE = new float[65536];

	static {
		for (int i = 0; i < 65536; i++) {
			SIN_TABLE[i] = (float) Math.sin(i * Math.PI * 2 / 65536);
		}
	}

	private MathHelper() {
	}

	/**
	 * Computes the floor of a double and returns it as a long
	 */
	public static long lfloor(double value) {
		long floor = (long) value;
		return value < floor ? floor - 1 : floor;
	}

	/**
	 * Computes the floor of a double and returns it as an int
	 */
	public static int floor(double value) {
		int floor = (int) value;
		return value < floor ? floor - 1 : floor;
	}

	/**
	 * Computes the floor of a float and returns it as an int
	 */
	public static int floor(float value) {
		int floor = (int) value;
		return value < floor ? floor - 1 : floor;
	}

	/**
	 * Interpolates between <tt>a</tt> and <tt>b</tt>, using the given
	 * <tt>slide</tt>, and clamps the result between <tt>a</tt> and </tt>b</tt>
	 */
	public static double clampedLerp(double a, double b, double slide) {
		if (slide < 0) {
			return a;
		} else if (slide > 1) {
			return b;
		} else {
			return a + (b - a) * slide;
		}
	}

	/**
	 * Looks up the sin of a value from a table
	 */
	public static float sin(float val) {
		return SIN_TABLE[(int) (val * 65536 / (2 * Math.PI)) & 65535];
	}

	/**
	 * Looks up the cos of a value from a table
	 */
	public static float cos(float val) {
		return SIN_TABLE[(int) (val * 65536 / (2 * Math.PI) + 65536 / 4) & 65535];
	}

	/**
	 * Returns a random value between a minimum and maximum, inclusive
	 */
	public static int randomRange(Random rand, int min, int max) {
		return min >= max ? min : rand.nextInt(max - min + 1) + min;
	}

	/**
	 * Returns a random value between minimum, inclusive, and maximum,
	 * exclusive.
	 */
	public static float randomRange(Random rand, float min, float max) {
		return min >= max ? min : rand.nextFloat() * (max - min) + min;
	}

	/**
	 * Returns a random element in the given list, weighted by each element's
	 * weight, according to the given weight extractor function.
	 */
	public static <T> T weightedRandom(Random rand, List<T> list, ToIntFunction<T> weightExtractor) {
		int weight = list.stream().mapToInt(weightExtractor).sum();
		if (weight <= 0) {
			return null;
		}
		weight = rand.nextInt(weight);
		for (int i = 0, e = list.size(); i < e; i++) {
			T t = list.get(i);
			weight -= weightExtractor.applyAsInt(t);
			if (weight < 0) {
				return t;
			}
		}
		return null;
	}

}
