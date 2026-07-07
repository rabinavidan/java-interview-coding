import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Question 8: Can a user transitively reach a target group through nested
 * group memberships? ("effective permissions" / privilege-escalation reachability)
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - O(V + E): V = users/groups, E = membership edges. Each node/edge visited once.
 *  - Space: O(V) for the visited set and BFS queue/parent map.
 */
public class PermissionEscalation {

    /**
     * BFS over the membership graph, starting from `start`. BFS (not DFS)
     * is preferred here because it naturally yields the SHORTEST
     * escalation path, which is what a real security report wants to show.
     *
     * @param memberships node -> list of groups it directly belongs to.
     * @return the shortest path from start to target (inclusive), or empty if unreachable.
     */
    public static Optional<List<String>> canReach(
            Map<String, List<String>> memberships, String start, String target) {

        if (start.equals(target)) {
            return Optional.of(List.of(start));
        }

        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>(); // child -> parent, to reconstruct the path
        Deque<String> queue = new ArrayDeque<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            for (String group : memberships.getOrDefault(current, List.of())) {
                if (!visited.add(group)) {
                    continue; // already visited -> also guards against cycles
                }
                parent.put(group, current);

                if (group.equals(target)) {
                    return Optional.of(reconstructPath(parent, start, target));
                }
                queue.add(group);
            }
        }

        return Optional.empty(); // target unreachable from start
    }

    private static List<String> reconstructPath(Map<String, String> parent, String start, String target) {
        List<String> path = new ArrayList<>();
        String node = target;
        while (!node.equals(start)) {
            path.add(node);
            node = parent.get(node);
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        Map<String, List<String>> memberships = Map.of(
                "alice", List.of("developers"),
                "developers", List.of("engineering"),
                "engineering", List.of("staff"),
                "bob", List.of("contractors"),
                "contractors", List.of(), // dead end -> no path to admins
                // Intentional cycle to prove the visited-set guards against infinite loops:
                "staff", List.of("admins", "engineering")
        );

        System.out.println("=== alice -> admins ===");
        canReach(memberships, "alice", "admins")
                .ifPresentOrElse(
                        path -> System.out.println("Reachable via: " + String.join(" -> ", path)),
                        () -> System.out.println("Not reachable"));

        System.out.println("\n=== bob -> admins ===");
        canReach(memberships, "bob", "admins")
                .ifPresentOrElse(
                        path -> System.out.println("Reachable via: " + String.join(" -> ", path)),
                        () -> System.out.println("Not reachable"));
    }
}
