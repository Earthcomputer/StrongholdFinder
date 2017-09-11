package seedfinder.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;

import seedfinder.Blocks;
import seedfinder.nbt.NBTCompound;
import seedfinder.nbt.NBTList;
import seedfinder.util.AABB;
import seedfinder.util.BlockPos;
import seedfinder.util.Mirror;
import seedfinder.util.Rotation;
import seedfinder.util.Storage3D;

public class NBTStructure {

	private int xSize;
	private int ySize;
	private int zSize;
	private List<BlockInfo> blocks = new ArrayList<>();

	private NBTStructure() {
	}

	public BlockPos findConnectedPos(NBTStructurePlacementSettings settings1, int x1, int y1, int z1,
			NBTStructurePlacementSettings settings2, int x2, int y2, int z2) {
		BlockPos pos1 = transform(x1, y1, z1, settings1);
		BlockPos pos2 = transform(x2, y2, z2, settings2);
		return pos1.add(-pos2.getX(), -pos2.getY(), -pos2.getZ());
	}

	public void placeInChunk(Storage3D world, int x, int y, int z, NBTStructurePlacementSettings settings) {
		settings.setBoundingBoxFromChunk();
	}

	public void placeInWorld(Storage3D world, int x, int y, int z, NBTStructurePlacementSettings settings) {
		float chance = settings.getIntegrity();
		if (chance >= 1) {
			placeInWorld(world, x, y, z, IBlockProcessor.identity(), settings);
		} else {
			Random rand = settings.getRNG(Optional.of(new BlockPos(x, y, z)));
			placeInWorld(world, x, y, z,
					(w, pos, block) -> rand.nextFloat() <= chance ? Optional.of(block) : Optional.empty(), settings);
		}
	}

	public void placeInWorld(Storage3D world, int x, int y, int z, IBlockProcessor blockProcessor,
			NBTStructurePlacementSettings settings) {
		if (!blocks.isEmpty() && xSize >= 1 && ySize >= 1 && zSize >= 1) {
			OptionalInt replacedBlock = settings.getReplacedBlock();
			Optional<AABB> bounds = settings.getBoundingBox();

			blocks.forEach(block -> {
				BlockPos pos = transform(block.getX(), block.getY(), block.getZ(), settings).add(x, y, z);
				blockProcessor.process(world, pos, block).ifPresent(newBlock -> {
					int blockId = newBlock.getBlock();
					if ((!replacedBlock.isPresent() || replacedBlock.getAsInt() != blockId)
							&& (!settings.ignoreStructureBlock() || blockId != Blocks.STRUCTURE_BLOCK)
							&& (!bounds.isPresent() || bounds.get().contains(pos))) {
						world.set(pos.getX(), pos.getY(), pos.getZ(), blockId);
					}
				});
			});
		}
	}

	public Map<BlockPos, String> getDataBlocks(int x, int y, int z, NBTStructurePlacementSettings settings) {
		Map<BlockPos, String> dataBlocks = new HashMap<>();
		Optional<AABB> bounds = settings.getBoundingBox();

		blocks.forEach(block -> {
			BlockPos pos = transform(block.getX(), block.getY(), block.getZ(), settings).add(x, y, z);
			if (!bounds.isPresent() || bounds.get().contains(pos)) {
				if (block.getBlock() == Blocks.STRUCTURE_BLOCK && block.hasTileEntity()) {
					NBTCompound nbt = block.getTileEntity();
					if ("DATA".equals(nbt.getString("mode"))) {
						dataBlocks.put(pos, nbt.getString("metadata"));
					}
				}
			}
		});

		return dataBlocks;
	}

	public static BlockPos transform(int x, int y, int z, NBTStructurePlacementSettings settings) {
		return transform(x, y, z, settings.getMirror(), settings.getRotation());
	}

	public static BlockPos transform(int x, int y, int z, Mirror mirror, Rotation rotation) {
		switch (mirror) {
		case LEFT_RIGHT:
			z = -z;
			break;
		case FRONT_BACK:
			x = -x;
			break;
		default:
			break;
		}

		switch (rotation) {
		case ROTATE_270:
			return new BlockPos(z, y, -x);
		case ROTATE_90:
			return new BlockPos(-z, y, x);
		case ROTATE_180:
			return new BlockPos(-x, y, -z);
		default:
			return new BlockPos(x, y, z);
		}
	}

	public static NBTStructure readFromNBT(NBTCompound root) {
		NBTStructure struct = new NBTStructure();

		NBTList size = root.getList("size");
		struct.xSize = size.getInt(0);
		struct.ySize = size.getInt(1);
		struct.zSize = size.getInt(2);

		NBTList paletteList = root.getList("palette");
		int[] palette = new int[paletteList.size()];
		for (int i = 0; i < palette.length; i++) {
			NBTCompound blockState = paletteList.getCompound(i);
			if (!blockState.containsKey("Name")) {
				palette[i] = Blocks.AIR;
			} else {
				palette[i] = Blocks.getByName(blockState.getString("Name")).orElseGet(() -> {
					NBTStructures.UNKNOWN_BLOCKS.add(blockState.getString("Name"));
					return Blocks.AIR;
				});
			}
		}

		NBTList blocks = root.getList("blocks");
		blocks.forEachCompound(block -> {
			NBTList pos = block.getList("pos");
			int paletteIdx = block.getInt("state");
			int blockId = paletteIdx < 0 || paletteIdx >= palette.length ? Blocks.AIR : palette[paletteIdx];
			Optional<NBTCompound> tileEntity = block.containsKey("nbt") ? Optional.of(block.getCompound("nbt"))
					: Optional.empty();
			struct.blocks.add(new BlockInfo(pos.getInt(0), pos.getInt(1), pos.getInt(2), blockId, tileEntity));
		});

		return struct;
	}

	private static class BlockInfo {
		private int x;
		private int y;
		private int z;
		private int block;
		private Optional<NBTCompound> tileEntity;

		public BlockInfo(int x, int y, int z, int block, Optional<NBTCompound> tileEntity) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.block = block;
			this.tileEntity = tileEntity;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getZ() {
			return z;
		}

		public int getBlock() {
			return block;
		}

		public NBTCompound getTileEntity() {
			return tileEntity.get();
		}

		public boolean hasTileEntity() {
			return tileEntity.isPresent();
		}
	}

	@FunctionalInterface
	public static interface IBlockProcessor {
		Optional<BlockInfo> process(Storage3D world, BlockPos pos, BlockInfo block);

		public static IBlockProcessor identity() {
			return (world, pos, block) -> Optional.of(block);
		}
	}

}
