public class MatchResult {
    public final boolean found;
    public final int x;
    public final int y;
    public final double similarity;

    public MatchResult(boolean found, int x, int y, double similarity) {
        this.found = found;
        this.x = x;
        this.y = y;
        this.similarity = similarity;
    }
}