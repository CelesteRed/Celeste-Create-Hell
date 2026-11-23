package com.celestered.earlyfixes;

import com.juanmuscaria.relauncher.CommandLineProvider;
import com.juanmuscaria.relauncher.RelaunchResult;
import com.juanmuscaria.relauncher.Relauncher;
import com.juanmuscaria.relauncher.RelauncherConfig;
import com.juanmuscaria.relauncher.jvm.JavaRuntimeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Does what Relauncher's own entry point would do (read {@code config/relauncher/config.cfg} and
 * restart), plus our per-machine arguments. Kept in its own class so a missing Relauncher only
 * fails here, as a catchable NoClassDefFoundError.
 */
final class RelaunchNow {
    private RelaunchNow() {
    }

    static void run() throws Exception {
        if (Relauncher.isRelaunched()) return;

        RelauncherConfig config = RelauncherConfig.load(Settings.gameDir().resolve("config").resolve("relauncher"));
        List<CommandLineProvider> argProviders = new ArrayList<>();
        if (config.isEnabled() && !config.getExtraJvmArgs().isEmpty()) {
            List<String> configArgs = Collections.unmodifiableList(new ArrayList<>(config.getExtraJvmArgs()));
            argProviders.add(new CommandLineProvider() {
                @Override
                public List<String> extraJvmArguments() {
                    return configArgs;
                }

                @Override
                public int priority() {
                    return Integer.MIN_VALUE;
                }
            });
        }
        argProviders.add(new LaunchTuning());

        List<JavaRuntimeProvider> javaProviders = new ArrayList<>();
        JavaRuntimeProvider javaFromConfig = config.toJavaRuntimeProvider();
        if (javaFromConfig != null) javaProviders.add(javaFromConfig);

        Log.info("Restarting the game through Relauncher with the pack updater and Celeste's launch tweaks");
        // Only returns if the restart didn't happen.
        RelaunchResult result = Relauncher.relaunch(argProviders, javaProviders);
        if (result.isFailed()) {
            Log.warn("Relauncher could not restart the game: " + result.reason()
                + (result.cause() != null ? " (" + result.cause() + ")" : ""));
        } else if (result.isSkipped()) {
            Log.info("Relauncher skipped the restart: " + result.reason());
        }
    }
}
