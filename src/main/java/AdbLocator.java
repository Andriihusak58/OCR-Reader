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

    /** LDPlayer exposes instance i on 127.0.0.1:(5555 + 2*i). Tries the first few; refused ports fail instantly. */
    public static void connectLocal(String adbPath) {
        for (int port = 5555; port <= 5561; port += 2) {
            try {
                Process p = new ProcessBuilder(adbPath, "connect", "127.0.0.1:" + port)
                        .redirectErrorStream(true).start();
                p.getInputStream().readAllBytes();
                if (!p.waitFor(5, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }
            } catch (IOException e) {
                return; // adb cannot be started at all - nothing else to try
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Returns the serial to use: the requested one if adb sees it, otherwise the first connected device.
     * If adb sees nothing, tries "adb connect" to the usual LDPlayer ports first.
     */
    public static String resolveSerial(String adbPath, String requested) {
        List<String> devices = listDevices(adbPath);
        if (devices.contains(requested)) {
            return requested;
        }
        if (devices.isEmpty()) {
            System.out.println("ADB sees no devices, trying to connect to LDPlayer...");
            connectLocal(adbPath);
            devices = listDevices(adbPath);
        }
        if (!devices.isEmpty()) {
            String chosen = devices.get(0);
            if (!chosen.equals(requested)) {
                System.out.println("ADB device '" + requested + "' not found, using '" + chosen + "'");
            }
            return chosen;
        }
        return requested;
    }
}
