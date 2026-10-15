import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Пошук шаблону на зображенні через OpenCV (matchTemplate, TM_CCOEFF_NORMED).
 *
 * Схожість: чим ближче до 1.0, тим кращий збіг. Правильна кнопка дає ~0.95+,
 * схожий, але інший елемент зазвичай нижче 0.6.
 */
public class TemplateMatcher {

    static {
        OpenCV.loadLocally();
    }

    public TemplateMatcher() {
    }

    public MatchResult find(BufferedImage screen, BufferedImage template, double threshold) {
        if (template.getWidth() > screen.getWidth() || template.getHeight() > screen.getHeight()) {
            return new MatchResult(false, -1, -1, 0.0);
        }

        Mat screenMat = toMat(screen);
        Mat templateMat = toMat(template);
        Mat result = new Mat();

        try {
            Imgproc.matchTemplate(screenMat, templateMat, result, Imgproc.TM_CCOEFF_NORMED);
            Core.MinMaxLocResult best = Core.minMaxLoc(result);

            double similarity = best.maxVal;
            // Однотонний шаблон дає ділення на нуль -> NaN/Infinity; такий збіг не вважаємо знайденим
            if (Double.isNaN(similarity) || Double.isInfinite(similarity)) {
                return new MatchResult(false, -1, -1, 0.0);
            }

            return new MatchResult(
                    similarity >= threshold,
                    (int) best.maxLoc.x,
                    (int) best.maxLoc.y,
                    similarity
            );
        } finally {
            screenMat.release();
            templateMat.release();
            result.release();
        }
    }

    // BufferedImage (будь-який тип) -> Mat у форматі BGR, який очікує OpenCV
    private static Mat toMat(BufferedImage source) {
        BufferedImage bgr = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g = bgr.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();

        byte[] data = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();

        Mat mat = new Mat(bgr.getHeight(), bgr.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }
}
