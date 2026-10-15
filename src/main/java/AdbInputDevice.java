import java.io.IOException;

/** Ввід в емулятор через "adb shell input". Курсор ПК не чіпає, тож можна працювати паралельно. */
public class AdbInputDevice implements InputDevice {

    private final Adb adb;

    public AdbInputDevice(Adb adb) {
        this.adb = adb;
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

    @Override
    public void zoomOut(int x, int y, int times) {
        // "adb input" не вміє мультитач (pinch). Реалізація — крок 5.
        System.out.println("WARNING: zoomOut is not implemented for ADB yet");
    }
}
