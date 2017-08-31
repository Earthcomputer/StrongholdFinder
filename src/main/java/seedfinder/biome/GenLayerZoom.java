package seedfinder.biome;

import seedfinder.util.IntCache;

public class GenLayerZoom extends GenLayer {

	public GenLayerZoom(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		int parentX = x >> 1;
		int parentZ = z >> 1;
		int parentWidth = (width >> 1) + 2;
		int parentHeight = (height >> 1) + 2;
		int[] parentValues = parent.getValues(parentX, parentZ, parentWidth, parentHeight);

		int zoomedWidth = parentWidth - 1 << 1;
		int zoomedHeight = parentHeight - 1 << 1;
		int[] zoomedValues = IntCache.get(zoomedWidth * zoomedHeight);

		for (int dz = 0; dz < parentHeight - 1; dz++) {
			int index = (dz << 1) * zoomedWidth;
			int valueTopLeft = parentValues[dz * parentWidth];
			int valueBottomLeft = parentValues[(dz + 1) * parentWidth];

			for (int dx = 0; dx < parentWidth - 1; dx++) {
				initChunkSeed(dx + parentX << 1, dz + parentZ << 1);

				int valueTopRight = parentValues[dx + 1 + dz * parentWidth];
				int valueBottomRight = parentValues[dx + 1 + (dz + 1) * parentWidth];

				zoomedValues[index] = valueTopLeft;
				zoomedValues[index++ + zoomedWidth] = choose(valueTopLeft, valueBottomLeft);
				zoomedValues[index] = choose(valueTopLeft, valueTopRight);
				zoomedValues[index++ + zoomedWidth] = modeOrRandom(valueTopLeft, valueTopRight, valueBottomLeft,
						valueBottomRight);

				valueTopLeft = valueTopRight;
				valueBottomLeft = valueBottomRight;
			}
		}

		int[] values = IntCache.get(width * height);

		for (int i = 0; i < height; ++i) {
			System.arraycopy(zoomedValues, (i + (z & 1)) * zoomedWidth + (x & 1), values, i * width, width);
		}

		return values;
	}

	public static GenLayer magnify(long uniquifier, GenLayer parent, int times) {
		GenLayer layer = parent;

		for (int i = 0; i < times; i++) {
			layer = new GenLayerZoom(uniquifier + i, layer);
		}

		return layer;
	}

}
