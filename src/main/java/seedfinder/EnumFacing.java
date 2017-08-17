package seedfinder;

import java.util.Random;

public enum EnumFacing {

	UP(0, 1, 0, Axis.Y), DOWN(0, -1, 0, Axis.Y), NORTH(0, 0, -1, Axis.Z), SOUTH(0, 0, 1, Axis.Z), WEST(-1, 0, 0,
			Axis.X), EAST(1, 0, 0, Axis.X);

	// Offsets to offset a block position in this direction
	private final int xOff;
	private final int yOff;
	private final int zOff;

	private final Axis axis;

	private EnumFacing(int xOff, int yOff, int zOff, Axis axis) {
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.axis = axis;
	}

	public int getXOffset() {
		return xOff;
	}

	public int getYOffset() {
		return yOff;
	}

	public int getZOffset() {
		return zOff;
	}

	public EnumFacing getOpposite() {
		switch (this) {
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case NORTH:
			return SOUTH;
		case SOUTH:
			return NORTH;
		case WEST:
			return EAST;
		case EAST:
			return WEST;
		default:
			throw new AssertionError();
		}
	}

	public Axis getAxis() {
		return axis;
	}

	public static enum Axis {
		X, Y, Z;
	}

	public static enum Plane {
		HORIZONTAL, VERTICAL;

		public EnumFacing[] facings() {
			switch (this) {
			case HORIZONTAL:
				return new EnumFacing[] { NORTH, EAST, SOUTH, WEST };
			case VERTICAL:
				return new EnumFacing[] { UP, DOWN };
			default:
				throw new AssertionError();
			}
		}

		public EnumFacing random(Random rand) {
			EnumFacing[] facings = facings();
			return facings[rand.nextInt(facings.length)];
		}
	}

}
