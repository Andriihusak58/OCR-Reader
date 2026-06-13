public class BattleSlot {
    public final String name;
    public final String templatePath;
    public final double threshold;

    public BattleSlot(String name, String templatePath, double threshold) {
        this.name = name;
        this.templatePath = templatePath;
        this.threshold = threshold;
    }
}