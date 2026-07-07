import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Question 4: Detect brute-force login attempts using a per-user sliding
 * time window: flag a user with 5+ failed logins within any 60-second span.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - O(n) total: each event is pushed once and popped at most once across
 *    the whole run (amortized O(1) per event), not O(n) per event.
 *  - Space: O(n) worst case if all events for one user stay in-window.
 */
public class BruteForceDetector {

    public record LoginEvent(String user, long timestampEpochSeconds) {}

    private static final int THRESHOLD = 5;
    private static final long WINDOW_SECONDS = 60;

    /**
     * Returns, in first-tripped order, the users who hit the threshold and
     * the timestamp at which they did.
     */
    public static Map<String, Long> detectBruteForce(List<LoginEvent> events) {
        // Per-user rolling window of recent failure timestamps.
        Map<String, Deque<Long>> windows = new LinkedHashMap<>();
        // LinkedHashMap so flagged users come out in the order they tripped.
        Map<String, Long> flagged = new LinkedHashMap<>();

        for (LoginEvent event : events) {
            Deque<Long> window = windows.computeIfAbsent(event.user(), u -> new ArrayDeque<>());
            window.addLast(event.timestampEpochSeconds());

            // Evict timestamps that have fallen outside the 60s window.
            // Relies on events arriving in non-decreasing timestamp order.
            while (window.peekFirst() != null
                    && event.timestampEpochSeconds() - window.peekFirst() > WINDOW_SECONDS) {
                window.pollFirst();
            }

            if (window.size() >= THRESHOLD && !flagged.containsKey(event.user())) {
                flagged.put(event.user(), event.timestampEpochSeconds());
            }
        }
        return flagged;
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        List<LoginEvent> events = List.of(
                new LoginEvent("alice", 0),
                new LoginEvent("bob", 5),
                new LoginEvent("alice", 10),
                new LoginEvent("alice", 20),
                new LoginEvent("bob", 65),   // spread out -> won't trip for bob
                new LoginEvent("alice", 30),
                new LoginEvent("alice", 40)  // 5th failure for alice within 40s -> trips
        );

        Map<String, Long> flagged = detectBruteForce(events);
        System.out.println("=== Users flagged for brute-force ===");
        flagged.forEach((user, ts) ->
                System.out.printf("%-8s tripped at t=%d%n", user, ts));
    }
}
