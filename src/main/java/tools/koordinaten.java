import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;

public class koordinaten {
    public koordinaten() {
    }

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();

        while(true) {
            for(int i = 3; i >= 0; --i) {
                Thread.sleep(1000L);
                System.out.println("Sekunden: " + i);
            }

            PointerInfo pointer = MouseInfo.getPointerInfo();
            Point p = pointer.getLocation();
            System.out.println("X: " + p.x + " Y:" + p.y);
            robot.getPixelColor(p.x, p.y);
        }
    }
}