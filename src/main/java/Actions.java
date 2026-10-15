import java.util.Random;

/**
 * "Людські" дії поверх InputDevice: випадкова точка всередині регіону
 * і випадкові паузи. Сам ввід виконує InputDevice.
 */
public class Actions {

    private final InputDevice device;
    private final Random random = new Random();

    public Actions(InputDevice device) {
        this.device = device;
    }

    // Повільний людський клік (меню / кнопки)
    public void click(Region region) throws InterruptedException {
        Thread.sleep(350 + random.nextInt(150));

        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        device.press(clickX, clickY, 30 + random.nextInt(50));

        System.out.println("Clicked at: " + clickX + ", " + clickY);
    }

    // Повільний hold (якщо треба для меню)
    public void holdClick(Region region, int holdMs) throws InterruptedException {
        Thread.sleep(350 + random.nextInt(150));

        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        device.press(clickX, clickY, holdMs);

        System.out.println("Hold clicked at: " + clickX + ", " + clickY + " for " + holdMs + "ms");
    }

    // Швидкий бойовий клік
    public void battleClick(Region region) throws InterruptedException {
        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        device.press(clickX, clickY, 20 + random.nextInt(40));

        System.out.println("Battle clicked at: " + clickX + ", " + clickY);
    }

    // Швидкий hold для висадки війська
    public void battleHoldClick(Region region, int holdMs) throws InterruptedException {
        int clickX = region.x + random.nextInt(region.width);
        int clickY = region.y + random.nextInt(region.height);

        device.press(clickX, clickY, holdMs);

        System.out.println("Battle hold clicked at: " + clickX + ", " + clickY + " for " + holdMs + "ms");
    }

    public void zoomOutMany(int x, int y, int times) throws InterruptedException {
        device.zoomOut(x, y, times);

        System.out.println("Zoomed out at: " + x + ", " + y);
    }
}
