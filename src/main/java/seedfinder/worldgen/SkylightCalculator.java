package seedfinder.worldgen;

import seedfinder.Blocks;
import seedfinder.util.EnumFacing;
import seedfinder.util.Storage2D;
import seedfinder.util.Storage3D;

public class SkylightCalculator {

	private static Storage2D heightMap = new Storage2D(0);
	private static Storage3D skylight = new Storage3D(0);

	public static Storage3D calcSkylight(Storage3D world, int minX, int minZ, int maxX, int maxZ) {
		heightMap.reallocate(minX, minZ, maxX, maxZ);
		int maxHeight = Integer.MIN_VALUE;

		skylight.reallocate(minX, 0, minZ, maxX, 255, maxZ);

		// Calculate height map and set all blocks with direct sky access to 15
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = 255; y >= 0; y--) {
					if (Blocks.getOpacity(world.get(x, y, z)) != 0) {
						if (maxHeight < y + 1) {
							maxHeight = y + 1;
						}
						heightMap.set(x, z, y + 1);
						break;
					} else {
						skylight.set(x, y, z, 15);
					}
				}
			}
		}

		// Update neighbors' skylight level within the finer details of the
		// surface terrain
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int height = heightMap.get(x, z);
				for (int y = maxHeight; y >= height; y--) {
					for (EnumFacing side : EnumFacing.values()) {
						int xOff = x + side.getXOffset();
						int yOff = y + side.getYOffset();
						int zOff = z + side.getZOffset();
						updateSkylight(world, skylight, xOff, yOff, zOff,
								reduceLight(15, Blocks.getOpacity(world.get(xOff, yOff, zOff))), minX, minZ, maxX,
								maxZ);
					}
				}
			}
		}

		return skylight;
	}

	private static void updateSkylight(Storage3D world, Storage3D skylight, int x, int y, int z, int newLight, int minX,
			int minZ, int maxX, int maxZ) {
		if (x < minX || x > maxX || z < minZ || z > maxZ || y < 0 || y > 255) {
			// don't update out of bounds skylight
			return;
		}
		if (newLight <= skylight.get(x, y, z)) {
			// we won't be changing anything, so there's no more work to do.
			return;
		}

		skylight.set(x, y, z, newLight);

		// Update neighbors' skylight values
		for (EnumFacing side : EnumFacing.values()) {
			int xOff = x + side.getXOffset();
			int yOff = y + side.getYOffset();
			int zOff = z + side.getZOffset();
			updateSkylight(world, skylight, xOff, yOff, zOff,
					reduceLight(newLight, Blocks.getOpacity(world.get(xOff, yOff, zOff))), minX, minZ, maxX, maxZ);
		}
	}

	private static int reduceLight(int light, int opacity) {
		if (opacity == 0) {
			return light - 1;
		} else {
			return light - opacity;
		}
	}

}
