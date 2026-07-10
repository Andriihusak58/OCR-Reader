import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImagineServer {
    public ImagineServer() {
    }

    public void save(BufferedImage image, String fileName) {
        try {
            File file = new File(fileName);
            ImageIO.write(image, "png", file);
            System.out.println("Saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}