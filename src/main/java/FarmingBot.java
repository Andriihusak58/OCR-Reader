public class FarmingBot {

    private final SearchBot searchBot;
    private final AttackBot attackBot;
    private final OcrService ocrService;
    private final BotConfig config;

    private volatile boolean running = true;
    private int attacks = 0;

    public FarmingBot(BotConfig config, GameContext ctx) throws Exception {
        this.config = config;
        this.searchBot = new SearchBot(config, ctx);
        this.attackBot = new AttackBot(ctx);
        this.ocrService = new OcrService(ctx.screen);
    }

    public void stop() {
        running = false;
        searchBot.stop();
        attackBot.stop();
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

            config.events.progress();

            if (myGold >= config.mainTargetGold && myElixir >= config.mainTargetElixir) {
                System.out.println("Target reached");
                reason = "Target reached";
                break;
            }

            boolean found = searchBot.findGoodBase();

            if (!running) break;

            if (!found) {
                System.out.println("Could not find good base");
                reason = "Could not find a good base";
                break;
            }

            config.events.progress();

            boolean attacked = attackBot.attack();

            if (!running) break;

            if (!attacked) {
                System.out.println("Attack failed");
                reason = "Attack failed";
                break;
            }

            attacks++;
            System.out.println("Attacks done: " + attacks);
            config.events.attackFinished(attacks);

            if (config.mainMaxAttacks > 0 && attacks >= config.mainMaxAttacks) {
                System.out.println("Attack limit reached");
                reason = "Attack limit reached (" + attacks + ")";
                break;
            }
        }

        System.out.println("FarmingBot stopped");
        return reason;
    }
}