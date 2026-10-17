import java.awt.image.BufferedImage;

/**
 * At 33% and 66% damage the game pauses the attack and asks to pick one of three rewards.
 * This class detects the "Pick a Reward!" banner and taps the chosen card.
 */
public class RewardPicker {

    public static final int LEFT = 0;
    public static final int MIDDLE = 1;
    public static final int RIGHT = 2;

    private static final int MAX_TAPS = 3;

    private final ScreenSource screen;
    private final Actions actions;
    private final ImageLoader loader = new ImageLoader();
    private final TemplateMatcher matcher = new TemplateMatcher();
    private final int choice;

    public RewardPicker(GameContext ctx, int choice) {
        this.screen = ctx.screen;
        this.actions = ctx.actions;
        this.choice = Math.max(LEFT, Math.min(RIGHT, choice));
    }

    public boolean isShown() {
        UiElement banner = UiElements.PICK_REWARD;
        BufferedImage frame = screen.capture(banner.region);
        MatchResult result = matcher.find(frame, loader.load(banner.templatePath), banner.threshold);
        return result.found;
    }

    /** If the reward screen is up, picks a card and returns true; false when it is not shown. */
    public boolean handle() throws InterruptedException {
        if (!isShown()) return false;

        System.out.println("Reward screen detected, picking card " + (choice + 1));
        for (int i = 0; i < MAX_TAPS; i++) {
            actions.click(card(choice));
            Thread.sleep(1500);
            if (!isShown()) {
                System.out.println("Reward picked");
                return true;
            }
            System.out.println("Reward screen still shown, tapping again");
        }
        return true;
    }

    private static Region card(int index) {
        return switch (index) {
            case LEFT -> Regions.REWARD_LEFT;
            case MIDDLE -> Regions.REWARD_MIDDLE;
            default -> Regions.REWARD_RIGHT;
        };
    }
}
