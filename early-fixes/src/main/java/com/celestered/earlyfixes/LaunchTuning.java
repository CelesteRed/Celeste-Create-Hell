package com.celestered.earlyfixes;

import com.juanmuscaria.relauncher.CommandLineProvider;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Extra JVM arguments picked per machine, handed to Relauncher before it restarts the game.
 * <p>
 * Relauncher puts these <em>before</em> the launcher's own arguments, so anything the player
 * set in their launcher wins over us. That's why there's no memory (-Xmx) tuning here: the
 * launcher's value would override it anyway. We only add flags that can't clash, and we check
 * for clashes (two garbage collectors) ourselves, because a clash stops Java from starting.
 * <p>
 * Nothing in here may throw: an exception would abort Relauncher's restart and the game.
 */
public final class LaunchTuning implements CommandLineProvider {
    static final String VERSION = LaunchTuning.class.getPackage().getImplementationVersion() != null
        ? LaunchTuning.class.getPackage().getImplementationVersion() : "dev";

    private static final Pattern GC_FLAG = Pattern.compile("^-XX:[+](Use\\w*GC|UseShenandoahGC|UseEpsilonGC)$");
    private static final long GIB = 1024L * 1024L * 1024L;

    @Override
    public List<String> extraJvmArguments() {
        List<String> args = new ArrayList<>();
        args.add("-Dceleste.earlyfixes.version=" + VERSION);
        try {
            Settings settings = Settings.load();
            garbageCollector(settings, args);
            memoryAdvice();
            if (settings.preferDiscreteGpu) {
                GpuPreference.apply();
            }
        } catch (Throwable t) {
            Log.warn("Launch tuning failed, continuing with defaults: " + t);
        }
        Log.info("Extra JVM arguments: " + args);
        return args;
    }

    @Override
    public int priority() {
        return 100;
    }

    private static void garbageCollector(Settings settings, List<String> args) {
        String mode = settings.garbageCollector;
        if (mode.equals("off")) return;

        List<String> existing = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : existing) {
            if (GC_FLAG.matcher(arg).matches()) {
                Log.info("Garbage collector already chosen by the launcher (" + arg + "), leaving it alone");
                return;
            }
        }

        int threads = Runtime.getRuntime().availableProcessors();
        long heap = Runtime.getRuntime().maxMemory();
        long ram = totalRam();
        int java = Runtime.version().feature();
        boolean worthIt = threads >= 8 && heap >= 7_500L * 1024 * 1024 && ram >= 15L * GIB;

        if (java < 21) {
            Log.info("Java " + java + " has no Generational ZGC, keeping the default collector");
            return;
        }
        if (mode.equals("on") || (mode.equals("auto") && worthIt)) {
            args.add("-XX:+UseZGC");
            // Generational mode is opt-in on Java 21-22 and the only mode from 23 on.
            if (java < 23) args.add("-XX:+ZGenerational");
            Log.info("Using Generational ZGC (" + threads + " threads, " + gib(heap) + " heap, " + gib(ram) + " RAM)");
        } else {
            Log.info("Keeping the default collector (" + threads + " threads, " + gib(heap) + " heap, " + gib(ram)
                + " RAM; ZGC needs 8+ threads, 8 GB heap and 16 GB RAM)");
        }
    }

    private static void memoryAdvice() {
        long heap = Runtime.getRuntime().maxMemory();
        long ram = totalRam();
        if (ram <= 0) return;
        if (heap > ram * 3 / 4) {
            Log.warn("Minecraft has " + gib(heap) + " of " + gib(ram) + " RAM. Leaving Windows less than a quarter "
                + "causes stutter; lower the memory slider in your launcher.");
        } else if (heap < 6L * GIB) {
            Log.warn("Minecraft has only " + gib(heap) + ". Celeste Create Hell needs about 8 GB; raise the memory "
                + "slider in your launcher if you can.");
        }
    }

    private static long totalRam() {
        try {
            Object os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) os).getTotalMemorySize();
            }
        } catch (Throwable ignored) {
        }
        return -1;
    }

    private static String gib(long bytes) {
        return String.format("%.1f GB", bytes / (double) GIB);
    }
}
