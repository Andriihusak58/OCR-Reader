/** Створює GameContext у момент запуску бота (режим вибирається в GUI). */
@FunctionalInterface
public interface ContextFactory {
    GameContext create() throws Exception;
}
