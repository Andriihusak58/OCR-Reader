public class UiElement {
    /** Область пошуку на екрані. null означає "шукати на всьому екрані". */
    public final Region region;
    public final String templatePath;
    public final double threshold;

    public UiElement(Region region, String templatePath, double threshold) {
        this.region = region;
        this.templatePath = templatePath;
        this.threshold = threshold;
    }

    /** Елемент, який шукається на всьому екрані. */
    public UiElement(String templatePath, double threshold) {
        this(null, templatePath, threshold);
    }
}
