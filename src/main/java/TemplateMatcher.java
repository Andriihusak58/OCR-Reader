import java.awt.image.BufferedImage;

public class TemplateMatcher {
    public TemplateMatcher() {
    }

    public MatchResult find(BufferedImage screen, BufferedImage template, double threshold) {
        int screenWidth = screen.getWidth();
        int screenHeight = screen.getHeight();
        int templateWidth = template.getWidth();
        int templateHeight = template.getHeight();
        double bestSimilarity = (double)0.0F;
        int bestX = -1;
        int bestY = -1;

        for(int y = 0; y <= screenHeight - templateHeight; ++y) {
            for(int x = 0; x <= screenWidth - templateWidth; ++x) {
                double similarity = this.calculateSimilarity(screen, template, x, y);
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        boolean found = bestSimilarity >= threshold;
        return new MatchResult(found, bestX, bestY, bestSimilarity);
    }

    private double calculateSimilarity(BufferedImage screen, BufferedImage template, int startX, int startY) {
        int matchedPixels = 0;
        int totalPixels = template.getWidth() * template.getHeight();

        for(int y = 0; y < template.getHeight(); ++y) {
            for(int x = 0; x < template.getWidth(); ++x) {
                int screenRgb = screen.getRGB(startX + x, startY + y);
                int templateRgb = template.getRGB(x, y);
                if (this.isSimilarColor(screenRgb, templateRgb, 20)) {
                    ++matchedPixels;
                }
            }
        }

        return (double)matchedPixels / (double)totalPixels;
    }

    private boolean isSimilarColor(int rgb1, int rgb2, int tolerance) {
        int r1 = rgb1 >> 16 & 255;
        int g1 = rgb1 >> 8 & 255;
        int b1 = rgb1 & 255;
        int r2 = rgb2 >> 16 & 255;
        int g2 = rgb2 >> 8 & 255;
        int b2 = rgb2 & 255;
        return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance && Math.abs(b1 - b2) <= tolerance;
    }
}
