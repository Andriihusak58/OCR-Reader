import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Тонка обгортка над командним рядком adb для одного пристрою (емулятора).
 * Потребує, щоб adb був у PATH (або вкажи повний шлях у конструкторі).
 */
public class Adb {

    private final String adbPath;
    private final String serial;

    public Adb(String serial) {
        this("adb", serial);
    }

    public Adb(String adbPath, String serial) {
        this.adbPath = adbPath;
        this.serial = serial;
    }

    /** Виконує команду adb і повертає її stdout як байти (для скріншотів). */
    public byte[] exec(String... args) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(adbPath);
        cmd.add("-s");
        cmd.add(serial);
        cmd.addAll(List.of(args));

        Process process = new ProcessBuilder(cmd).start();
        try {
            // stderr читаємо паралельно, щоб показати користувачу справжню причину помилки
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            Thread errReader = new Thread(() -> {
                try (InputStream e = process.getErrorStream()) {
                    e.transferTo(err);
                } catch (IOException ignored) {
                }
            }, "adb-stderr");
            errReader.setDaemon(true);
            errReader.start();

            ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
            try (InputStream in = process.getInputStream()) {
                in.transferTo(out);
            }

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                throw new IOException("adb timeout: " + String.join(" ", cmd));
            }
            errReader.join(1000);

            if (process.exitValue() != 0) {
                String detail = err.toString(StandardCharsets.UTF_8).trim();
                if (detail.isEmpty() && out.size() > 0 && out.size() < 400) {
                    detail = out.toString(StandardCharsets.UTF_8).trim();
                }
                throw new IOException("adb failed (exit " + process.exitValue() + "): "
                        + String.join(" ", cmd)
                        + (detail.isEmpty() ? "" : " - " + detail)
                        + ". Is LDPlayer running with ADB debugging enabled?");
            }
            return out.toByteArray();
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
