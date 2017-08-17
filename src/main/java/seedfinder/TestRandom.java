package seedfinder;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A test class to ensure that we're getting the same random calls as Minecraft
 */
public class TestRandom extends Random {

	private static final long serialVersionUID = 7000147630210182013L;

	private boolean debugActive = false;

	public TestRandom() {
		super();
	}

	public TestRandom(long seed) {
		super(seed);
	}

	@Override
	public int nextInt() {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextInt()");
		return super.nextInt();
	}

	@Override
	public int nextInt(int bound) {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextInt(" + bound + ")");
		return super.nextInt(bound);
	}

	@Override
	public long nextLong() {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextLong()");
		return super.nextLong();
	}

	@Override
	public boolean nextBoolean() {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextBoolean()");
		return super.nextBoolean();
	}

	@Override
	public float nextFloat() {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextFloat()");
		return super.nextFloat();
	}

	@Override
	public double nextDouble() {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextDouble()");
		return super.nextDouble();
	}

	@Override
	public synchronized double nextGaussian() {
		if (debugActive)
			System.out.println(getCaller() + " Random.nextGaussian()");
		return super.nextGaussian();
	}

	@Override
	public void setSeed(long seed) {
		debugActive = true;
		System.out.println(getCaller() + " Random.setSeed(" + seed + ")");
		super.setSeed(seed);
	}

	private String getCaller() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

		// Reverse the stack trace array
		for (int i = 0; i < stackTrace.length / 2; i++) {
			StackTraceElement tmp = stackTrace[i];
			stackTrace[i] = stackTrace[stackTrace.length - i - 1];
			stackTrace[stackTrace.length - i - 1] = tmp;
		}

		return Arrays.stream(stackTrace).limit(stackTrace.length - 3).map(String::valueOf)
				.collect(Collectors.joining(" ||| "));
	}

}
