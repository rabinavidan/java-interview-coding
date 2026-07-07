import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Question 3: Top-N IPs by failed login attempts.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - Counting:  O(n)        (n = log lines)
 *  - Top-N:     O(k log N)  (k = unique IPs, N = requested top count) via a bounded min-heap
 *  - Space:     O(k)
 */
public class TopFailedLogins {

    // Matches "ip=<value>" and "status=<value>" tokens anywhere in the line.
    private static final Pattern IP_TOKEN = Pattern.compile("ip=(\\S+)");
    private static final Pattern STATUS_TOKEN = Pattern.compile("status=(\\S+)");

    /** Counts FAILED attempts per IP. Skips lines that don't parse cleanly. */
    public static Map<String, Integer> countFailuresByIp(Path logFile) throws IOException {
        Map<String, Integer> failuresByIp = new HashMap<>();

        try (Stream<String> lines = Files.lines(logFile)) {
            lines.forEach(line -> {
                Matcher statusMatcher = STATUS_TOKEN.matcher(line);
                if (!statusMatcher.find() || !"FAILED".equals(statusMatcher.group(1))) {
                    return; // not a failed attempt (or unparseable) -> ignore
                }
                Matcher ipMatcher = IP_TOKEN.matcher(line);
                if (!ipMatcher.find()) {
                    return; // malformed line -> skip rather than crash the job
                }
                failuresByIp.merge(ipMatcher.group(1), 1, Integer::sum);
            });
        }
        return failuresByIp;
    }

    /**
     * Returns the top N IPs by failure count using a bounded min-heap of
     * size N, which is cheaper than sorting all unique IPs when N << k.
     */
    public static List<Map.Entry<String, Integer>> topN(Map<String, Integer> failuresByIp, int n) {
        // Min-heap ordered by count ascending, so the smallest is always
        // at the top and easy to evict once the heap exceeds size N.
        PriorityQueue<Map.Entry<String, Integer>> heap = new PriorityQueue<>(
                Map.Entry.comparingByValue()
        );

        for (Map.Entry<String, Integer> entry : failuresByIp.entrySet()) {
            heap.offer(entry);
            if (heap.size() > n) {
                heap.poll(); // evict the current smallest
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(heap);
        // Heap order isn't the display order -> sort descending, tie-break by IP for determinism.
        result.sort(Map.Entry.<String, Integer>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()));
        return result;
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) throws IOException {
        Path logFile = Path.of("auth.log");
        Files.writeString(logFile, String.join("\n",
                "2026-07-05T10:00:01Z user=alice ip=203.0.113.7 status=FAILED",
                "2026-07-05T10:00:02Z user=bob   ip=198.51.100.4 status=SUCCESS",
                "2026-07-05T10:00:03Z user=carol ip=203.0.113.7 status=FAILED",
                "2026-07-05T10:00:04Z user=dave  ip=203.0.113.7 status=FAILED",
                "2026-07-05T10:00:05Z user=eve   ip=192.0.2.55  status=FAILED",
                "2026-07-05T10:00:06Z user=alice ip=198.51.100.4 status=FAILED"
        ));

        Map<String, Integer> failures = countFailuresByIp(logFile);
        System.out.println("=== Top 2 IPs by failed logins ===");
        topN(failures, 2).forEach(e ->
                System.out.printf("%-15s -> %d failures%n", e.getKey(), e.getValue()));
    }
}
