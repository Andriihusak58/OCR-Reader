import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Random;

public class Actions {

    private static final Random random = new Random();
    private static final Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create Robot", e);
        }
    }

    // Повільний людський клік (меню / кнопки)
    public static void click(Region region) throws InterruptedException {
        Thread.sleep(350 + random.nextInt(150));

        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        Thread.sleep(30 + random.nextInt(50));

        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        System.out.println("Clicked at: " + clickX + ", " + clickY);
    }

    // Повільний hold (якщо треба для меню)
    public static void holdClick(Region region, int holdMs) throws InterruptedException {
        Thread.sleep(350 + random.nextInt(150));

        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        Thread.sleep(holdMs);

        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        System.out.println("Hold clicked at: " + clickX + ", " + clickY + " for " + holdMs + "ms");
    }

    // Швидкий бойовий клік
    public static void battleClick(Region region) throws InterruptedException {
        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        Thread.sleep(20 + random.nextInt(40));

        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        System.out.println("Battle clicked at: " + clickX + ", " + clickY);
    }

    // Швидкий hold для висадки війська
    public static void battleHoldClick(Region region, int holdMs) throws InterruptedException {
        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        robot.mouseMove(clickX, clickY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        Thread.sleep(holdMs);

        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        System.out.println("Battle hold clicked at: " + clickX + ", " + clickY + " for " + holdMs + "ms");
    }

    // Zoom out через CTRL + колесо в конкретній точці
    public static void zoomOutMany(int x, int y, int times) throws InterruptedException {

        robot.mouseMove(x, y);

        Thread.sleep(200);

        robot.keyPress(KeyEvent.VK_CONTROL);

        for (int i = 0; i < times; i++) {

            robot.mouseWheel(3);

            Thread.sleep(100 + random.nextInt(100));
        }

        robot.keyRelease(KeyEvent.VK_CONTROL);

        System.out.println("Zoomed out at: " + x + ", " + y);
    }

}