import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

/** Ввід через java.awt.Robot: керує справжнім курсором миші на ПК. */
public class RobotInputDevice implements InputDevice {

    private final Robot robot;
    private final Random random = new Random();

    public RobotInputDevice() throws AWTException {
        this.robot = new Robot();
    }

    @Override
    public void press(int x, int y, int holdMs) throws InterruptedException {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        try {
            Thread.sleep(holdMs);
        } finally {
            // навіть при зупинці (переривання) кнопка не лишається затиснутою
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    // Zoom out через CTRL + колесо миші в конкретній точці
    @Override
    public void zoomOut(int x, int y, int times) throws InterruptedException {
        robot.mouseMove(x, y);

        Thread.sleep(200);

        robot.keyPress(KeyEvent.VK_CONTROL);

        try {
            for (int i = 0; i < times; i++) {
                robot.mouseWheel(3);
                Thread.sleep(100 + random.nextInt(100));
            }
        } finally {
            robot.keyRelease(KeyEvent.VK_CONTROL);
        }
    }
}
