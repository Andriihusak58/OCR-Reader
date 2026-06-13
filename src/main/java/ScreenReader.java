import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class ScreenReader {
    private final Robot robot = new Robot();

    public ScreenReader() throws AWTException {
    }

    public BufferedImage capture(Region region) {
        Rectangle rect = new Rectangle(region.x, region.y, region.width, region.height);
        return this.robot.createScreenCapture(rect);
    }
}