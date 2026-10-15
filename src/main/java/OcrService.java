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

    public int readStableDigitsAsInt(Region region) {
        BufferedImage image = screenReader.capture(region);
        String text = textReader.readStableDigits(image, 5);

        if (text.isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}