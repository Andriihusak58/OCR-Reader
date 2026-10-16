/**
 * Stops a run that makes no progress (for example the bot is stuck on a popup it does not understand).
 * The bot calls touch() through BotEvents.progress(); if nothing happens for timeoutMs the callback fires once.
 */
public class Watchdog {

    private final long timeoutMs;
    private volatile long lastProgress = System.currentTimeMillis();
    private volatile boolean triggered;
    private volatile boolean stopped;

    public Watchdog(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void touch() {
        lastProgress = System.currentTimeMillis();
    }

    public boolean triggered() {
        return triggered;
    }

    public void stop() {
        stopped = true;
    }

    /** Starts the checking thread; onStuck runs on that thread. A timeout of 0 or less disables the watchdog. */
    public void start(Runnable onStuck) {
        if (timeoutMs <= 0) {
            return;
        }
        lastProgress = System.currentTimeMillis();

        Thread t = new Thread(() -> {
            try {
                while (!stopped) {
                    Thread.sleep(Math.min(5000, Math.max(50, timeoutMs / 4)));
                    if (!stopped && System.currentTimeMillis() - lastProgress > timeoutMs) {
                        triggered = true;
                        onStuck.run();
                        return;
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "watchdog");
        t.setDaemon(true);
        t.start();
    }
}
