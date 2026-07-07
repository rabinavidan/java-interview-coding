import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Question 13: First non-repeated event ID in a stream, both as a
 * batch computation and as an incremental "so far" query.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - Batch: O(n) time, O(k) space (k = unique IDs).
 *  - Streaming: amortized O(1) per new event.
 */
public class FirstNonRepeatedEvent {

    /* =========================================================
     * Batch version: given the whole stream up front.
     * ========================================================= */
    public static Optional<Long> firstNonRepeated(List<Long> eventIds) {
        // LinkedHashMap: HashMap's O(1) counting + preserves insertion order,
        // so the first count==1 entry in iteration order IS the first
        // non-repeated ID by arrival time.
        Map<Long, Integer> counts = new LinkedHashMap<>();
        for (Long id : eventIds) {
            counts.merge(id, 1, Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == 1) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /* =========================================================
     * Streaming version: processes one event at a time and can
     * answer "first non-repeated so far" after every event,
     * without re-scanning from the start.
     * ========================================================= */
    public static class StreamingTracker {
        private final Map<Long, Integer> counts = new LinkedHashMap<>();
        private final Deque<Long> candidates = new ArrayDeque<>(); // front = current best guess

        public void addEvent(Long id) {
            counts.merge(id, 1, Integer::sum);
            candidates.addLast(id);
            pruneStaleCandidates();
        }

        /** Lazily drops candidates from the front that are no longer count==1. */
        private void pruneStaleCandidates() {
            while (!candidates.isEmpty() && counts.get(candidates.peekFirst()) > 1) {
                candidates.pollFirst();
            }
        }

        public Optional<Long> firstNonRepeatedSoFar() {
            return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.peekFirst());
        }
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        List<Long> events = List.of(101L, 202L, 101L, 303L, 202L, 404L);

        System.out.println("=== Batch ===");
        System.out.println("First non-repeated: " + firstNonRepeated(events).orElse(null));

        System.out.println("\n=== Streaming (after each event) ===");
        StreamingTracker tracker = new StreamingTracker();
        for (Long id : events) {
            tracker.addEvent(id);
            System.out.printf("after %-4d -> first non-repeated so far: %s%n",
                    id, tracker.firstNonRepeatedSoFar().orElse(null));
        }
    }
}
