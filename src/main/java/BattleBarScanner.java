import java.awt.image.BufferedImage;

public class BattleBarScanner {

    // Іконки на панелі мають змінні цифри кількості, тому поріг трохи нижчий за DEFAULT_THRESHOLD
    private static final double SLOT_THRESHOLD = 0.80;

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

        layout.king = findSlot("images/KING.png", SLOT_THRESHOLD);
        layout.queen = findSlot("images/QUEEN.png", SLOT_THRESHOLD);
        layout.warden = findSlot("images/WARDEN.png", SLOT_THRESHOLD);
        layout.champion = findSlot("images/CHAMPION.png", SLOT_THRESHOLD);
        layout.DRAGONDUKE = findSlot("images/DRAGONDUKE.png", SLOT_THRESHOLD);
        layout.MINIONPRINCE = findSlot("images/MINIONPRINCE.png", SLOT_THRESHOLD);

        layout.valkyrie = findSlot("images/VALKYRIE.png", SLOT_THRESHOLD);
        layout.jumpSpell = findSlot("images/JUMP_SPELL.png", SLOT_THRESHOLD);
        layout.dragon = findSlot("images/DRAGON.png", SLOT_THRESHOLD);
        layout.wizard = findSlot("images/WIZARD.png", SLOT_THRESHOLD);
        layout.barbarian = findSlot("images/BARBARIAN.png", SLOT_THRESHOLD);
        layout.SNEAKY_GOBLIN = findSlot("images/SNEAKY_GOBLIN.png", SLOT_THRESHOLD);



        return layout;
    }
}
