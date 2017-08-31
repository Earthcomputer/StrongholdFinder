package seedfinder.structure;

import java.util.Random;

import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;
import seedfinder.util.ChunkPos;

public class MineshaftFinder extends StructureFinder {

	public static final MineshaftFinder INSTANCE = new MineshaftFinder();

	private MineshaftFinder() {
	}

	@Override
	public boolean isStructureAt(Random rand, long worldSeed, ChunkPos pos) {
		return rand.nextDouble() < 0.004 && rand.nextInt(80) < Math.max(pos.getX(), pos.getZ());
	}

	@Override
	protected Mineshaft createStructure(Random rand, ChunkPos pos) {
		rand.nextInt(); // we do what we feel like
		isStructureAt(rand, 0, pos);

		int biome = BiomeProvider.getBiomes(null, (pos.getX() << 4) + 8, (pos.getZ() << 4) + 8, 1, 1)[0];
		MineshaftGen.Type mineshaftType;
		if (Biomes.getType(biome) == Biomes.EnumType.MESA) {
			mineshaftType = MineshaftGen.Type.MESA;
		} else {
			mineshaftType = MineshaftGen.Type.NORMAL;
		}
		return new Mineshaft(rand, pos.getX(), pos.getZ(), mineshaftType);
	}

	public static class Mineshaft extends Structure {
		private MineshaftGen.Type mineshaftType;

		public Mineshaft(Random rand, int chunkX, int chunkZ, MineshaftGen.Type mineshaftType) {
			this.mineshaftType = mineshaftType;

			MineshaftGen.DirtRoom start = new MineshaftGen.DirtRoom(0, rand, (chunkX << 4) + 2, (chunkZ << 4) + 2,
					mineshaftType);
			getComponents().add(start);
			start.addMoreComponents(start, getComponents(), rand);

			updateBoundingBox();

			// Equivalent code to adjustVertical, but adapted to cater for mesa
			// variant, and the dirt room's connectedRooms.
			int toMoveUp;
			if (mineshaftType == MineshaftGen.Type.MESA) {
				toMoveUp = 63 - getBoundingBox().getMaxY() + getBoundingBox().getYSize() / 2 + 5;
			} else {
				final int maxHeight = 63 - 10;
				int newTop = getBoundingBox().getYSize() + 1;

				if (newTop < maxHeight) {
					newTop += rand.nextInt(maxHeight - newTop);
				}

				toMoveUp = newTop - getBoundingBox().getMaxY();
			}

			setBoundingBox(getBoundingBox().getOffset(0, toMoveUp, 0));

			getComponents().forEach(component -> {
				component.setBoundingBox(component.getBoundingBox().getOffset(0, toMoveUp, 0));
				if (component instanceof MineshaftGen.DirtRoom) {
					MineshaftGen.DirtRoom room = (MineshaftGen.DirtRoom) component;
					for (int i = 0; i < room.connectedRooms.size(); i++) {
						room.connectedRooms.set(i, room.connectedRooms.get(i).getOffset(0, toMoveUp, 0));
					}
				}
			});
		}

		public MineshaftGen.Type getType() {
			return mineshaftType;
		}
	}

}
