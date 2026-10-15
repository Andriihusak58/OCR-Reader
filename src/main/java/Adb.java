import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

        Process process = new ProcessBuilder(cmd)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();

        ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 20);
        try (InputStream in = process.getInputStream()) {
            in.transferTo(out);
        }

        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IOException("adb timeout: " + String.join(" ", cmd));
        }
        if (process.exitValue() != 0) {
            throw new IOException("adb failed (exit " + process.exitValue() + "): "
                    + String.join(" ", cmd) + " — чи запущений емулятор і виконано 'adb connect'?");
        }
        return out.toByteArray();
    }
}
