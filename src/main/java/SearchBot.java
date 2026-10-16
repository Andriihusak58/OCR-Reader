import com.sun.source.util.SourcePositions;

import java.util.Random;

public class SearchBot {

    private final Bot bot;
    private final Actions actions;
    private final OcrService ocrService;
    private final BotConfig config;
    private final Random random = new Random();

    private volatile boolean running = true;

    public SearchBot(BotConfig config, GameContext ctx) {
        this.config = config;
        this.bot = new Bot(ctx);
        this.actions = ctx.actions;
        this.ocrService = new OcrService(ctx.screen);
    }

    public void stop() {
        running = false;
    }

    private boolean shouldStop() {
        return !running;
    }

    private void sleep(int minMs, int randomAddMs) throws InterruptedException {
        int total = minMs + random.nextInt(randomAddMs);
        int waited = 0;

        while (waited < total) {
            if (shouldStop()) {
                return;
            }

            Thread.sleep(100);
            waited += 100;
        }
    }

    public void prepareHomeScreen() throws Exception {
        clearPopups();
    }

    private void clearPopups() throws Exception {

        if (bot.pressIfFound(UiElements.OKAY_STAR_BONUS)) {
            sleep(300, 390);
        }

        if (shouldStop()) return;

        if (bot.pressIfFound(UiElements.CASE)) {
            sleep(150, 150);
            if (shouldStop()) return;

            actions.battleClick(Regions.CASE);
            sleep(150, 150);
            if (shouldStop()) return;

            actions.battleClick(Regions.CASE);
            sleep(150, 150);
            if (shouldStop()) return;

            actions.battleClick(Regions.CASE);
            sleep(150, 150);
            if (shouldStop()) return;

            actions.battleClick(Regions.CASE);
            sleep(4150, 150);
            if (shouldStop()) return;
            System.out.println("uuhuhuhuhuhki");
            actions.battleClick(Regions.CONTINUE);
            sleep(200, 150);
            if (shouldStop()) return;
        }

        if (bot.pressIfFound(UiElements.OKAY_STAR_BONUS)) {
            sleep(150, 150);
        }
    }

    private void collectAndZoomOut() throws Exception {
        actions.zoomOutMany(1002, 757, 1);
        sleep(150, 150);
    }

    public boolean findGoodBase() throws Exception {

        collectAndZoomOut();

        if (!bot.pressIfFound(UiElements.ATTACK)) return false;
        sleep(400, 150);
        if (shouldStop()) return false;

        if (!bot.pressIfFound(UiElements.FIND_MATCH)) return false;
        sleep(400, 150);
        if (shouldStop()) return false;

        if (!bot.pressIfFound(UiElements.ATTACK_GO)) return false;
        sleep(3000, 1000);
        if (shouldStop()) return false;

        int ocrFailCount = 0;

        while (running) {
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
                    sleep(3000, 1000);
                    if (shouldStop()) return false;
                    continue;
                }

                sleep(2500, 1000);
                if (shouldStop()) return false;
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

            sleep(3000, 1000);
            if (shouldStop()) return false;
        }

        return false; // running став false -> зупинка за запитом
    }
}
