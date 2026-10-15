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

    private BufferedImage fullFrame() {
        try {
            byte[] png = adb.exec("exec-out", "screencap", "-p");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
            if (image == null) {
                throw new IOException("adb screencap returned no image");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("ADB screenshot failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ADB screenshot interrupted", e);
        }
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
