/**
 * Низькорівневий ввід у гру. Працює з точними координатами;
 * "людська" випадковість (точка в регіоні, паузи) лежить вище, в {@link Actions}.
 */
public interface InputDevice {

    /** Натискає в точці (x, y) і утримує holdMs мілісекунд. */
    void press(int x, int y, int holdMs) throws InterruptedException;

    /** Віддаляє камеру гри навколо точки (x, y) вказану кількість разів. */
    void zoomOut(int x, int y, int times) throws InterruptedException;
}
