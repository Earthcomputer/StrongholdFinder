package seedfinder.biome;

public class GenLayerFuzzyZoom extends GenLayerZoom {

	public GenLayerFuzzyZoom(long uniquifier, GenLayer parent) {
		super(uniquifier, parent);
	}
	
	@Override
	public int modeOrRandom(int a, int b, int c, int d) {
		return choose(a, b, c, d);
	}

}
