import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Question 7: Find files accessible by more than K distinct users --
 * "overexposed data" in Varonis terms.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - O(n) to build file -> users, n = number of ACL tuples.
 *  - O(k log k) to sort k distinct files by exposure.
 *  - Space: O(n) worst case.
 */
public class OverexposedFiles {

    public record AclEntry(String user, String file, String permission) {}
    public record Exposure(String file, int userCount) {}

    /** Files with strictly more than K distinct users granted any permission. */
    public static List<Exposure> findOverexposed(List<AclEntry> acl, int k) {
        Map<String, Set<String>> usersByFile = new HashMap<>();

        for (AclEntry entry : acl) {
            // Set dedupes a user who shows up multiple times (READ + WRITE rows) for free.
            usersByFile.computeIfAbsent(entry.file(), f -> new HashSet<>()).add(entry.user());
        }

        List<Exposure> overexposed = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : usersByFile.entrySet()) {
            int userCount = entry.getValue().size();
            if (userCount > k) { // strictly "more than K" -> K itself is not flagged
                overexposed.add(new Exposure(entry.getKey(), userCount));
            }
        }

        overexposed.sort((a, b) -> Integer.compare(b.userCount(), a.userCount()));
        return overexposed;
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        List<AclEntry> acl = List.of(
                new AclEntry("alice", "/shares/finance/budget.xlsx", "READ"),
                new AclEntry("bob", "/shares/finance/budget.xlsx", "READ"),
                new AclEntry("carol", "/shares/finance/budget.xlsx", "WRITE"),
                new AclEntry("carol", "/shares/finance/budget.xlsx", "READ"), // dup user, diff perm
                new AclEntry("dave", "/shares/finance/budget.xlsx", "READ"),
                new AclEntry("alice", "/shares/hr/salaries.csv", "READ"),
                new AclEntry("bob", "/shares/hr/salaries.csv", "READ")
        );

        System.out.println("=== Files accessible by more than 2 users ===");
        for (Exposure exposure : findOverexposed(acl, 2)) {
            System.out.printf("%-32s -> %d users%n", exposure.file(), exposure.userCount());
        }
    }
}
