import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Question 5: Flag (user, resource) accesses that fall outside the user's
 * historical baseline -- a simplified UEBA "first time this user touched
 * this resource" anomaly check.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - O(m) to scan m new events, O(1) average per Set.contains lookup.
 *  - Space: O(1) extra (result list aside).
 */
public class AccessAnomalyDetector {

    public record AccessEvent(String user, String resource) {}

    /**
     * Returns the subset of newEvents that are anomalous: the user has no
     * baseline record of ever accessing that resource before.
     */
    public static List<AccessEvent> findAnomalies(
            Map<String, Set<String>> baseline, List<AccessEvent> newEvents) {

        List<AccessEvent> anomalies = new ArrayList<>();
        for (AccessEvent event : newEvents) {
            // getOrDefault(Set.of()) handles "brand-new user, no baseline"
            // without a separate containsKey check.
            Set<String> knownResources = baseline.getOrDefault(event.user(), Set.of());
            if (!knownResources.contains(event.resource())) {
                anomalies.add(event);
            }
        }
        return anomalies;
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        Map<String, Set<String>> baseline = Map.of(
                "alice", Set.of("/finance/reports", "/finance/budget"),
                "bob", Set.of("/hr/policies")
        );

        List<AccessEvent> newEvents = List.of(
                new AccessEvent("alice", "/finance/reports"),   // known -> fine
                new AccessEvent("alice", "/engineering/source"),// never touched -> anomaly
                new AccessEvent("bob", "/hr/policies"),         // known -> fine
                new AccessEvent("carol", "/finance/budget")     // no baseline at all -> anomaly
        );

        System.out.println("=== Anomalous accesses ===");
        for (AccessEvent anomaly : findAnomalies(baseline, newEvents)) {
            System.out.printf("%-6s -> %s%n", anomaly.user(), anomaly.resource());
        }
    }
}
