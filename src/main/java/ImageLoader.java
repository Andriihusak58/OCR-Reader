import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoader {
    public ImageLoader() {
    }

    public BufferedImage load(String fileNme) {
        try {
            return ImageIO.read(new File(fileNme));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load image: " + fileNme, e);
        }
    }
}