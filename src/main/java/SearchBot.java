import java.util.Random;

public class SearchBot {

    private final Bot bot;
    private final OcrService ocrService;
    private final BotConfig config;
    private final Random random = new Random();

    public SearchBot(BotConfig config) throws Exception {
        this.config = config;
        this.bot = new Bot();
        this.ocrService = new OcrService();
    }

    public void prepareHomeScreen() throws Exception {
        clearPopups();
        collectAndZoomOut();
    }

    private void clearPopups() throws Exception {

        if (bot.pressIfFound(UiElements.OKAY_STAR_BONUS)) {
            Thread.sleep(300 + random.nextInt(390));
        }

        if (bot.pressIfFound(UiElements.CASE)) {
            Thread.sleep(150 + random.nextInt(150));
            Actions.battleClick(Regions.CASE);
            Thread.sleep(150 + random.nextInt(150));
            Actions.battleClick(Regions.CASE);
            Thread.sleep(150 + random.nextInt(150));
            Actions.battleClick(Regions.CASE);
            Thread.sleep(150 + random.nextInt(150));
            Actions.battleClick(Regions.CASE);
            Thread.sleep(1150 + random.nextInt(150));
            Actions.battleClick(Regions.CONTINUE);
            Thread.sleep(150 + random.nextInt(150));
        }

        if (bot.pressIfFound(UiElements.OKAY_STAR_BONUS)) {
            Thread.sleep(150 + random.nextInt(150));
        }
    }

    private void collectAndZoomOut() throws Exception {
        Actions.zoomOutMany(1002, 757, 1);
        Thread.sleep(150 + random.nextInt(150));
    }

    public boolean findGoodBase() throws Exception {
        if (!bot.pressIfFound(UiElements.ATTACK)) return false;
        Thread.sleep(400 + random.nextInt(150));

        if (!bot.pressIfFound(UiElements.FIND_MATCH)) return false;
        Thread.sleep(400 + random.nextInt(150));

        if (!bot.pressIfFound(UiElements.ATTACK_GO)) return false;
        Thread.sleep(3000 + random.nextInt(1000));

        int ocrFailCount = 0;

        while (true) {
            int gold = ocrService.readStableDigitsAsInt(Regions.GOLD_ATTACK);
            int elixir = ocrService.readStableDigitsAsInt(Regions.ELIXIR_ATTACK);

            System.out.println("Enemy gold: " + gold);
            System.out.println("Enemy elixir: " + elixir);

            if (gold == -1 || elixir == -1) {
                ocrFailCount++;
                System.out.println("OCR failed, retry... (" + ocrFailCount + ")");

                if (ocrFailCount >= 5) {
                    System.out.println("Too many OCR fails -> NEXT");

                    if (!bot.pressIfFound(UiElements.NEXT)) {
                        return false;
                    }

                    ocrFailCount = 0;
                    Thread.sleep(3000 + random.nextInt(1000));
                    continue;
                }

                Thread.sleep(2500 + random.nextInt(1000));
                continue;
            }

            ocrFailCount = 0;

            if (gold + elixir >= config.enemyMinLootTotal) {
                System.out.println("Good base found");
                return true;
            }

            System.out.println("Bad base -> next");

            if (!bot.pressIfFound(UiElements.NEXT)) {
                return false;
            }

            Thread.sleep(3000 + random.nextInt(1000));
        }
    }
}