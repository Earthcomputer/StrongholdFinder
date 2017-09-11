package seedfinder.structure;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Random;

import seedfinder.util.AABB;
import seedfinder.util.BlockPos;
import seedfinder.util.ChunkPos;
import seedfinder.util.Mirror;
import seedfinder.util.Rotation;

public class NBTStructurePlacementSettings {

	private Mirror mirror = Mirror.NONE;
	private Rotation rotation = Rotation.NONE;
	private OptionalInt replacedBlock = OptionalInt.empty();
	private Optional<ChunkPos> chunk = Optional.empty();
	private Optional<AABB> boundingBox = Optional.empty();
	private boolean ignoreStructureBlock = true;
	private float integrity = 1;
	private Optional<Random> rand = Optional.empty();
	private OptionalLong seed = OptionalLong.empty();

	public NBTStructurePlacementSettings setMirror(Mirror mirror) {
		this.mirror = mirror;
		return this;
	}

	public NBTStructurePlacementSettings setRotation(Rotation rotation) {
		this.rotation = rotation;
		return this;
	}

	public NBTStructurePlacementSettings setReplacedBlock(int replacedBlock) {
		this.replacedBlock = OptionalInt.of(replacedBlock);
		return this;
	}

	public NBTStructurePlacementSettings setChunk(ChunkPos chunk) {
		this.chunk = Optional.of(chunk);
		return this;
	}

	public NBTStructurePlacementSettings setBoundingBox(AABB boundingBox) {
		this.boundingBox = Optional.of(boundingBox);
		return this;
	}

	public NBTStructurePlacementSettings setIgnoreStructureBlock(boolean ignoreStructureBlock) {
		this.ignoreStructureBlock = ignoreStructureBlock;
		return this;
	}

	public NBTStructurePlacementSettings setIntegrity(float integrity) {
		this.integrity = integrity;
		return this;
	}

	public NBTStructurePlacementSettings setRNG(Random rand) {
		this.rand = Optional.of(rand);
		return this;
	}

	public NBTStructurePlacementSettings setSeed(long seed) {
		this.seed = OptionalLong.of(seed);
		return this;
	}

	public Mirror getMirror() {
		return mirror;
	}

	public Rotation getRotation() {
		return rotation;
	}

	public OptionalInt getReplacedBlock() {
		return replacedBlock;
	}

	public Optional<AABB> getBoundingBox() {
		if (!boundingBox.isPresent() && chunk.isPresent()) {
			setBoundingBoxFromChunk();
		}
		return boundingBox;
	}

	public boolean ignoreStructureBlock() {
		return ignoreStructureBlock;
	}

	public float getIntegrity() {
		return integrity;
	}

	public Random getRNG(Optional<BlockPos> pos) {
		if (rand.isPresent()) {
			return rand.get();
		}
		if (seed.isPresent()) {
			return seed.getAsLong() == 0 ? new Random(System.currentTimeMillis()) : new Random(seed.getAsLong());
		}
		if (pos.isPresent()) {
			BlockPos p = pos.get();
			int x = p.getX();
			int z = p.getZ();
			return new Random(x * x * 4987142 + x * 5947611 + z * z * 4392871L + z * 389711 ^ 987234911L);
		}
		return new Random(System.currentTimeMillis());
	}

	void setBoundingBoxFromChunk() {
		chunk.ifPresent(chunk -> {
			int x = chunk.getX() * 16;
			int z = chunk.getZ() * 16;
			boundingBox = Optional.of(new AABB(x, 0, z, x + 15, 255, z + 15));
		});
	}

}
