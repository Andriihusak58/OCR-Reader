public class Main {

    private final BotConfig config;
    private final ContextFactory contextFactory;
    private FarmingBot farmingBot;

    public Main() {
        this(new BotConfig(), GameContext::robot);
    }

    public Main(BotConfig config, ContextFactory contextFactory) {
        this.config = config;
        this.contextFactory = contextFactory;
    }

    public void stop() {
        if (farmingBot != null) {
            farmingBot.stop();
        }
    }

    public void run() throws Exception {
        farmingBot = new FarmingBot(config, contextFactory.create());
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
