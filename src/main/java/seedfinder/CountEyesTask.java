package seedfinder;

public class CountEyesTask extends Task {

	private int eyes = 0;

	public CountEyesTask() {
		super(Type.COUNT_EYES);
	}

	public void addEye() {
		eyes++;
	}

	public int getEyes() {
		return eyes;
	}

}
