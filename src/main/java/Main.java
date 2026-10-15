public class Main {

    private final BotConfig config;
    private FarmingBot farmingBot;

    public Main() {
        this.config = new BotConfig();
    }

    public Main(BotConfig config) {
        this.config = config;
    }

    public void stop() {
        if (farmingBot != null) {
            farmingBot.stop();
        }
    }

    public void run() throws Exception {
        farmingBot = new FarmingBot(config, GameContext.robot());
        farmingBot.run();
    }

    public static void main(String[] args) {
        try {
            Main mainBot = new Main();
            mainBot.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}