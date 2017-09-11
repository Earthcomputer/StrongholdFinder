package seedfinder.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class MinecraftJarHelper {

	public static Optional<File> getLatestJarFile() {
		// Find the Minecraft directory
		String os = System.getProperty("os.name").toLowerCase();
		File file = null;
		if (os.contains("mac") || os.contains("darwin")) {
			file = new File(System.getProperty("user.home"), "Library/Application Support/minecraft");
		} else if (os.contains("win")) {
			file = new File(System.getenv("appdata"), ".minecraft");
		}
		if (file == null || !file.exists()) {
			file = new File(System.getProperty("user.home"), ".minecraft");
		}
		// Find the latest version folder
		file = new File(file, "versions");
		if (!file.exists()) {
			return Optional.empty();
		}
		File[] versionDirs = file.listFiles(File::isDirectory);
		if (versionDirs.length == 0) {
			return Optional.empty();
		}
		// Sort directories so that the latest version appears first
		// Versions are sorted from later to older versions, with unknown
		// version formats last.
		Arrays.sort(versionDirs, (a, b) -> {
			String[] aParts = a.getName().split(".");
			String[] bParts = b.getName().split(".");
			for (int i = 0, e = Math.max(aParts.length, bParts.length); i < e; i++) {
				int aNum;
				try {
					aNum = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
				} catch (NumberFormatException ex) {
					aNum = Integer.MIN_VALUE;
				}
				int bNum;
				try {
					bNum = i < bParts.length ? Integer.parseInt(bParts[i]) : 0;
				} catch (NumberFormatException ex) {
					bNum = Integer.MIN_VALUE;
				}
				if (aNum != bNum) {
					// Compare backwards: bigger numbers first
					return Integer.compare(bNum, aNum);
				}
			}
			return 0;
		});
		// Find the jar file
		for (File versionDir : versionDirs) {
			file = new File(versionDir, versionDir.getName() + ".jar");
			if (file.exists()) {
				return Optional.of(file);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a stream of pairs of values. The keys contain the structure
	 * names, while the values contain input streams with the NBT data of those
	 * structures. The returned stream may throw an IOException wrapped in a
	 * RuntimeException, so this IOException has to be unwrapped.
	 */
	public static Stream<Map.Entry<String, InputStream>> getStructures(JarFile jarFile) throws IOException {
		return Collections.list(jarFile.entries()).stream()
				.filter(it -> it.getName().startsWith("assets/minecraft/structures/") && it.getName().endsWith(".nbt"))
				.map(it -> {
					try {
						return new AbstractMap.SimpleEntry<>(
								it.getName().substring("assets/minecraft/structures/".length(),
										it.getName().length() - ".nbt".length()),
								jarFile.getInputStream(it));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});

	}

}
