/**
 * Перевірка зуму в LDPlayer: відкрий гру на головному екрані і запусти.
 * Камера має віддалитись (pinch двома пальцями).
 */
public class ZoomTest {

    private static final String ADB_PATH = "C:\\\\LDPlayer\\\\LDPlayer14\\\\adb.exe"; // або повний шлях до adb.exe
    private static final String SERIAL = "emulator-5554";

    public static void main(String[] args) throws Exception {
        AdbInputDevice input = new AdbInputDevice(new Adb(ADB_PATH, SERIAL));
        input.zoomOut(1002, 757, 1);
        System.out.println("Zoom sent");
    }
}
