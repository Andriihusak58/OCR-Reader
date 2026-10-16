import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/** Скріншоти з Android-емулятора через "adb exec-out screencap -p". Не залежить від монітора ПК. */
public class AdbScreenSource implements ScreenSource {

    private final Adb adb;
    private Region cachedBounds;

    public AdbScreenSource(Adb adb) {
        this.adb = adb;
    }

    private static final int ATTEMPTS = 3;

    private BufferedImage fullFrame() {
        IOException last = null;
        try {
            for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
                try {
                    byte[] png = adb.exec("exec-out", "screencap", "-p");
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
                    if (image == null) {
                        throw new IOException("adb screencap returned no image");
                    }
                    return image;
                } catch (IOException e) {
                    last = e;
                    if (attempt < ATTEMPTS) {
                        System.out.println("ADB screenshot failed (attempt " + attempt + "/" + ATTEMPTS
                                + "), retrying: " + e.getMessage());
                        Thread.sleep(700);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ADB screenshot interrupted", e);
        }
        throw new IllegalStateException("ADB screenshot failed: " + last.getMessage(), last);
    }

    @Override
    public BufferedImage capture(Region region) {
        BufferedImage frame = fullFrame();
        if (cachedBounds == null) {
            cachedBounds = new Region(0, 0, frame.getWidth(), frame.getHeight());
        }

        int x = Math.max(0, region.x);
        int y = Math.max(0, region.y);
        int w = Math.min(region.width, frame.getWidth() - x);
        int h = Math.min(region.height, frame.getHeight() - y);
        if (w <= 0 || h <= 0) {
            throw new IllegalStateException("Region " + region.x + "," + region.y + " " + region.width + "x"
                    + region.height + " is outside the " + frame.getWidth() + "x" + frame.getHeight()
                    + " emulator screen. The emulator must run at 1920x1080 "
                    + "(LDPlayer > Settings > Game settings > Tablet 1920x1080)");
        }

        // копія, щоб підзображення не тримало весь кадр у пам'яті
        BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        copy.getGraphics().drawImage(frame.getSubimage(x, y, w, h), 0, 0, null);
        return copy;
    }

    @Override
    public Region bounds() {
        if (cachedBounds == null) {
            BufferedImage frame = fullFrame();
            cachedBounds = new Region(0, 0, frame.getWidth(), frame.getHeight());
        }
        return cachedBounds;
    }
}
