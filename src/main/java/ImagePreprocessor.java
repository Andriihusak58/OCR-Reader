import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePreprocessor {

    public BufferedImage preprocess(BufferedImage original) {
        BufferedImage scaled = scale(original, 3);
        BufferedImage masked = extractWhiteText(scaled);
        return masked;
    }

    private BufferedImage scale(BufferedImage src, int factor) {
        int w = src.getWidth() * factor;
        int h = src.getHeight() * factor;

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();

        return result;
    }

    private BufferedImage extractWhiteText(BufferedImage src) {
        BufferedImage result = new BufferedImage(
                src.getWidth(),
                src.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                boolean isWhiteText = r > 200 && g > 200 && b > 200;

                if (isWhiteText) {
                    result.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    result.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }

        return result;
    }
}