import java.awt.image.BufferedImage;

public class Bot {

    private final ScreenReader reader;
    private final ImageLoader loader;
    private final TemplateMatcher matcher;

    public Bot() throws Exception {
        this.reader = new ScreenReader();
        this.loader = new ImageLoader();
        this.matcher = new TemplateMatcher();
    }

    public boolean pressIfFound(UiElement element) throws InterruptedException {

        BufferedImage screen = reader.capture(element.region);
        BufferedImage template = loader.load(element.templatePath);

        MatchResult result = matcher.find(screen, template, element.threshold);

        System.out.println("Similarity: " + result.similarity);

        if (result.found) {
            Actions.click(element.region);
            System.out.println("Pressed");
            return true;
        }

        System.out.println("Not found");
        return false;
    }
}