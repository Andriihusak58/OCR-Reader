import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dev-інструмент: шукає шаблон у збереженому скріншоті й друкує позицію та оцінку збігу.
 * Так підбираються пороги для UiElements без запуску бота.
 *
 * Аргументи: <screenshot.png> <template.png> [threshold]
 */
public class MatchTest {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: MatchTest <screenshot.png> <template.png> [threshold]");
            return;
        }

        BufferedImage screen = ImageIO.read(new File(args[0]));
        BufferedImage template = ImageIO.read(new File(args[1]));
        double threshold = args.length > 2 ? Double.parseDouble(args[2]) : 0.85;

        TemplateMatcher matcher = new TemplateMatcher();

        long start = System.nanoTime();
        MatchResult result = matcher.find(screen, template, threshold);
        long ms = (System.nanoTime() - start) / 1_000_000;

        System.out.println("similarity = " + result.similarity);
        System.out.println("position   = x=" + result.x + " y=" + result.y
                + " (template " + template.getWidth() + "x" + template.getHeight() + ")");
        System.out.println("found      = " + result.found + " (threshold " + threshold + ")");
        System.out.println("time       = " + ms + " ms (перший запуск довший: завантаження OpenCV)");
    }
}
