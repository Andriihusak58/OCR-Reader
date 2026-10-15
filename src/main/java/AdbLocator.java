import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helps the user avoid manual setup: finds adb.exe in the usual LDPlayer / Android SDK
 * locations and lists the devices that adb currently sees.
 */
public final class AdbLocator {

    private AdbLocator() {
    }

    /** Looks for adb.exe in typical install folders. Returns its absolute path or null. */
    public static String findAdb() {
        List<File> candidates = new ArrayList<>();

        for (File root : File.listRoots()) {
            addLdPlayerDirs(candidates, root);
            addLdPlayerDirs(candidates, new File(root, "Program Files"));
            addLdPlayerDirs(candidates, new File(root, "Program Files (x86)"));
        }

        String local = System.getenv("LOCALAPPDATA");
        if (local != null) {
            candidates.add(new File(local, "Android\\Sdk\\platform-tools\\adb.exe"));
        }
        String sdk = System.getenv("ANDROID_HOME");
        if (sdk != null) {
            candidates.add(new File(sdk, "platform-tools\\adb.exe"));
        }

        for (File f : candidates) {
            if (f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    private static void addLdPlayerDirs(List<File> out, File base) {
        // newest first: LDPlayer14, LDPlayer9, ...
        File[] dirs = base.listFiles(f -> f.isDirectory() && f.getName().toLowerCase().startsWith("ldplayer"));
        if (dirs == null) {
            return;
        }
        java.util.Arrays.sort(dirs, (a, b) -> b.getName().compareToIgnoreCase(a.getName()));
        for (File d : dirs) {
            out.add(new File(d, "adb.exe"));
            // e.g. C:\LDPlayer\LDPlayer14 may itself sit inside C:\LDPlayer
            File[] sub = d.listFiles(f -> f.isDirectory() && f.getName().toLowerCase().startsWith("ldplayer"));
            if (sub != null) {
                for (File s : sub) {
                    out.add(new File(s, "adb.exe"));
                }
            }
        }
    }

    /** Runs "adb devices" and returns serials in state "device". Empty list on any failure. */
    public static List<String> listDevices(String adbPath) {
        List<String> serials = new ArrayList<>();
        try {
            Process p = new ProcessBuilder(adbPath, "devices").redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!p.waitFor(10, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return serials;
            }
            for (String line : out.split("\\R")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2 && parts[1].equals("device")) {
                    serials.add(parts[0]);
                }
            }
        } catch (IOException e) {
            // adb not found / cannot start
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return serials;
    }
}
