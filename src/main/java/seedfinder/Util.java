package seedfinder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

// Debug class
public class Util {

	public static long getSeed(Random rand) {
		try {
			Field field = Random.class.getDeclaredField("seed");
			field.setAccessible(true);
			return ((AtomicLong) field.get(rand)).get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void printSummary(int[] values) {
		System.out.println(
				values.length + "/" + Arrays.stream(values).sum() + "/" + Arrays.stream(values).max().getAsInt() + "/"
						+ Arrays.stream(values).min().getAsInt() + "/" + Arrays.hashCode(values));
	}

	public static void printValues(int[] values, int width, int height) {
		int max = Arrays.stream(values).max().getAsInt();
		max = Math.max(width, max);
		max = Math.max(height, max);
		int digits = max < 16 ? 1 : (int) Math.ceil(Math.log(max) / Math.log(16));

		for (int i = 0; i <= digits; i++) {
			System.out.print(" ");
		}
		for (int dx = 0; dx < width; dx++) {
			System.out.printf(" %0" + digits + "x", dx);
		}
		System.out.println();
		for (int dz = 0; dz < height; dz++) {
			System.out.printf(" %0" + digits + "x", dz);
			for (int dx = 0; dx < width; dx++) {
				System.out.printf(" %0" + digits + "x", values[dx + dz * width]);
			}
			System.out.println();
		}
	}

}
