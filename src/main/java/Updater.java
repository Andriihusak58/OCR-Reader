import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Self-update: downloads the new BotOCR.jar and images.zip from the GitHub release, then a small
 * batch script swaps the jar after the program has exited (a running jar is locked on Windows)
 * and starts the program again. The bundled JRE, tessdata and the user's settings are not touched.
 */
public final class Updater {

    static final String ALLOWED_PREFIX = "https://github.com/Andriihusak58/OCR-Reader/releases/download/";
    private static final long MAX_BYTES = 200L * 1024 * 1024;
    static final String JAR_NAME = "BotOCR.jar";

    public interface Progress {
        void say(String message);
    }

    private Updater() {
    }

    /** The jar this program runs from, or null when started from an IDE / classes folder. */
    static Path runningJar() {
        try {
            Path p = Path.of(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isRegularFile(p) && p.getFileName().toString().endsWith(".jar") ? p : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Self-update only works for the release layout: BotOCR.jar (+ BotOCR.exe) in the program folder. */
    public static boolean canSelfUpdate() {
        Path jar = runningJar();
        return jar != null && jar.getFileName().toString().equals(JAR_NAME);
    }

    /** Downloads and verifies everything. Afterwards call {@link #launchSwap(Path)} and exit. */
    public static void stage(Path appDir, String jarUrl, String imagesUrl, String allowedPrefix, Progress progress)
            throws Exception {
        if (jarUrl == null || !jarUrl.startsWith(allowedPrefix)) {
            throw new IllegalStateException("Unexpected download address: " + jarUrl);
        }
        if (imagesUrl != null && !imagesUrl.startsWith(allowedPrefix)) {
            throw new IllegalStateException("Unexpected download address: " + imagesUrl);
        }

        Path newJar = appDir.resolve(JAR_NAME + ".new");
        Path zip = appDir.resolve("images.zip.tmp");
        try {
            progress.say("Downloading " + JAR_NAME + "...");
            download(jarUrl, newJar, progress);
            verifyJar(newJar);

            if (imagesUrl != null) {
                progress.say("Downloading images.zip...");
                download(imagesUrl, zip, progress);
                progress.say("Updating images...");
                extractImages(zip, appDir.resolve("images"));
            }
        } catch (Exception e) {
            Files.deleteIfExists(newJar);
            throw e;
        } finally {
            Files.deleteIfExists(zip);
        }
        progress.say("Update downloaded. Restarting...");
    }

    /** Downloads with a few retries: some networks drop the first TLS handshake to the GitHub download hosts. */
    private static void download(String url, Path target, Progress progress) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                downloadOnce(url, target, progress);
                return;
            } catch (java.io.IOException e) {
                last = e;
                Files.deleteIfExists(target);
                if (attempt < 3) {
                    progress.say("Download failed (" + e.getMessage() + "), retrying...");
                    Thread.sleep(2000L * attempt);
                }
            }
        }
        throw last;
    }

    private static void downloadOnce(String url, Path target, Progress progress) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("User-Agent", "BotOCR-updater")
                .GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Download failed (HTTP " + response.statusCode() + "): " + url);
        }
        long total = 0;
        try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(target)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            long lastReport = 0;
            while ((n = in.read(buf)) > 0) {
                total += n;
                if (total > MAX_BYTES) throw new IllegalStateException("Download is too large");
                out.write(buf, 0, n);
                if (total - lastReport >= 5L * 1024 * 1024) {
                    lastReport = total;
                    progress.say("  " + (total / (1024 * 1024)) + " MB");
                }
            }
        }
        if (total == 0) throw new IllegalStateException("Downloaded file is empty: " + url);
    }

    /** The new jar must be a real jar that starts the GUI, otherwise we would brick the installation. */
    static void verifyJar(Path jar) throws Exception {
        try (JarFile jf = new JarFile(jar.toFile())) {
            if (jf.getManifest() == null
                    || !"BotGui".equals(jf.getManifest().getMainAttributes().getValue("Main-Class"))
                    || jf.getEntry("BotGui.class") == null) {
                throw new IllegalStateException("Downloaded jar is not a valid BotOCR build");
            }
        }
    }

    /** Extracts "images/..." entries of the zip into imagesDir (existing files are replaced, extras kept). */
    static void extractImages(Path zip, Path imagesDir) throws Exception {
        Files.createDirectories(imagesDir);
        Path root = imagesDir.toAbsolutePath().normalize();
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("images/")) name = name.substring("images/".length());
                Path target = root.resolve(name).normalize();
                if (!target.startsWith(root)) {
                    throw new IllegalStateException("Unsafe path in images.zip: " + entry.getName());
                }
                Files.createDirectories(target.getParent());
                Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /** Text of the swap script (ASCII only; paths are relative to the program folder). */
    static String swapScript(boolean hasExe) {
        String start = hasExe ? "start \"\" \"BotOCR.exe\"" : "start \"\" javaw -jar \"BotOCR.jar\"";
        return String.join("\r\n",
                "@echo off",
                "cd /d \"%~dp0\"",
                "set tries=0",
                ":retry",
                "ping -n 2 127.0.0.1 >nul",
                "move /y \"BotOCR.jar.new\" \"BotOCR.jar\" >nul 2>&1",
                "if not errorlevel 1 goto done",
                "set /a tries+=1",
                "if %tries% lss 30 goto retry",
                "echo Update failed: BotOCR.jar could not be replaced. The old version is kept.",
                "pause",
                "exit /b 1",
                ":done",
                start,
                "(goto) 2>nul & del \"%~f0\"",
                "");
    }

    /** Removes the BotOCR.jar.bak left by older versions of the updater. */
    public static void deleteOldBackup() {
        try {
            Path jar = runningJar();
            if (jar != null) Files.deleteIfExists(jar.resolveSibling("BotOCR.jar.bak"));
        } catch (Exception ignored) {
        }
    }

    /** Starts the swap script as a separate process. The caller must exit the JVM right after. */
    public static void launchSwap(Path appDir) throws Exception {
        Path script = appDir.resolve("update.bat");
        Files.writeString(script, swapScript(Files.exists(appDir.resolve("BotOCR.exe"))), Charset.defaultCharset());
        new ProcessBuilder("cmd.exe", "/c", script.toString())
                .directory(appDir.toFile())
                .start();
    }
}
