package seedfinder.structure;

import java.util.Random;
import java.util.Set;

import seedfinder.biome.BiomeProvider;
import seedfinder.biome.Biomes;
import seedfinder.util.ChunkPos;

public class TempleFinder extends ScatteredStructureFinder {

	public static final TempleFinder INSTANCE = new TempleFinder();

	private static final Set<Integer> ALLOWED_BIOMES = Biomes.setOf(Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.JUNGLE,
			Biomes.JUNGLE_HILLS, Biomes.ICE_FLATS, Biomes.TAIGA_COLD);

	private TempleFinder() {
		super(14357617);
	}

	@Override
	protected boolean isValidPosition(Random rand, int chunkX, int chunkZ) {
		return ALLOWED_BIOMES.contains(BiomeProvider.getBiomes(null, chunkX * 16 + 8, chunkZ * 16 + 8, 1, 1)[0]);
	}

	@Override
	protected Temple createStructure(Random rand, ChunkPos pos) {
		rand.nextInt(); // meh

		Temple.Type type;
		int biome = BiomeProvider.getBiomes(null, pos.getX() * 16 + 8, pos.getZ() * 16 + 8, 1, 1)[0];

		switch (Biomes.getType(biome)) {
		case DESERT:
			type = Temple.Type.DESERT_TEMPLE;
			break;
		case JUNGLE:
			type = Temple.Type.JUNGLE_TEMPLE;
			break;
		case SWAMP:
			type = Temple.Type.WITCH_HUT;
			break;
		case SNOW:
			type = Temple.Type.IGLOO;
			break;
		default:
			throw new IllegalStateException("Invalid biome for temple with ID: " + biome);
		}

		return new Temple(rand, pos.getX(), pos.getZ(), type);
	}

	public static class Temple extends Structure {

		private Type type;

		public Temple(Random rand, int chunkX, int chunkZ, Type type) {
			this.type = type;

			switch (type) {
			case DESERT_TEMPLE:
				getComponents().add(new TempleGen.DesertTemple(rand, chunkX * 16, chunkZ * 16));
				break;
			case JUNGLE_TEMPLE:
				getComponents().add(new TempleGen.JungleTemple(rand, chunkX * 16, chunkZ * 16));
				break;
			case WITCH_HUT:
				getComponents().add(new TempleGen.WitchHit(rand, chunkX * 16, chunkZ * 16));
				break;
			case IGLOO:
				getComponents().add(new TempleGen.Igloo(rand, chunkX * 16, chunkZ * 16));
				break;
			}

			updateBoundingBox();
		}

		public Type getType() {
			return type;
		}

		public static enum Type {
			DESERT_TEMPLE, JUNGLE_TEMPLE, WITCH_HUT, IGLOO
		}

	}

}
