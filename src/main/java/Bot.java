import java.awt.image.BufferedImage;

public class Bot {

    private final ScreenSource reader;
    private final Actions actions;
    private final ImageLoader loader;
    private final TemplateMatcher matcher;

    public Bot(GameContext ctx) {
        this.reader = ctx.screen;
        this.actions = ctx.actions;
        this.loader = new ImageLoader();
        this.matcher = new TemplateMatcher();
    }

    public boolean pressIfFound(UiElement element) throws InterruptedException {

        BufferedImage screen = reader.capture(element.region);
        BufferedImage template = loader.load(element.templatePath);

        MatchResult result = matcher.find(screen, template, element.threshold);

        System.out.println("Similarity: " + result.similarity);

        if (result.found) {
            actions.click(element.region);
            System.out.println("Pressed");
            return true;
        }

        System.out.println("Not found");
        return false;
    }
}
