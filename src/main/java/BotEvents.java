/**
 * Callbacks from the running bot (used by the GUI now, and by notifications later).
 * Methods are called from the bot thread, so UI code must hop to the EDT itself.
 */
public interface BotEvents {

    BotEvents NONE = new BotEvents() {
    };

    /** The bot did something meaningful (read the screen, found a base ...). Used by the stuck watchdog. */
    default void progress() {
    }

    /** A battle was completed. totalAttacks counts attacks in the current run. */
    default void attackFinished(int totalAttacks) {
    }

    /** The bot loop ended. reason is a short human-readable text. */
    default void stopped(String reason, int totalAttacks) {
    }
}
