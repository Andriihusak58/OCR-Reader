import java.util.Random;

public class Builder {

    private volatile boolean running = true;
    private final BotConfig config;

    public Builder() {
        this.config = new BotConfig();
    }

    public Builder(BotConfig config) {
        this.config = config;
    }

    public void stop() {
        running = false;
    }

    public void run() throws Exception {
        final Bot bot = new Bot();
        final OcrService ocrService = new OcrService();
        final Random random = new Random();

        while (running) {

            bot.pressIfFound(UiElements.Attack_DC);
            Thread.sleep(300 + random.nextInt(150));

            bot.pressIfFound(UiElements.Find_Now);
            Thread.sleep(5000 + random.nextInt(1000));

            bot.pressIfFound(UiElements.Battle_Machine);
            Thread.sleep(300 + random.nextInt(150));

            Actions.battleHoldClick(Regions.POINT_2, 600 + random.nextInt(200));
            Thread.sleep(300 + random.nextInt(150));

            Actions.battleClick(Regions.SLOT_1);
            Thread.sleep(300 + random.nextInt(150));

            Actions.battleHoldClick(Regions.POINT_2, 1100 + random.nextInt(200));
            Thread.sleep(60000 + random.nextInt(5000));

            bot.pressIfFound(UiElements.SURRENDER_DC);
            Thread.sleep(300 + random.nextInt(150));

            bot.pressIfFound(UiElements.SURRENDER_OKAY);
            Thread.sleep(500 + random.nextInt(150));

            bot.pressIfFound(UiElements.RETURN_HOME_DC);
            Thread.sleep(1500 + random.nextInt(150));

            bot.pressIfFound(UiElements.OKAY_STAR_BONUS_DC);
            Thread.sleep(500 + random.nextInt(150));

            Actions.zoomOutMany(1002, 1004, 5);
            Thread.sleep(300 + random.nextInt(150));

            Actions.battleClick(Regions.ELIXIR_CART);
            Thread.sleep(300 + random.nextInt(150));

            bot.pressIfFound(UiElements.COLLECT);
            Thread.sleep(300 + random.nextInt(150));

            Actions.battleClick(Regions.Attack_DC);
            Thread.sleep(2000 + random.nextInt(350));

            int myGold = ocrService.readStableDigitsAsInt(Regions.MY_GOLD);
            int myElixir = ocrService.readStableDigitsAsInt(Regions.MY_ELIXIR);

            System.out.println("My gold: " + myGold);
            System.out.println("My elixir: " + myElixir);

            if (myGold == -1 || myElixir == -1) {
                System.out.println("OCR failed");
                continue;
            }

            if (myGold >= config.builderTargetGold && myElixir >= config.builderTargetElixir) {
                System.out.println("Target reached");
                break;
            }
        }

        System.out.println("Builder bot stopped");
    }

    public static void main(String[] args) throws Exception {
        Builder builder = new Builder();
        builder.run();
    }
}