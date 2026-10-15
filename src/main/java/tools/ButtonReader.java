import java.awt.image.BufferedImage;
import java.util.Scanner;

/**
 * Знімає скріншоти з емулятора (LDPlayer) через ADB.
 * Запусти, перейди в грі на потрібний екран, введи в консолі назву
 * (наприклад CASE) і натисни Enter -> збережеться CASE.png (весь екран).
 * Порожній рядок = вихід.
 */
public class ButtonReader {

    private static final String ADB_PATH = "adb"; // або повний шлях до adb.exe
    private static final String SERIAL = "emulator-5554";

    public static void main(String[] args) {
        try {
            AdbScreenSource screen = new AdbScreenSource(new Adb("C:\\LDPlayer\\LDPlayer14\\adb.exe", SERIAL));
            ImagineServer saver = new ImagineServer();
            Scanner in = new Scanner(System.in);

            while (true) {
                System.out.print("Назва кнопки (Enter = вихід): ");
                String name = in.nextLine().trim();
                if (name.isEmpty()) break;

                BufferedImage image = screen.capture(screen.bounds());
                saver.save(image, name + ".png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}