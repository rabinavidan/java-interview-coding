import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Question 1: Word Frequency in a log file.
 *
 * Interview focus points:
 *  1. File reading with try-with-resources (resource safety).
 *  2. HashMap for O(1) average insert/lookup during counting.
 *  3. Sorting by VALUE (HashMap has no order -> sort the entrySet).
 *
 * Complexity:
 *  - Counting:  O(n)        (n = total words)
 *  - Sorting:   O(k log k)  (k = unique words)
 *  - Space:     O(k)
 */
public class WordFrequency {

    /* =========================================================
     * Approach 1: Classic imperative style.
     * Best to START with this in an interview - shows you
     * understand the mechanics before reaching for Streams.
     * ========================================================= */
    public static Map<String, Integer> countWordsClassic(Path logFile) throws IOException {
        Map<String, Integer> counts = new HashMap<>();

        // try-with-resources: the Stream (and underlying file handle)
        // is closed automatically, even if an exception is thrown.
        try (Stream<String> lines = Files.lines(logFile)) {
            lines.forEach(line -> {
                // Normalize: lowercase, then split on anything that is NOT a letter/digit.
                // "\\W+" splits on non-word characters (handles spaces, commas, brackets...).
                String[] words = line.toLowerCase().split("\\W+");
                for (String word : words) {
                    if (word.isEmpty()) continue;         // split can produce empty tokens
                    if (word.matches("\\d+")) continue;   // skip pure numbers (timestamps!)
                    // ^ Interview discussion point: without this filter, log timestamps
                    //   dominate the results. Always clarify "what counts as a word?"

                    // The interview gem: merge() replaces the containsKey if/else.
                    // If 'word' is absent -> put 1.
                    // If present -> apply Integer::sum on (oldValue, 1).
                    counts.merge(word, 1, Integer::sum);

                    // Equivalent alternative:
                    // counts.put(word, counts.getOrDefault(word, 0) + 1);
                }
            });
        }
        return counts;
    }

    /* =========================================================
     * Sorting a HashMap by VALUE (descending).
     * Key insight: a HashMap CANNOT be sorted in place.
     * We sort its entries and rebuild an ordered structure.
     * ========================================================= */
    public static List<Map.Entry<String, Integer>> topN(Map<String, Integer> counts, int n) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());

        // Comparator.comparingByValue() sorts ascending -> reversed() for descending.
        // Tie-breaker on the key gives deterministic output (nice interview touch).
        entries.sort(
            Map.Entry.<String, Integer>comparingByValue().reversed()
                     .thenComparing(Map.Entry.comparingByKey())
        );

        return entries.subList(0, Math.min(n, entries.size()));
    }

    /* =========================================================
     * Approach 2: Full Streams one-liner style.
     * groupingBy + counting is the idiomatic Java 8+ answer.
     * Mention it AFTER the classic version to show range.
     * ========================================================= */
    public static Map<String, Long> countWordsStreams(Path logFile) throws IOException {
        try (Stream<String> lines = Files.lines(logFile)) {
            return lines
                .flatMap(line -> Stream.of(line.toLowerCase().split("\\W+")))
                .filter(word -> !word.isEmpty())
                .filter(word -> !word.matches("\\d+")) // same numeric filter as above
                .collect(Collectors.groupingBy(
                        word -> word,          // classifier: the word itself is the key
                        Collectors.counting()  // downstream: count occurrences -> Long
                ));
        }
    }

    /** Streams version of Top-N, collected into a LinkedHashMap to PRESERVE sort order. */
    public static Map<String, Long> topNStreams(Map<String, Long> counts, int n) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,          // merge function (never triggered here)
                        LinkedHashMap::new    // CRITICAL: HashMap would lose the order!
                ));
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) throws IOException {
        // Create a sample log file for the demo
        Path logFile = Path.of("app.log");
        Files.writeString(logFile, String.join("\n",
                "2026-07-05 10:00:01 INFO  Connection established to database",
                "2026-07-05 10:00:02 ERROR Connection timeout while reading",
                "2026-07-05 10:00:03 INFO  Retry connection attempt 1",
                "2026-07-05 10:00:04 ERROR Connection refused by host",
                "2026-07-05 10:00:05 WARN  Connection pool nearly exhausted",
                "2026-07-05 10:00:06 INFO  Connection established to cache"
        ));

        System.out.println("=== Approach 1: Classic HashMap + merge() ===");
        Map<String, Integer> counts = countWordsClassic(logFile);
        topN(counts, 5).forEach(e ->
                System.out.printf("%-12s -> %d%n", e.getKey(), e.getValue()));

        System.out.println("\n=== Approach 2: Streams groupingBy + counting ===");
        Map<String, Long> streamCounts = countWordsStreams(logFile);
        topNStreams(streamCounts, 5).forEach((word, count) ->
                System.out.printf("%-12s -> %d%n", word, count));
    }
}
