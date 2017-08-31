package seedfinder.worldgen;

import java.util.Random;

import seedfinder.Blocks;
import seedfinder.util.MathHelper;
import seedfinder.util.Storage3D;

public class CaveGen {

	private static final int RANGE = 8;

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
		int tries = rand.nextInt(rand.nextInt(rand.nextInt(15) + 1) + 1);

		if (rand.nextInt(7) != 0) {
			tries = 0;
		}

		for (int i = 0; i < tries; i++) {
			double x = chunkX * 16 + rand.nextInt(16);
			double y = rand.nextInt(rand.nextInt(120) + 8);
			double z = chunkZ * 16 + rand.nextInt(16);
			int numTunnels = 1;

			if (rand.nextInt(4) == 0) {
				addRoom(rand, rand.nextLong(), originalX, originalZ, chunk, x, y, z);
				numTunnels += rand.nextInt(4);
			}

			for (int j = 0; j < numTunnels; j++) {
				float yaw = rand.nextFloat() * ((float) Math.PI * 2F);
				float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float maxRadius = rand.nextFloat() * 2.0F + rand.nextFloat();

				if (rand.nextInt(10) == 0) {
					maxRadius *= rand.nextFloat() * rand.nextFloat() * 3.0F + 1.0F;
				}

				addTunnel(rand.nextLong(), originalX, originalZ, chunk, x, y, z, maxRadius, yaw, pitch, 0, 0, 1.0D);
			}
		}
	}

	protected static void addRoom(Random rand, long tunnelSeed, int originalX, int originalZ, Storage3D chunk, double x,
			double y, double z) {
		addTunnel(tunnelSeed, originalX, originalZ, chunk, x, y, z, 1.0F + rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1,
				0.5D);
	}

	protected static void addTunnel(long tunnelSeed, int originalX, int originalZ, Storage3D chunk, double x, double y,
			double z, float maxRadius, float yaw, float pitch, int startDistance, int endDistance,
			double widthToHeightRatio) {
		double chunkCenterX = originalX * 16 + 8;
		double chunkCenterZ = originalZ * 16 + 8;
		float deltaYaw = 0.0F;
		float deltaPitch = 0.0F;
		Random rand = new Random(tunnelSeed);

		if (endDistance <= 0) {
			int maxDistance = RANGE * 16 - 16;
			endDistance = maxDistance - rand.nextInt(maxDistance / 4);
		}

		boolean isRoom = false;

		if (startDistance == -1) {
			startDistance = endDistance / 2;
			isRoom = true;
		}

		int tJunctionDistance = rand.nextInt(endDistance / 2) + endDistance / 4;

		boolean steep = rand.nextInt(6) == 0;

		for (int distance = startDistance; distance < endDistance; distance++) {
			double hRadius = 1.5D + MathHelper.sin(distance * (float) Math.PI / endDistance) * maxRadius;
			double vRadius = hRadius * widthToHeightRatio;
			float dh = MathHelper.cos(pitch);
			float dy = MathHelper.sin(pitch);
			x += MathHelper.cos(yaw) * dh;
			y += dy;
			z += MathHelper.sin(yaw) * dh;

			if (steep) {
				pitch = pitch * 0.92F;
			} else {
				pitch = pitch * 0.7F;
			}

			pitch = pitch + deltaPitch * 0.1F;
			yaw += deltaYaw * 0.1F;
			deltaPitch = deltaPitch * 0.9F;
			deltaYaw = deltaYaw * 0.75F;
			deltaPitch = deltaPitch + (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * 2.0F;
			deltaYaw = deltaYaw + (rand.nextFloat() - rand.nextFloat()) * rand.nextFloat() * 4.0F;

			if (!isRoom && distance == tJunctionDistance && maxRadius > 1.0F && endDistance > 0) {
				addTunnel(rand.nextLong(), originalX, originalZ, chunk, x, y, z, rand.nextFloat() * 0.5F + 0.5F,
						yaw - (float) Math.PI / 2F, pitch / 3.0F, distance, endDistance, 1.0D);
				addTunnel(rand.nextLong(), originalX, originalZ, chunk, x, y, z, rand.nextFloat() * 0.5F + 0.5F,
						yaw + (float) Math.PI / 2F, pitch / 3.0F, distance, endDistance, 1.0D);
				return;
			}

			if (isRoom || rand.nextInt(4) != 0) {
				double dxFromCenter = x - chunkCenterX;
				double dzFromCenter = z - chunkCenterZ;
				double distanceLeft = endDistance - distance;
				double d7 = maxRadius + 2.0F + 16.0F; // TODO: what
														// is this?

				if (dxFromCenter * dxFromCenter + dzFromCenter * dzFromCenter - distanceLeft * distanceLeft > d7 * d7) {
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
						for (int blockX = minX; blockX < maxX; blockX++) {
							double normDx = (blockX + originalX * 16 + 0.5D - x) / hRadius;

							for (int blockZ = minZ; blockZ < maxZ; blockZ++) {
								double normDz = (blockZ + originalZ * 16 + 0.5D - z) / hRadius;

								if (normDx * normDx + normDz * normDz < 1.0D) {
									for (int blockY = maxY; blockY > minY; blockY--) {
										double normDy = (blockY - 1 + 0.5D - y) / vRadius;

										if (normDy > -0.7D
												&& normDx * normDx + normDy * normDy + normDz * normDz < 1.0D) {
											int blockHere = chunk.get(blockX, blockY, blockZ);
											int blockAbove = chunk.get(blockX, blockY + 1, blockZ);

											if (canReplaceBlock(blockHere, blockAbove)) {
												if (blockY - 1 < 10) {
													chunk.set(blockX, blockY, blockZ, Blocks.LAVA);
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

	protected static boolean canReplaceBlock(int blockHere, int blockAbove) {
		if (blockHere == Blocks.STONE) {
			return true;
		} else if (blockHere == Blocks.DIRT) {
			return true;
		} else if (blockHere == Blocks.GRASS) {
			return true;
		} else if (blockHere == Blocks.HARDENED_CLAY) {
			return true;
		} else if (blockHere == Blocks.SANDSTONE) {
			return true;
		} else if (blockHere == Blocks.MYCELIUM) {
			return true;
		} else if (blockHere == Blocks.SNOW_LAYER) {
			return true;
		} else if (blockHere == Blocks.SAND || blockHere == Blocks.GRAVEL) {
			return blockAbove != Blocks.WATER && blockAbove != Blocks.FLOWING_WATER;
		} else {
			return false;
		}
	}
}
