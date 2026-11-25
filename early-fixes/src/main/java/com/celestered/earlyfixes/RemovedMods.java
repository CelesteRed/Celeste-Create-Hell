package com.celestered.earlyfixes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Deletes mods that were removed from the pack. unsup only deletes files it installed itself, so jars that
 * came from an .mrpack import (or an older pack version) would otherwise stay in players' mods folders.
 * Runs before any mod is loaded, so the jars are not locked yet.
 */
final class RemovedMods {
    /** Lowercase filename prefixes of mods that are no longer part of Celeste Create Hell. */
    private static final String[] PREFIXES = {
            "emotecraft-", "emotetweaks-", "figura-", "cosmetica-", "pehkui-", "create_dupe_fix", "createdupefix",
            "zfastnoise", "fastnoise",
    };

    private RemovedMods() {}

    static void clean() {
        Path mods = Paths.get("mods");
        if (!Files.isDirectory(mods)) return;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(mods, "*.jar")) {
            for (Path f : files) {
                String name = f.getFileName().toString().toLowerCase(Locale.ROOT);
                for (String p : PREFIXES) {
                    if (name.startsWith(p)) {
                        try {
                            Files.delete(f);
                            Log.info("Removed mod that is no longer in the pack: " + f.getFileName());
                        } catch (IOException e) {
                            Log.warn("Could not remove old mod " + f.getFileName() + ": " + e);
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Log.warn("Could not scan mods folder: " + e);
        }
    }
}
