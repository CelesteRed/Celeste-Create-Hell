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
 * Runs in ModLauncher's {@code onLoad}, which always happens before any service's
 * {@code initialize} (where Relauncher would normally restart the game). In order:
 * <ol>
 *   <li><b>Sodium + NVIDIA fix.</b> On NVIDIA + Windows, Sodium overwrites the process command line
 *   with {@code "net.caffeinemc.sodium / net.minecraft.client.main.Main /"} so the driver stops
 *   applying its Minecraft profile. Relauncher reads that same command line (GetCommandLineW) to
 *   restart the game, so we put the real one back first. The restarted game applies Sodium's
 *   workaround again on its own.</li>
 *   <li><b>Restart through Relauncher ourselves</b> ({@link RelaunchNow}), with Relauncher's config
 *   arguments (the unsup javaagent) plus {@link LaunchTuning}. We deliberately don't register
 *   LaunchTuning as a Relauncher SPI service: Relauncher would load it through a URLClassLoader,
 *   which on Windows keeps this jar locked for the whole session (the original process lingers
 *   while the restarted game runs), so unsup could never update or delete it.</li>
 * </ol>
 */
public final class EarlyFixesService implements ITransformationService {
    private static final String SODIUM_COMMAND_LINE = "net.caffeinemc.mods.sodium.client.platform.windows.WindowsCommandLine";
    private static final String EARLY_WINDOW_HANDLER = "net.neoforged.fml.loading.ImmediateWindowHandler";
    private static final long EARLY_WINDOW_TIMEOUT_SECONDS = 10;

    private boolean done;

    @Override
    public String name() {
        return "celeste_early_fixes";
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        if (done) return;
        done = true;
        // Inside the restarted game there is nothing to do, and Sodium's workaround must stay in place.
        if (Boolean.getBoolean("relauncher.didRelaunch")) return;
        Settings settings = Settings.load();
        if (settings.restoreSodiumCommandLine) {
            restoreSodiumCommandLine();
        }
        try {
            RelaunchNow.run();
        } catch (Throwable t) {
            // Relauncher missing or broken: let Relauncher's own initialize() (if any) handle the restart.
            Log.warn("Could not restart through Relauncher, leaving it to Relauncher itself: " + t);
        }
    }

    @Override
    public void initialize(IEnvironment environment) {
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<? extends ITransformer<?>> transformers() {
        return Collections.emptyList();
    }

    private static void restoreSodiumCommandLine() {
        try {
            if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) return;
            Class<?> commandLine = findClass(SODIUM_COMMAND_LINE);
            if (commandLine == null) return; // Sodium not installed
            Field hook = commandLine.getDeclaredField("ACTIVE_COMMAND_LINE_HOOK");
            hook.setAccessible(true);
            if (hook.get(null) == null) return; // Sodium didn't touch the command line (not NVIDIA)

            // Let the early window finish creating its OpenGL context first. The NVIDIA driver
            // reads the command line at that moment, which is the whole point of Sodium's change.
            long start = System.nanoTime();
            waitForEarlyWindow();
            commandLine.getMethod("resetCommandLine").invoke(null);
            Log.info("Restored the command line Sodium replaced for its NVIDIA workaround, so Relauncher sees the real "
                + "launch arguments (waited " + (System.nanoTime() - start) / 1_000_000 + " ms for the early window)");
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
            // Only the one-shot init future: FML's window also holds periodic "tick" futures that
            // never complete, and waiting on those would just burn the whole timeout.
            for (Class<?> c = provider.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                Field f;
                try {
                    f = c.getDeclaredField("initializationFuture");
                } catch (NoSuchFieldException e) {
                    continue;
                }
                if (Modifier.isStatic(f.getModifiers()) || !Future.class.isAssignableFrom(f.getType())) return;
                f.setAccessible(true);
                Object future = f.get(provider);
                if (future instanceof Future) {
                    ((Future<?>) future).get(EARLY_WINDOW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
                return;
            }
        } catch (Throwable t) {
            // A missing or slow early window just means we restore a little earlier.
        }
    }

    private static Class<?> findClass(String name) {
        ClassLoader[] loaders = {
            EarlyFixesService.class.getClassLoader(),
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
