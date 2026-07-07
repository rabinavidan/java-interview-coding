import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Question 6: Recursively find files matching a glob pattern under a root
 * directory, and sum file sizes per immediate top-level subfolder of root.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - O(f) where f = number of files/directories visited under root.
 *  - Space: O(f) worst case for matches, O(k) for k top-level subfolders.
 */
public class DirectoryTraversal {

    public record ScanResult(List<Path> matches, Map<String, Long> bytesByTopFolder) {}

    /* =========================================================
     * Approach 1: Hand-rolled recursion. START here in an
     * interview to prove you understand the base/recursive case.
     * ========================================================= */
    public static void walkClassic(Path dir, PathMatcher matcher, List<Path> matchesOut) {
        List<Path> children;
        try (var stream = Files.list(dir)) {
            children = stream.toList();
        } catch (IOException e) {
            return; // unreadable directory -> skip, don't abort the whole walk
        }

        for (Path child : children) {
            if (Files.isDirectory(child)) {
                walkClassic(child, matcher, matchesOut); // recursive case
            } else if (matcher.matches(child.getFileName())) {
                matchesOut.add(child); // base case: a matching file
            }
        }
    }

    /* =========================================================
     * Approach 2: Files.walkFileTree + SimpleFileVisitor.
     * Production-grade: visitFileFailed lets us skip permission
     * errors/broken symlinks instead of the whole walk throwing.
     * ========================================================= */
    public static ScanResult scan(Path root, String globPattern) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        List<Path> matches = new ArrayList<>();
        Map<String, Long> bytesByTopFolder = new HashMap<>();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (matcher.matches(file.getFileName())) {
                    matches.add(file);
                }
                topLevelFolder(root, file).ifPresent(folder ->
                        bytesByTopFolder.merge(folder, attrs.size(), Long::sum));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Permission denied, broken symlink, etc. -> skip and keep walking.
                return FileVisitResult.CONTINUE;
            }
        });

        return new ScanResult(matches, bytesByTopFolder);
    }

    /** Returns the name of the immediate child of root that contains `file`, if any. */
    private static java.util.Optional<String> topLevelFolder(Path root, Path file) {
        Path relative = root.relativize(file);
        if (relative.getNameCount() < 2) {
            return java.util.Optional.empty(); // file lives directly in root, no subfolder
        }
        return java.util.Optional.of(relative.getName(0).toString());
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) throws IOException {
        Path root = Files.createTempDirectory("scan-demo");
        Path logsDir = Files.createDirectories(root.resolve("logs"));
        Path srcDir = Files.createDirectories(root.resolve("src/nested"));

        Files.writeString(logsDir.resolve("app.log"), "1234567890");     // 10 bytes
        Files.writeString(logsDir.resolve("error.log"), "12345");        // 5 bytes
        Files.writeString(srcDir.resolve("Main.java"), "1234567890123"); // 13 bytes

        ScanResult result = scan(root, "*.log");

        System.out.println("=== Files matching *.log ===");
        result.matches().forEach(p -> System.out.println(root.relativize(p)));

        System.out.println("\n=== Bytes per top-level folder ===");
        result.bytesByTopFolder().forEach((folder, bytes) ->
                System.out.printf("%-8s -> %d bytes%n", folder, bytes));
    }
}
