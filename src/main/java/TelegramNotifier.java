import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Telegram Bot API client (no extra dependencies).
 * Every user creates their own bot with @BotFather and pastes the token into the GUI,
 * so no shared secret ever ships with the program.
 */
public class TelegramNotifier {

    public static final String DEFAULT_API = "https://api.telegram.org";

    private static final Pattern CHAT_ID = Pattern.compile("\"chat\"\\s*:\\s*\\{\\s*\"id\"\\s*:\\s*(-?\\d+)");
    private static final Pattern USERNAME = Pattern.compile("\"username\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern DESCRIPTION = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]*)\"");

    private final String apiBase;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public TelegramNotifier() {
        this(DEFAULT_API);
    }

    public TelegramNotifier(String apiBase) {
        this.apiBase = apiBase;
    }

    /** Checks the token and returns the bot username (without @). Throws with a readable message. */
    public String checkToken(String token) throws IOException, InterruptedException {
        String body = call(token, "getMe", "");
        Matcher m = USERNAME.matcher(body);
        return m.find() ? m.group(1) : "bot";
    }

    /**
     * Waits until somebody writes to the bot and returns that chat id.
     * The user has to send any message (e.g. /start) to the bot while this runs.
     * Returns null on timeout.
     */
    public String waitForChatId(String token, int timeoutSeconds) throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            // long-poll for up to 5 s; the newest message comes last
            String body = call(token, "getUpdates", "timeout=5&allowed_updates=%5B%22message%22%5D");
            String id = null;
            Matcher m = CHAT_ID.matcher(body);
            while (m.find()) {
                id = m.group(1);
            }
            if (id != null) {
                return id;
            }
            Thread.sleep(300);
        }
        return null;
    }

    public void sendMessage(String token, String chatId, String text) throws IOException, InterruptedException {
        call(token, "sendMessage", "chat_id=" + enc(chatId) + "&text=" + enc(text));
    }

    private String call(String token, String method, String form) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiBase + "/bot" + token.trim() + "/" + method))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = response.body();
        if (response.statusCode() != 200) {
            Matcher d = DESCRIPTION.matcher(body);
            String why = d.find() ? d.group(1) : "HTTP " + response.statusCode();
            if (response.statusCode() == 401 || response.statusCode() == 404) {
                why = "invalid bot token (" + why + ")";
            }
            throw new IOException(why);
        }
        return body;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
