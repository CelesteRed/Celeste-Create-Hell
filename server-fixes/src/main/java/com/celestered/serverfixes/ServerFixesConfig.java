package com.celestered.serverfixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/** config/celeste_server_fixes.json, written with defaults on first start. Restart to apply changes. */
public final class ServerFixesConfig {
    /** Seconds of fan airflow it takes to evaporate one full block of water (or turn a water catalyst back to empty). 0 = never. */
    public static int fanWaterLifetimeSeconds = 300;
    /** Water-holding fan catalyst blocks and what they turn into once their water has evaporated. */
    public static Map<String, String> evaporatesInto = new LinkedHashMap<>();
    /** Dripstone filling a cauldron uses up the water/lava source above it (water: 1 in 3 drips on average = 1 source per full cauldron). */
    public static boolean dripstoneConsumesSource = true;

    static {
        evaporatesInto.put("create_connected:fan_splashing_catalyst", "create_connected:empty_fan_catalyst");
        evaporatesInto.put("dndesires:splashing_sail", "create:sail_frame");
        load();
    }

    private ServerFixesConfig() {}

    public static void init() {}

    private static void load() {
        Path file = Paths.get("config", "celeste_server_fixes.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try {
            if (Files.exists(file)) {
                JsonObject o = gson.fromJson(Files.readString(file), JsonObject.class);
                if (o.has("fanWaterLifetimeSeconds")) fanWaterLifetimeSeconds = Math.max(0, o.get("fanWaterLifetimeSeconds").getAsInt());
                if (o.has("dripstoneConsumesSource")) dripstoneConsumesSource = o.get("dripstoneConsumesSource").getAsBoolean();
                if (o.has("evaporatesInto")) {
                    Map<String, String> m = new LinkedHashMap<>();
                    o.getAsJsonObject("evaporatesInto").entrySet().forEach(e -> m.put(e.getKey(), e.getValue().getAsString()));
                    evaporatesInto = m;
                }
            }
            JsonObject out = new JsonObject();
            out.addProperty("_comment", "Celeste Server Fixes. fanWaterLifetimeSeconds: fan airflow time to evaporate one full water block (0 = off). evaporatesInto: water fan catalysts -> block they become when dry. dripstoneConsumesSource: dripstone cauldron filling uses up the source above.");
            out.addProperty("fanWaterLifetimeSeconds", fanWaterLifetimeSeconds);
            JsonObject m = new JsonObject();
            evaporatesInto.forEach(m::addProperty);
            out.add("evaporatesInto", m);
            out.addProperty("dripstoneConsumesSource", dripstoneConsumesSource);
            Files.createDirectories(file.getParent());
            Files.writeString(file, gson.toJson(out));
        } catch (IOException | RuntimeException e) {
            System.err.println("[celeste_server_fixes] could not read/write " + file + ": " + e);
        }
    }
}
