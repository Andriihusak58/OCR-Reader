public class BotConfig {

    public int mainTargetGold = 10_000_000;
    public int mainTargetElixir = 10_000_000;
    public int enemyMinLootTotal = 1_500_000;

    // 0 = no limit
    public int mainMaxAttacks = 0;
    public int builderMaxAttacks = 0;

    public int builderTargetGold = 4_500_000;
    public int builderTargetElixir = 4_500_000;

    // stop the run if nothing useful happens for this long (0 = off); must be longer than one battle
    public int stuckTimeoutMinutes = 6;

    public BotEvents events = BotEvents.NONE;
}