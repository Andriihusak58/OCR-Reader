import java.awt.image.BufferedImage;

public class ButtonReader {
    public ButtonReader() {
    }

    public static void main(String[] args) {
        try {
            ScreenReader reader = new ScreenReader();
            ImagineServer saver = new ImagineServer();
            System.out.println("1 Sekunde...");
            Thread.sleep(1000L);
            BufferedImage image = reader.capture(Regions.MY_ELIXIR);
            saver.save(image, "MY_ELIXIR.png");
            System.out.println("Fertig!");
        } catch (Exception e) {
        e.printStackTrace();
    }

    }
}