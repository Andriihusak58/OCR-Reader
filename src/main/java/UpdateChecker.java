import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks GitHub for a newer release. Never throws: any network/parse problem means "no update info".
 */
public final class UpdateChecker {

    /** Current program version. Keep in sync with the tag of the GitHub release (v1.0.0 -> 1.0.0). */
    public static final String VERSION = "1.0.3";

    private static final String LATEST_URL =
            "https://api.github.com/repos/Andriihusak58/OCR-Reader/releases/latest";

    /** jarUrl / imagesUrl are the release assets BotOCR.jar and images.zip (null if not attached). */
    public record Update(String version, String url, String jarUrl, String imagesUrl) {
    }

    private UpdateChecker() {
    }

    /** Returns the newer release, or null if up to date / unreachable. */
    public static Update check() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(LATEST_URL))
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "BotOCR-update-check")
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            return parse(response.body(), VERSION);
        } catch (Exception e) {
            return null;
        }
    }

    static Update parse(String json, String currentVersion) {
        String tag = field(json, "tag_name");
        String url = field(json, "html_url");
        if (tag == null || url == null) return null;
        String version = tag.startsWith("v") || tag.startsWith("V") ? tag.substring(1) : tag;
        if (!isNewer(version, currentVersion)) return null;
        return new Update(version, url, asset(json, "BotOCR.jar"), asset(json, "images.zip"));
    }

    private static String asset(String json, String fileName) {
        Matcher m = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        while (m.find()) {
            if (m.group(1).endsWith("/" + fileName)) return m.group(1);
        }
        return null;
    }

    /** First occurrence of "name": "value" (top-level release fields come before nested ones). */
    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static boolean isNewer(String candidate, String current) {
        int[] a = numbers(candidate);
        int[] b = numbers(current);
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y) return x > y;
        }
        return false;
    }

    private static int[] numbers(String v) {
        String[] parts = v.split("[^0-9]+");
        return java.util.Arrays.stream(parts).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
    }
}
