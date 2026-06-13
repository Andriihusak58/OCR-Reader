import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TextReader {

    private final ImagePreprocessor preprocessor;

    public TextReader() {
        this.preprocessor = new ImagePreprocessor();
    }

    private ITesseract createTesseract() {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("eng");
        tesseract.setVariable("tessedit_char_whitelist", "0123456789");
        tesseract.setVariable("user_defined_dpi", "300");
        tesseract.setPageSegMode(13);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }

    public String readDigitsOnly(BufferedImage image) {
        File tempFile = null;

        try {
            BufferedImage processed = preprocessor.preprocess(image);

            tempFile = File.createTempFile("ocr_", ".png");
            ImageIO.write(processed, "png", tempFile);

            ITesseract tesseract = createTesseract();
            String raw = tesseract.doOCR(tempFile);

            return raw.replaceAll("[^0-9]", "");
        } catch (Exception e) {
            throw new RuntimeException("OCR digits failed", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public String readStableDigits(BufferedImage image, int attempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                String result = readDigitsOnly(image);

                if (!result.isEmpty()) {
                    return result;
                }

                Thread.sleep(50);
            } catch (Exception ignored) {
            }
        }

        return "";
    }
}