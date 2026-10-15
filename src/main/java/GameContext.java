import java.awt.AWTException;

/**
 * Усе, що потрібно ботам для роботи з грою: звідки брати скріншоти
 * і як виконувати дії. Створюється один раз і передається вниз по класах.
 */
public class GameContext {

    public final ScreenSource screen;
    public final Actions actions;

    public GameContext(ScreenSource screen, InputDevice input) {
        this.screen = screen;
        this.actions = new Actions(input);
    }

    /** Гра на екрані ПК: скріншоти й ввід через java.awt.Robot. */
    public static GameContext robot() throws AWTException {
        return new GameContext(new ScreenReader(), new RobotInputDevice());
    }

    /** Гра в Android-емуляторі (LDPlayer): скріншоти й ввід через adb, монітор ПК не потрібен. */
    public static GameContext adb(String serial) {
        Adb adb = new Adb(serial);
        return new GameContext(new AdbScreenSource(adb), new AdbInputDevice(adb));
    }
}
