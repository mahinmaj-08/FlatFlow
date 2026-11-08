import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Version B: the JDK's built-in HttpServer. There is no JSON support, so we write it by hand. Port 7071. */
public class JdkApp {
    // Reads a flat JSON object like {"title":"x","assigneeUserId":2}. Fine for this spike, too fragile for real use.
    static final Pattern FIELD = Pattern.compile("\"(\\w+)\"\\s*:\\s*(\"((?:[^\"\\\\]|\\\\.)*)\"|-?\\d+)");

    static Map<String, String> parseFlatJson(String body) {
        Map<String, String> m = new HashMap<>();
        Matcher f = FIELD.matcher(body);
        while (f.find()) m.put(f.group(1), f.group(3) != null ? f.group(3) : f.group(2));
        return m;
    }

    static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }

    static String toJson(Chore c) {
        return "{\"id\":" + c.id() + ",\"title\":\"" + esc(c.title()) + "\",\"assigneeUserId\":" + c.assigneeUserId()
            + ",\"dueDate\":\"" + c.dueDate() + "\",\"status\":\"" + c.status() + "\"}";
    }

    static String toJson(List<Chore> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) sb.append(i > 0 ? "," : "").append(toJson(list.get(i)));
        return sb.append("]").toString();
    }

    static void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    public static void main(String[] args) throws IOException {
        ChoreRepository repo = new ChoreRepository("flatflow-jdk.db");
        HttpServer server = HttpServer.create(new InetSocketAddress(7071), 0);
        server.createContext("/api/chores", ex -> {
            try {
                String method = ex.getRequestMethod();
                if (method.equals("GET")) {
                    send(ex, 200, toJson(repo.findAll()));
                } else if (method.equals("POST")) {
                    Map<String, String> in = parseFlatJson(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    Chore c = new Chore(null, in.get("title"), Integer.parseInt(in.get("assigneeUserId")), in.get("dueDate"), in.get("status"));
                    send(ex, 201, toJson(repo.save(c)));
                } else {
                    send(ex, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (IllegalArgumentException e) {   // also catches NumberFormatException
                send(ex, 400, "{\"error\":\"" + esc(String.valueOf(e.getMessage())) + "\"}");
            } catch (Exception e) {
                send(ex, 500, "{\"error\":\"Server error\"}");
            }
        });
        server.start();
        System.out.println("JDK server on http://localhost:7071");
    }
}
