package com.celestered.earlyfixes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Laptops with integrated + dedicated graphics often run Java on the weak integrated GPU.
 * Windows lets each program pick a GPU under Settings > Display > Graphics; that choice is the
 * per-user registry value {@code HKCU\Software\Microsoft\DirectX\UserGpuPreferences\<exe path>}.
 * <p>
 * We set "high performance" for the Java executable Relauncher is about to restart, but only
 * when the machine really has both kinds of GPU and the player hasn't picked anything for that
 * executable yet. Runs before the restart, so the restarted game already gets the dedicated GPU.
 */
final class GpuPreference {
    private static final String KEY = "HKCU\\Software\\Microsoft\\DirectX\\UserGpuPreferences";
    private static final String HIGH_PERFORMANCE = "GpuPreference=2;";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    private static final Pattern IGNORE = Pattern.compile(
        "(?i).*(microsoft basic|remote|virtual|parsec|displaylink|spacedesk|indirect|idd|citrix|vmware|hyper-v).*");
    // Checked first: AMD APU names like "Radeon RX Vega 11 Graphics" and Intel's iGPU "Arc(TM) Graphics"
    // (no model number) would otherwise look discrete.
    private static final Pattern INTEGRATED = Pattern.compile(
        "(?i).*(intel.*(uhd|hd graphics|iris)|intel(\\(r\\))?\\s*arc(\\(tm\\))?\\s*graphics|radeon\\s*(\\(tm\\)\\s*)?graphics"
            + "|radeon\\s*\\d{3,4}m|vega\\s*\\d+\\s*graphics).*");
    private static final Pattern DISCRETE = Pattern.compile(
        "(?i).*(nvidia|geforce|quadro|rtx|gtx|radeon\\s*(rx|pro|vii|r9|r7)|intel.*arc).*");

    private GpuPreference() {
    }

    static void apply() {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) return;
        String exe = ProcessHandle.current().info().command().orElse(null);
        if (exe == null) return;

        if (run(Duration.ofSeconds(5), "reg", "query", KEY, "/v", exe).exitCode == 0) {
            Log.info("Windows already has a GPU choice for " + exe + ", leaving it alone");
            return;
        }

        List<String> gpus = detectGpus();
        boolean discrete = false;
        boolean integrated = false;
        for (String gpu : gpus) {
            if (IGNORE.matcher(gpu).matches()) continue;
            if (INTEGRATED.matcher(gpu).matches()) integrated = true;
            else if (DISCRETE.matcher(gpu).matches()) discrete = true;
        }
        if (!(discrete && integrated)) {
            Log.info("Single-GPU machine " + gpus + ", no GPU preference needed");
            return;
        }

        Result r = run(Duration.ofSeconds(5), "reg", "add", KEY, "/v", exe, "/t", "REG_SZ", "/d", HIGH_PERFORMANCE, "/f");
        if (r.exitCode == 0) {
            Log.info("Hybrid graphics " + gpus + ": told Windows to run " + exe + " on the dedicated GPU "
                + "(change it under Settings > Display > Graphics)");
        } else {
            Log.warn("Could not set the GPU preference: " + r.output.trim());
        }
    }

    /** Video controller names, cached for a week because asking Windows takes a second or two. */
    private static List<String> detectGpus() {
        Path cache = Settings.dataDir().resolve("gpus.txt");
        try {
            if (Files.isRegularFile(cache)) {
                FileTime modified = Files.getLastModifiedTime(cache);
                if (modified.toInstant().plus(CACHE_TTL).isAfter(Instant.now())) {
                    return clean(Files.readAllLines(cache, StandardCharsets.UTF_8));
                }
            }
        } catch (Throwable ignored) {
        }

        Result r = run(Duration.ofSeconds(10), "powershell", "-NoProfile", "-NonInteractive", "-Command",
            "Get-CimInstance Win32_VideoController | ForEach-Object { $_.Name }");
        List<String> gpus = r.exitCode == 0 ? clean(Arrays.asList(r.output.split("\\R"))) : new ArrayList<>();
        if (!gpus.isEmpty()) {
            try {
                Files.createDirectories(cache.getParent());
                Files.write(cache, gpus, StandardCharsets.UTF_8);
            } catch (Throwable ignored) {
            }
        }
        return gpus;
    }

    private static List<String> clean(List<String> lines) {
        List<String> out = new ArrayList<>();
        for (String l : lines) {
            String t = l.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private static final class Result {
        final int exitCode;
        final String output;

        Result(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }

    private static Result run(Duration timeout, String... command) {
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
            p.getOutputStream().close();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Thread reader = new Thread(() -> {
                try (InputStream in = p.getInputStream()) {
                    in.transferTo(buf);
                } catch (Throwable ignored) {
                }
            }, "celeste-early-fixes-reader");
            reader.setDaemon(true);
            reader.start();
            if (!p.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                return new Result(-1, "timed out");
            }
            reader.join(1000);
            return new Result(p.exitValue(), buf.toString(StandardCharsets.UTF_8));
        } catch (Throwable t) {
            return new Result(-1, String.valueOf(t));
        }
    }
}
