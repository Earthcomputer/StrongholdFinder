package seedfinder.loot;

import java.util.Random;

import seedfinder.util.MathHelper;

public class RandomValueRange {

	private int min;
	private int max;

	public RandomValueRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public RandomValueRange(int value) {
		this(value, value);
	}

	public int nextInt(Random rand) {
		return MathHelper.randomRange(rand, min, max);
	}

	public float nextFloat(Random rand) {
		return MathHelper.randomRange(rand, (float) min, (float) max);
	}

}
