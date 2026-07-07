import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Question 12: Find duplicate files (identical content) under a root
 * directory, without an O(f^2) all-pairs comparison.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - O(f) to list and size-group files.
 *  - O(b) to hash only files that share a size with another file
 *    (b = total bytes in those candidates, typically << all files).
 *  - Space: O(f).
 */
public class DuplicateFileFinder {

    private static final int BUFFER_SIZE = 8192;

    /** Returns groups (2+ files) of byte-identical files under root. */
    public static List<List<Path>> findDuplicates(Path root) throws IOException {
        // Step 1: group by file size -- cheap (metadata only, no I/O), and
        // eliminates most non-duplicates before we pay for any hashing.
        Map<Long, List<Path>> bySize = new HashMap<>();
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> allFiles = paths.filter(Files::isRegularFile).toList();
            for (Path path : allFiles) {
                bySize.computeIfAbsent(Files.size(path), s -> new ArrayList<>()).add(path);
            }
        }

        // Step 2: only hash files that had at least one same-size sibling.
        Map<String, List<Path>> byHash = new HashMap<>();
        for (List<Path> sameSize : bySize.values()) {
            if (sameSize.size() < 2) {
                continue; // unique size -> can't be a duplicate of anything
            }
            for (Path path : sameSize) {
                String hash = sha256Hex(path);
                byHash.computeIfAbsent(hash, h -> new ArrayList<>()).add(path);
            }
        }

        // Step 3: only groups with 2+ files are actual duplicates.
        List<List<Path>> duplicateGroups = new ArrayList<>();
        for (List<Path> group : byHash.values()) {
            if (group.size() > 1) {
                duplicateGroups.add(group);
            }
        }
        return duplicateGroups;
    }

    /** Streams the file through a digest in fixed-size chunks -- avoids loading large files fully into memory. */
    private static String sha256Hex(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream in = Files.newInputStream(file)) {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e); // never happens on a standard JVM
        }
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) throws IOException {
        Path root = Files.createTempDirectory("dup-demo");
        Path dirA = Files.createDirectories(root.resolve("a"));
        Path dirB = Files.createDirectories(root.resolve("b"));

        Files.writeString(dirA.resolve("report.txt"), "identical contents");
        Files.writeString(dirB.resolve("backup-report.txt"), "identical contents"); // duplicate of the above
        Files.writeString(dirA.resolve("unique.txt"), "one of a kind");

        System.out.println("=== Duplicate groups ===");
        for (List<Path> group : findDuplicates(root)) {
            System.out.println("Group:");
            group.forEach(p -> System.out.println("  " + root.relativize(p)));
        }
    }
}
