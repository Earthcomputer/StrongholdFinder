package seedfinder.structure;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import seedfinder.nbt.NBTBase;
import seedfinder.nbt.NBTCompound;
import seedfinder.util.MinecraftJarHelper;

public class NBTStructures {

	public static final String IGLOO_TOP = "igloo/igloo_top";
	public static final String IGLOO_MIDDLE = "igloo/igloo_middle";
	public static final String IGLOO_BOTTOM = "igloo/igloo_bottom";

	private static final Map<String, NBTStructure> structures = new HashMap<>();
	static final Set<String> UNKNOWN_BLOCKS = new LinkedHashSet<>();

	public static void load() throws IOException {
		UNKNOWN_BLOCKS.clear();
		File jarFile = MinecraftJarHelper.getLatestJarFile()
				.orElseThrow(() -> new IOException("Unable to find Minecraft JAR"));
		try (JarFile jar = new JarFile(jarFile)) {
			MinecraftJarHelper.getStructures(jar).forEach(entry -> {
				try {
					NBTCompound tag = NBTBase.readCompressed(entry.getValue());
					structures.put(entry.getKey(), NBTStructure.readFromNBT(tag));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (RuntimeException e) {
			if (e.getClass() != RuntimeException.class) {
				throw e;
			}
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
			throw e;
		}
		UNKNOWN_BLOCKS.forEach(block -> System.err.println("Unknown block in structure: \"" + block + "\""));
	}

	public static NBTStructure get(String id) {
		return structures.get(id);
	}

}
