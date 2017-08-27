package seedfinder.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedfinder.AABB;
import seedfinder.Storage3D;

/**
 * Represents a configuration of structure components (rooms, corridors, etc.)
 */
public abstract class Structure {

	private List<Component> components = new ArrayList<>();
	private AABB boundingBox;

	public List<Component> getComponents() {
		return components;
	}

	public AABB getBoundingBox() {
		return boundingBox;
	}

	public boolean isValid() {
		return true;
	}

	/**
	 * Updates the main bounding box to the minimum-sized bounding box
	 * containing all of the structure's components.
	 */
	protected void updateBoundingBox() {
		boundingBox = components.stream().map(Component::getBoundingBox).reduce(new AABB(Integer.MAX_VALUE,
				Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
				AABB::max);
	}

	/**
	 * Randomly moves the structure vertically
	 */
	protected void adjustVertical(Random rand) {
		final int maxHeight = 63 - 10;
		int newTop = this.boundingBox.getYSize() + 1;

		if (newTop < maxHeight) {
			newTop += rand.nextInt(maxHeight - newTop);
		}

		int toMoveUp = newTop - boundingBox.getMaxY();
		boundingBox = boundingBox.getOffset(0, toMoveUp, 0);

		components.forEach(c -> c.setBoundingBox(c.getBoundingBox().getOffset(0, toMoveUp, 0)));
	}

	/**
	 * Populates this structure into the world. Assumes the RNG's seed has
	 * already been initialized.
	 */
	public void populate(Storage3D world, Random rand, int chunkX, int chunkZ) {
		AABB popBB = new AABB((chunkX << 4) + 8, 1, (chunkZ << 4) + 8, (chunkX << 4) + 8 + 15, 512,
				(chunkZ << 4) + 8 + 15);

		components.forEach(component -> {
			if (component.getBoundingBox().intersectsWith(popBB)) {
				component.placeInWorld(world, rand, popBB);
			}
		});
	}

}
