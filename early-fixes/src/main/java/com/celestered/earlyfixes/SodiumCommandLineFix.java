package com.celestered.earlyfixes;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * On NVIDIA + Windows, Sodium overwrites the process command line with
 * {@code "net.caffeinemc.sodium / net.minecraft.client.main.Main /"} so the driver stops
 * applying its Minecraft profile. Relauncher reads that same command line (GetCommandLineW)
 * to restart the game, so without this fix the restarted JVM gets garbage arguments.
 * <p>
 * Sodium's change happens while FML loads the early window, which is before any
 * transformation service's {@code onLoad}. Relauncher restarts the game in its
 * {@code initialize}, and ModLauncher always runs every {@code onLoad} before any
 * {@code initialize}, so restoring the command line here always wins the race.
 * The relaunched game applies Sodium's workaround again on its own.
 */
public final class SodiumCommandLineFix implements ITransformationService {
    private static final String SODIUM_COMMAND_LINE = "net.caffeinemc.mods.sodium.client.platform.windows.WindowsCommandLine";
    private static final String EARLY_WINDOW_HANDLER = "net.neoforged.fml.loading.ImmediateWindowHandler";
    private static final long EARLY_WINDOW_TIMEOUT_SECONDS = 15;

    private boolean done;

    @Override
    public String name() {
        return "celeste_early_fixes";
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        run();
    }

    @Override
    public void initialize(IEnvironment environment) {
        run();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<? extends ITransformer<?>> transformers() {
        return Collections.emptyList();
    }

    private void run() {
        if (done) return;
        done = true;
        try {
            if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) return;
            // Inside the relaunched game Sodium's workaround must stay in place.
            if (Boolean.getBoolean("relauncher.didRelaunch")) return;
            if (!Settings.load().restoreSodiumCommandLine) return;

            Class<?> commandLine = findClass(SODIUM_COMMAND_LINE);
            if (commandLine == null) return; // Sodium not installed
            Field hook = commandLine.getDeclaredField("ACTIVE_COMMAND_LINE_HOOK");
            hook.setAccessible(true);
            if (hook.get(null) == null) return; // Sodium didn't touch the command line (not NVIDIA)

            // Let the early window finish creating its OpenGL context first. The NVIDIA driver
            // reads the command line at that moment, which is the whole point of Sodium's change.
            waitForEarlyWindow();
            commandLine.getMethod("resetCommandLine").invoke(null);
            Log.info("Restored the command line Sodium replaced for its NVIDIA workaround, so Relauncher sees the real launch arguments");
        } catch (Throwable t) {
            Log.warn("Could not restore Sodium's command line: " + t);
        }
    }

    private static void waitForEarlyWindow() {
        try {
            Class<?> handler = findClass(EARLY_WINDOW_HANDLER);
            if (handler == null) return;
            Field providerField = handler.getDeclaredField("provider");
            providerField.setAccessible(true);
            Object provider = providerField.get(null);
            if (provider == null) return;
            for (Class<?> c = provider.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || !Future.class.isAssignableFrom(f.getType())) continue;
                    f.setAccessible(true);
                    Object future = f.get(provider);
                    if (future instanceof Future) {
                        ((Future<?>) future).get(EARLY_WINDOW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    }
                }
            }
        } catch (Throwable t) {
            // A missing or slow early window just means we restore a little earlier.
        }
    }

    private static Class<?> findClass(String name) {
        ClassLoader[] loaders = {
            SodiumCommandLineFix.class.getClassLoader(),
            Thread.currentThread().getContextClassLoader(),
            ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader cl : loaders) {
            if (cl == null) continue;
            try {
                return Class.forName(name, false, cl);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
