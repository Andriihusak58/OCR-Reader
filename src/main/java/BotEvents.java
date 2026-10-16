/**
 * Callbacks from the running bot (used by the GUI now, and by notifications later).
 * Methods are called from the bot thread, so UI code must hop to the EDT itself.
 */
public interface BotEvents {

    BotEvents NONE = new BotEvents() {
    };

    /** A battle was completed. totalAttacks counts attacks in the current run. */
    default void attackFinished(int totalAttacks) {
    }

    /** The bot loop ended. reason is a short human-readable text. */
    default void stopped(String reason, int totalAttacks) {
    }
}
