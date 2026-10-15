import java.awt.image.BufferedImage;

public class BattleBarScanner {

    private final ScreenSource reader;
    private final ImageLoader loader;
    private final TemplateMatcher matcher;

    public BattleBarScanner(ScreenSource screen) {
        this.reader = screen;
        this.loader = new ImageLoader();
        this.matcher = new TemplateMatcher();
    }

    public Region findSlot(String templatePath, double threshold) {
        BufferedImage screen = reader.capture(Regions.BATTLE_BAR);
        BufferedImage template = loader.load(templatePath);

        MatchResult result = matcher.find(screen, template, threshold);

        System.out.println(templatePath + " similarity: " + result.similarity);

        if (!result.found) {
            return null;
        }

        int x = Regions.BATTLE_BAR.x + result.x;
        int y = Regions.BATTLE_BAR.y + result.y;

        return new Region(x, y, template.getWidth(), template.getHeight());
    }

    public BattleLayout scanAll() {
        BattleLayout layout = new BattleLayout();

        layout.king = findSlot("images/KING.png", 0.70);
        layout.queen = findSlot("images/QUEEN.png", 0.70);
        layout.warden = findSlot("images/WARDEN.png", 0.70);
        layout.champion = findSlot("images/CHAMPION.png", 0.70);
        layout.DRAGONDUKE = findSlot("images/DRAGONDUKE.png", 0.70);
        layout.MINIONPRINCE = findSlot("images/MINIONPRINCE.png", 0.70);

        layout.valkyrie = findSlot("images/VALKYRIE.png", 0.70);
        layout.jumpSpell = findSlot("images/JUMP_SPELL.png", 0.70);
        layout.dragon = findSlot("images/DRAGON.png", 0.70);
        layout.wizard = findSlot("images/WIZARD.png", 0.70);
        layout.barbarian = findSlot("images/BARBARIAN.png", 0.70);
        layout.SNEAKY_GOBLIN = findSlot("images/SNEAKY_GOBLIN.png", 0.70);



        return layout;
    }
}