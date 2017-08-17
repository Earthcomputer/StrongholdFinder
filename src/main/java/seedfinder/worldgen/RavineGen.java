package seedfinder.worldgen;

import java.util.Random;

import seedfinder.Blocks;
import seedfinder.MathHelper;
import seedfinder.Storage3D;

public class RavineGen {

	private static final int RANGE = 8;

	// TODO: why does this have to be 1024?
	private static float[] radiusSqScaleByHeight = new float[1024];

	public static void generate(Random rand, long seed, int x, int z, Storage3D chunk) {
		for (int dx = -RANGE; dx <= RANGE; dx++) {
			for (int dz = -RANGE; dz <= RANGE; dz++) {
				WorldGen.setMapGenSeedForChunk(rand, seed, x + dx, z + dz);
				recursiveGenerate(rand, x + dx, z + dz, x, z, chunk);
			}
		}
	}

	private static void recursiveGenerate(Random rand, int chunkX, int chunkZ, int originalX, int originalZ,
			Storage3D chunk) {
		if (rand.nextInt(50) == 0) {
			double x = (double) (chunkX * 16 + rand.nextInt(16));
			double y = (double) (rand.nextInt(rand.nextInt(40) + 8) + 20);
			double z = (double) (chunkZ * 16 + rand.nextInt(16));
			final int numRavines = 1;

			for (int i = 0; i < numRavines; i++) {
				float yaw = rand.nextFloat() * ((float) Math.PI * 2F);
				float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float maxRadius = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
				addTunnel(rand.nextLong(), originalX, originalZ, chunk, x, y, z, maxRadius, yaw, pitch, 0, 0, 3.0D);
			}
		}
	}

	private static void addTunnel(long seed, int originalX, int originalZ, Storage3D chunk, double x, double y,
			double z, float maxRadius, float yaw, float pitch, int startDistance, int endDistance,
			double widthToHeightRatio) {
		Random rand = new Random(seed);
		double chunkCenterX = (double) (originalX * 16 + 8);
		double chunkCenterZ = (double) (originalZ * 16 + 8);
		float deltaYaw = 0.0F;
		float deltaPitch = 0.0F;

		if (endDistance <= 0) {
			int maxDistance = RANGE * 16 - 16;
			endDistance = maxDistance - rand.nextInt(maxDistance / 4);
		}

		boolean isRoom = false;

		if (startDistance == -1) {
			startDistance = endDistance / 2;
			isRoom = true;
		}

		float radiusSq = 1.0F;

		for (int blockY = 0; blockY < 256; blockY++) {
			if (blockY == 0 || rand.nextInt(3) == 0) {
				radiusSq = 1.0F + rand.nextFloat() * rand.nextFloat();
			}

			radiusSqScaleByHeight[blockY] = radiusSq * radiusSq;
		}

		for (int distance = startDistance; distance < endDistance; distance++) {
			double hRadius = 1.5D
					+ (double) (MathHelper.sin((float) distance * (float) Math.PI / (float) endDistance) * maxRadius);
			double vRadius = hRadius * widthToHeightRatio;
			hRadius = hRadius * ((double) rand.nextFloat() * 0.25D + 0.75D);
			vRadius = vRadius * ((double) rand.nextFloat() * 0.25D + 0.75D);
			float dh = MathHelper.cos(pitch);
			float dy = MathHelper.sin(pitch);
			x += (double) (MathHelper.cos(yaw) * dh);
			y += (double) dy;
			z += (double) (MathHelper.sin(yaw) * dh);
			pitch = pitch * 0.7F;
			pitch = pitch + deltaPitch * 0.05F;
			yaw += deltaYaw * 0.05F;
			deltaPitch = deltaPitch * 0.8F;
			deltaYaw = deltaYaw * 0.5F;
			deltaPitch = deltaPitch + (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * 2.0F;
			deltaYaw = deltaYaw + (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * 4.0F;

			if (isRoom || rand.nextInt(4) != 0) {
				double dxFromCenter = x - chunkCenterX;
				double dzFromCenter = z - chunkCenterZ;
				double distanceLeft = (double) (endDistance - distance);
				double d6 = (double) (maxRadius + 2.0F + 16.0F); // TODO: what
																	// is this?

				if (dxFromCenter * dxFromCenter + dzFromCenter * dzFromCenter - distanceLeft * distanceLeft > d6 * d6) {
					return;
				}

				if (x >= chunkCenterX - 16.0D - hRadius * 2.0D && z >= chunkCenterZ - 16.0D - hRadius * 2.0D
						&& x <= chunkCenterX + 16.0D + hRadius * 2.0D && z <= chunkCenterZ + 16.0D + hRadius * 2.0D) {
					int minX = MathHelper.floor(x - hRadius) - originalX * 16 - 1;
					int maxX = MathHelper.floor(x + hRadius) - originalX * 16 + 1;
					int minY = MathHelper.floor(y - vRadius) - 1;
					int maxY = MathHelper.floor(y + vRadius) + 1;
					int minZ = MathHelper.floor(z - hRadius) - originalZ * 16 - 1;
					int maxZ = MathHelper.floor(z + hRadius) - originalZ * 16 + 1;

					if (minX < 0) {
						minX = 0;
					}

					if (maxX > 16) {
						maxX = 16;
					}

					if (minY < 1) {
						minY = 1;
					}

					if (maxY > 248) {
						maxY = 248;
					}

					if (minZ < 0) {
						minZ = 0;
					}

					if (maxZ > 16) {
						maxZ = 16;
					}

					boolean foundWater = false;

					for (int blockX = minX; !foundWater && blockX < maxX; blockX++) {
						for (int blockZ = minZ; !foundWater && blockZ < maxZ; blockZ++) {
							for (int blockY = maxY + 1; !foundWater && blockY >= minY - 1; blockY--) {
								if (blockY >= 0 && blockY < 256) {
									int block = chunk.get(blockX, blockY, blockZ);

									if (block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
										foundWater = true;
									}

									if (blockY != minY - 1 && blockX != minX && blockX != maxX - 1 && blockZ != minZ
											&& blockZ != maxZ - 1) {
										blockY = minY;
									}
								}
							}
						}
					}

					if (!foundWater) {
						for (int blockX = minX; blockX < maxX; ++blockX) {
							double normDx = ((double) (blockX + originalX * 16) + 0.5D - x) / hRadius;

							for (int blockZ = minZ; blockZ < maxZ; ++blockZ) {
								double normDz = ((double) (blockZ + originalZ * 16) + 0.5D - z) / hRadius;

								if (normDx * normDx + normDz * normDz < 1.0D) {
									for (int blockY = maxY; blockY > minY; --blockY) {
										double normDy = ((double) (blockY - 1) + 0.5D - y) / vRadius;

										if ((normDx * normDx + normDz * normDz)
												* (double) radiusSqScaleByHeight[blockY - 1]
												+ normDy * normDy / 6.0D < 1.0D) {
											int block = chunk.get(blockX, blockY, blockZ);

											if (block == Blocks.STONE || block == Blocks.DIRT
													|| block == Blocks.GRASS) {
												if (blockY - 1 < 10) {
													chunk.set(blockX, blockY, blockZ, Blocks.FLOWING_LAVA);
												} else {
													chunk.set(blockX, blockY, blockZ, Blocks.AIR);
												}
											}
										}
									}
								}
							}
						}

						if (isRoom) {
							break;
						}
					}
				}
			}
		}
	}

}
