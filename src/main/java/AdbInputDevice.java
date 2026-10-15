import java.io.IOException;

/** Ввід в емулятор через adb. Курсор ПК не чіпає, тож можна працювати паралельно. */
public class AdbInputDevice implements InputDevice {

    // Мультитач-пристрій LDPlayer (видно через "adb shell getevent -lp", ім'я "input").
    private static final String DEFAULT_TOUCH_DEVICE = "/dev/input/event4";
    private static final int SCREEN_W = 1920;
    private static final int SCREEN_H = 1080;

    // Коди Linux input (десятковий формат, який приймає sendevent)
    private static final int EV_SYN = 0, EV_KEY = 1, EV_ABS = 3;
    private static final int BTN_TOUCH = 330;
    private static final int ABS_MT_SLOT = 47, ABS_MT_POSITION_X = 53, ABS_MT_POSITION_Y = 54,
            ABS_MT_TRACKING_ID = 57, ABS_MT_PRESSURE = 58;

    private final Adb adb;
    private final String touchDevice;

    public AdbInputDevice(Adb adb) {
        this(adb, DEFAULT_TOUCH_DEVICE);
    }

    public AdbInputDevice(Adb adb, String touchDevice) {
        this.adb = adb;
        this.touchDevice = touchDevice;
    }

    @Override
    public void press(int x, int y, int holdMs) throws InterruptedException {
        try {
            // swipe з однаковими початком і кінцем = натискання з утриманням
            adb.exec("shell", "input", "swipe",
                    String.valueOf(x), String.valueOf(y),
                    String.valueOf(x), String.valueOf(y),
                    String.valueOf(Math.max(holdMs, 1)));
        } catch (IOException e) {
            throw new IllegalStateException("ADB tap failed: " + e.getMessage(), e);
        }
    }

    /** Зум-аут: двопальцевий pinch (пальці зводяться до точки), "times" разів. */
    @Override
    public void zoomOut(int x, int y, int times) throws InterruptedException {
        try {
            for (int i = 0; i < times; i++) {
                adb.exec("shell", buildPinchScript(x, y, 450, 60, 12, 100 + i * 2));
                Thread.sleep(300);
            }
        } catch (IOException e) {
            throw new IllegalStateException("ADB zoom failed: " + e.getMessage(), e);
        }
    }

    /**
     * Скрипт для "adb shell": два пальці рухаються від відстані startDist до endDist
     * по горизонталі навколо (cx, cy) за "steps" кроків. Один виклик adb на весь жест.
     */
    String buildPinchScript(int cx, int cy, int startDist, int endDist, int steps, int trackingId) {
        StringBuilder sb = new StringBuilder();

        for (int s = 0; s <= steps; s++) {
            int dist = startDist + (endDist - startDist) * s / steps;
            int lx = clamp(cx - dist, 0, SCREEN_W - 1);
            int rx = clamp(cx + dist, 0, SCREEN_W - 1);
            int y = clamp(cy, 0, SCREEN_H - 1);

            if (s == 0) {
                // обидва пальці торкаються екрана
                event(sb, EV_ABS, ABS_MT_SLOT, 0);
                event(sb, EV_ABS, ABS_MT_TRACKING_ID, trackingId);
                event(sb, EV_ABS, ABS_MT_SLOT, 1);
                event(sb, EV_ABS, ABS_MT_TRACKING_ID, trackingId + 1);
                event(sb, EV_KEY, BTN_TOUCH, 1);
            }

            finger(sb, 0, lx, y);
            finger(sb, 1, rx, y);
            event(sb, EV_SYN, 0, 0);
            sb.append("sleep 0.02;");
        }

        // відпустити обидва пальці
        event(sb, EV_ABS, ABS_MT_SLOT, 0);
        event(sb, EV_ABS, ABS_MT_TRACKING_ID, -1);
        event(sb, EV_ABS, ABS_MT_SLOT, 1);
        event(sb, EV_ABS, ABS_MT_TRACKING_ID, -1);
        event(sb, EV_KEY, BTN_TOUCH, 0);
        event(sb, EV_SYN, 0, 0);

        return sb.toString();
    }

    private void finger(StringBuilder sb, int slot, int x, int y) {
        event(sb, EV_ABS, ABS_MT_SLOT, slot);
        event(sb, EV_ABS, ABS_MT_POSITION_X, x);
        event(sb, EV_ABS, ABS_MT_POSITION_Y, y);
        event(sb, EV_ABS, ABS_MT_PRESSURE, 1);
    }

    private void event(StringBuilder sb, int type, int code, int value) {
        sb.append("sendevent ").append(touchDevice).append(' ')
                .append(type).append(' ').append(code).append(' ').append(value).append(';');
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
