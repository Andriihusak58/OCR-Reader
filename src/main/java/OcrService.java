import java.awt.image.BufferedImage;

public class OcrService {

    private final ScreenSource screenReader;
    private final TextReader textReader;
    private final ImagineServer saver;

    public OcrService(ScreenSource screen) {
        this.screenReader = screen;
        this.textReader = new TextReader();
        this.saver = new ImagineServer();
    }

    public String readDigitsOnly(Region region) {
        BufferedImage image = screenReader.capture(region);
        return textReader.readDigitsOnly(image);
    }

    public String readDigitsOnly(Region region, String saveFileName) {
        BufferedImage image = screenReader.capture(region);
        saver.save(image, saveFileName);
        return textReader.readDigitsOnly(image);
    }

    public int readDigitsAsInt(Region region) {
        String text = readDigitsOnly(region);

        if (text.isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_DEBUG_FILES = 30;

    /**
     * Читає число з регіону. Кожна спроба робить НОВИЙ знімок, бо випадкова невдача
     * зазвичай через момент кадру (панель ще з'являється, число анімується), а не через OCR.
     * Якщо всі спроби невдалі, останній кадр зберігається в ocr_fails/ для розбору.
     */
    public int readStableDigitsAsInt(Region region) {
        BufferedImage image = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            image = screenReader.capture(region);
            String text = textReader.readDigitsOnly(image);

            if (!text.isEmpty()) {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException ignored) {
                    // надто довге число -> шум, пробуємо ще раз
                }
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        saveFailedImage(image, region);
        return -1;
    }

    private void saveFailedImage(BufferedImage image, Region region) {
        if (image == null) {
            return;
        }

        java.io.File dir = new java.io.File("ocr_fails");
        dir.mkdirs();

        String[] existing = dir.list();
        if (existing != null && existing.length >= MAX_DEBUG_FILES) {
            return;
        }

        saver.save(image, "ocr_fails/fail_" + region.x + "_" + region.y + "_" + System.currentTimeMillis() + ".png");
    }
}