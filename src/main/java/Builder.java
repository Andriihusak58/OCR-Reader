import java.util.Random;

public class Builder {

    private volatile boolean running = true;
    private int attacks = 0;
    private final BotConfig config;
    private final ContextFactory contextFactory;
    private final Random random = new Random();

    public Builder() {
        this(new BotConfig(), GameContext::robot);
    }

    public Builder(BotConfig config, ContextFactory contextFactory) {
        this.config = config;
        this.contextFactory = contextFactory;
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

    public void run() throws Exception {
        String reason = "Error";
        try {
            reason = runLoop();
        } catch (InterruptedException e) {
            reason = "Stopped by user";
            throw e;
        } catch (Exception e) {
            reason = "Error: " + e.getMessage();
            throw e;
        } finally {
            config.events.stopped(reason, attacks);
        }
    }

    private String runLoop() throws Exception {
        String reason = "Stopped by user";
        final GameContext ctx = contextFactory.create();
        final Bot bot = new Bot(ctx);
        final OcrService ocrService = new OcrService(ctx.screen);
        final Actions actions = ctx.actions;

        while (running) {

            bot.pressIfFound(UiElements.Attack_DC);
            sleep(300, 150);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.Find_Now);
            sleep(5000, 1000);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.Battle_Machine);
            sleep(300, 150);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.Battle_COPTER);
            sleep(300, 150);
            if (shouldStop()) break;

            actions.battleHoldClick(Regions.POINT_2, 600 + random.nextInt(200));
            sleep(300, 150);
            if (shouldStop()) break;

            actions.battleClick(Regions.SLOT_1);
            sleep(300, 150);
            if (shouldStop()) break;

            actions.battleHoldClick(Regions.POINT_2, 1100 + random.nextInt(200));
            sleep(60000, 5000);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.SURRENDER_DC);
            sleep(300, 150);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.SURRENDER_OKAY);
            sleep(500, 150);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.RETURN_HOME_DC);
            sleep(4500, 150);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.OKAY_STAR_BONUS_DC);
            sleep(500, 150);
            if (shouldStop()) break;

            attacks++;
            System.out.println("Attacks done: " + attacks);
            config.events.attackFinished(attacks);

            actions.zoomOutMany(1002, 757, 1);
            sleep(300, 150);
            if (shouldStop()) break;

            actions.battleClick(Regions.ELIXIR_CART);
            sleep(300, 150);
            if (shouldStop()) break;

            bot.pressIfFound(UiElements.COLLECT);
            sleep(300, 150);
            if (shouldStop()) break;

            actions.battleClick(Regions.Attack_DC);
            sleep(2000, 350);
            if (shouldStop()) break;

            if (config.builderMaxAttacks > 0 && attacks >= config.builderMaxAttacks) {
                System.out.println("Attack limit reached");
                reason = "Attack limit reached (" + attacks + ")";
                break;
            }

            int myGold = ocrService.readStableDigitsAsInt(Regions.MY_GOLD);
            int myElixir = ocrService.readStableDigitsAsInt(Regions.MY_ELIXIR);

            System.out.println("My gold: " + myGold);
            System.out.println("My elixir: " + myElixir);

            if (myGold == -1 || myElixir == -1) {
                System.out.println("OCR failed");
                continue;
            }

            config.events.progress();

            if (myGold >= config.builderTargetGold && myElixir >= config.builderTargetElixir) {
                System.out.println("Target reached");
                reason = "Target reached";
                break;
            }
        }

        System.out.println("Builder bot stopped");
        return reason;
    }

    public static void main(String[] args) throws Exception {
        Builder builder = new Builder();
        builder.run();
    }
}
