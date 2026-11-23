package com.celestered.earlyfixes;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

/**
 * Player-editable settings, stored in {@code config/celeste-early-fixes.properties}.
 * Everything defaults to on; a missing or broken file never stops the game from launching.
 */
final class Settings {
    static final String FILE_NAME = "celeste-early-fixes.properties";

    /** auto = only on machines that can afford it, on = always, off = never. */
    final String garbageCollector;
    final boolean preferDiscreteGpu;
    final boolean restoreSodiumCommandLine;

    private Settings(Properties p) {
        garbageCollector = p.getProperty("garbageCollector", "auto").trim().toLowerCase(Locale.ROOT);
        preferDiscreteGpu = bool(p, "preferDiscreteGpu", true);
        restoreSodiumCommandLine = bool(p, "restoreSodiumCommandLine", true);
    }

    static Path gameDir() {
        return Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
    }

    static Path dataDir() {
        return gameDir().resolve("config").resolve("celeste-early-fixes");
    }

    static Settings load() {
        Path file = gameDir().resolve("config").resolve(FILE_NAME);
        Properties p = new Properties();
        try {
            if (Files.isRegularFile(file)) {
                try (InputStream in = Files.newInputStream(file)) {
                    p.load(in);
                }
            } else {
                writeDefaults(file);
            }
        } catch (Throwable t) {
            Log.warn("Could not read " + file + ", using defaults: " + t);
        }
        return new Settings(p);
    }

    private static void writeDefaults(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("# Celeste Early Fixes - launch tweaks for Celeste Create Hell\n");
            w.write("# Changes take effect the next time you launch the game.\n\n");
            w.write("# Garbage collector: auto, on or off.\n");
            w.write("# auto switches to Generational ZGC (less stutter) on machines with 8+ CPU threads,\n");
            w.write("# 16 GB+ RAM and 8 GB+ given to Minecraft. It never overrides a collector you set yourself.\n");
            w.write("garbageCollector=auto\n\n");
            w.write("# On laptops with both integrated and dedicated graphics, ask Windows to run\n");
            w.write("# Minecraft's Java on the dedicated GPU. Never overrides a choice you made in\n");
            w.write("# Windows Settings > Display > Graphics.\n");
            w.write("preferDiscreteGpu=true\n\n");
            w.write("# Undo Sodium's NVIDIA command-line workaround before Relauncher restarts the game,\n");
            w.write("# so the restarted game gets the real launch arguments. Leave this on.\n");
            w.write("restoreSodiumCommandLine=true\n");
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }
}
