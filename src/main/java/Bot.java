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

    /**
     * Шукає елемент в його області пошуку (або на всьому екрані, якщо область не задана)
     * і клікає в знайдене місце, а не за заздалегідь відомими координатами.
     */
    public boolean pressIfFound(UiElement element) throws InterruptedException {
        Region searchArea = element.region != null ? element.region : reader.bounds();

        BufferedImage screen = reader.capture(searchArea);
        BufferedImage template = loader.load(element.templatePath);

        MatchResult result = matcher.find(screen, template, element.threshold);

        System.out.println(element.templatePath + " similarity: " + result.similarity);

        if (!result.found) {
            System.out.println("Not found");
            return false;
        }

        actions.click(clickArea(searchArea, result, template));
        System.out.println("Pressed");
        return true;
    }

    // Центральна частина знайденого збігу в координатах екрана, щоб клік не потрапив на край кнопки
    private Region clickArea(Region searchArea, MatchResult match, BufferedImage template) {
        int insetX = template.getWidth() / 5;
        int insetY = template.getHeight() / 5;

        return new Region(
                searchArea.x + match.x + insetX,
                searchArea.y + match.y + insetY,
                Math.max(1, template.getWidth() - 2 * insetX),
                Math.max(1, template.getHeight() - 2 * insetY)
        );
    }
}
