package seedfinder.biome;

import seedfinder.IntCache;

public class GenLayerVoronoiZoom extends GenLayer {

	public GenLayerVoronoiZoom(long uniquifier, GenLayer parent) {
		super(uniquifier);
		this.parent = parent;
	}

	@Override
	public int[] getValues(int x, int z, int width, int height) {
		x = x - 2;
		z = z - 2;

		int parentX = x >> 2;
		int parentZ = z >> 2;
		int parentWidth = (width >> 2) + 2;
		int parentHeight = (height >> 2) + 2;
		int[] parentValues = parent.getValues(parentX, parentZ, parentWidth, parentHeight);

		int zoomedWidth = parentWidth - 1 << 2;
		int zoomedHeight = parentHeight - 1 << 2;
		int[] zoomedValues = IntCache.get(zoomedWidth * zoomedHeight);

		for (int dz = 0; dz < parentHeight - 1; dz++) {
			int valueTopLeft = parentValues[dz * parentWidth];
			int valueBottomLeft = parentValues[(dz + 1) * parentWidth];

			for (int dx = 0; dx < parentWidth - 1; dx++) {
				final double range = 3.6;
				initChunkSeed(dx + parentX << 2, dz + parentZ << 2);
				double topLeftX = (nextInt(1024) / 1024.0 - 0.5) * range;
				double topLeftZ = (nextInt(1024) / 1024.0 - 0.5) * range;
				initChunkSeed(dx + parentX + 1 << 2, dz + parentZ << 2);
				double topRightX = (nextInt(1024) / 1024.0 - 0.5) * range + 4.0;
				double topRightZ = (nextInt(1024) / 1024.0 - 0.5) * range;
				initChunkSeed(dx + parentX << 2, dz + parentZ + 1 << 2);
				double bottomLeftX = (nextInt(1024) / 1024.0 - 0.5) * range;
				double bottomLeftZ = (nextInt(1024) / 1024.0 - 0.5) * range + 4.0;
				initChunkSeed(dx + parentX + 1 << 2, dz + parentZ + 1 << 2);
				double bottomRightX = (nextInt(1024) / 1024.0 - 0.5) * range + 4.0;
				double bottomRightZ = (nextInt(1024) / 1024.0 - 0.5) * range + 4.0;

				int valueTopRight = parentValues[dx + 1 + dz * parentWidth] & 255;
				int valueBottomRight = parentValues[dx + 1 + (dz + 1) * parentWidth] & 255;

				for (int ddz = 0; ddz < 4; ddz++) {
					int zoomedIndex = ((dz << 2) + ddz) * zoomedWidth + (dx << 2);

					for (int ddx = 0; ddx < 4; ddx++) {
						double distSqTopLeft = (ddz - topLeftZ) * (ddz - topLeftZ)
								+ (ddx - topLeftX) * (ddx - topLeftX);
						double distSqTopRight = (ddz - topRightZ) * (ddz - topRightZ)
								+ (ddx - topRightX) * (ddx - topRightX);
						double distSqBottomLeft = (ddz - bottomLeftZ) * (ddz - bottomLeftZ)
								+ (ddx - bottomLeftX) * (ddx - bottomLeftX);
						double distSqBottomRight = (ddz - bottomRightZ) * (ddz - bottomRightZ)
								+ (ddx - bottomRightX) * (ddx - bottomRightX);

						if (distSqTopLeft < distSqTopRight && distSqTopLeft < distSqBottomLeft
								&& distSqTopLeft < distSqBottomRight) {
							zoomedValues[zoomedIndex++] = valueTopLeft;
						} else if (distSqTopRight < distSqTopLeft && distSqTopRight < distSqBottomLeft
								&& distSqTopRight < distSqBottomRight) {
							zoomedValues[zoomedIndex++] = valueTopRight;
						} else if (distSqBottomLeft < distSqTopLeft && distSqBottomLeft < distSqTopRight
								&& distSqBottomLeft < distSqBottomRight) {
							zoomedValues[zoomedIndex++] = valueBottomLeft;
						} else {
							zoomedValues[zoomedIndex++] = valueBottomRight;
						}
					}
				}

				valueTopLeft = valueTopRight;
				valueBottomLeft = valueBottomRight;
			}
		}

		int[] values = IntCache.get(width * height);

		for (int i = 0; i < height; ++i) {
			System.arraycopy(zoomedValues, (i + (z & 3)) * zoomedWidth + (x & 3), values, i * width, width);
		}

		return values;
	}

}
