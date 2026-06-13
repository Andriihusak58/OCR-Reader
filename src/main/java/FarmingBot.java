public class FarmingBot {

    private final SearchBot searchBot;
    private final AttackBot attackBot;
    private final OcrService ocrService;
    private final BotConfig config;

    private volatile boolean running = true;

    public FarmingBot(BotConfig config) throws Exception {
        this.config = config;
        this.searchBot = new SearchBot(config);
        this.attackBot = new AttackBot();
        this.ocrService = new OcrService();
    }

    public void stop() {
        running = false;
        attackBot.stop();
    }

    public void run() throws Exception {
        while (running) {

            searchBot.prepareHomeScreen();

            if (!running) break;

            int myGold = ocrService.readStableDigitsAsInt(Regions.MY_GOLD);
            int myElixir = ocrService.readStableDigitsAsInt(Regions.MY_ELIXIR);

            System.out.println("My gold: " + myGold);
            System.out.println("My elixir: " + myElixir);

            if (!running) break;

            if (myGold == -1 || myElixir == -1) {
                System.out.println("OCR failed");
                continue;
            }

            if (myGold >= config.mainTargetGold && myElixir >= config.mainTargetElixir) {
                System.out.println("Target reached");
                break;
            }

            boolean found = searchBot.findGoodBase();

            if (!running) break;

            if (!found) {
                System.out.println("Could not find good base");
                break;
            }

            boolean attacked = attackBot.attack();

            if (!running) break;

            if (!attacked) {
                System.out.println("Attack failed");
                break;
            }
        }

        System.out.println("FarmingBot stopped");
    }
}