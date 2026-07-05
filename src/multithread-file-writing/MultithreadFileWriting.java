import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Question 2: Read 3 text files with one thread per file, and have each
 * thread write its contents DIRECTLY into a single shared summary file.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Core idea: reading is independent per thread (no shared state), but
 * writing touches a SHARED resource (the summary file), so that part must
 * be synchronized to avoid interleaved/corrupted output.
 */
public class MultithreadFileWriting {

    public static void writeSummary(List<Path> inputFiles, Path summaryFile) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(inputFiles.size());
        CountDownLatch latch = new CountDownLatch(inputFiles.size());
        Object writeLock = new Object(); // guards the shared writer

        try (BufferedWriter writer = Files.newBufferedWriter(summaryFile)) {
            for (Path file : inputFiles) {
                executor.submit(() -> {
                    try {
                        // Reading is thread-local work: no shared state, no lock needed.
                        List<String> lines = Files.readAllLines(file);

                        // Writing touches the SHARED writer -> must be synchronized so
                        // one thread's block of lines can't get interleaved with another's.
                        synchronized (writeLock) {
                            writer.write("=== " + file.getFileName() + " ===");
                            writer.newLine();
                            for (String line : lines) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all 3 threads to finish writing before closing the file.
            latch.await();
        } finally {
            executor.shutdown();
        }
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) throws Exception {
        Path dir = Path.of(".");
        Path input1 = dir.resolve("input1.txt");
        Path input2 = dir.resolve("input2.txt");
        Path input3 = dir.resolve("input3.txt");

        Files.writeString(input1, "Alpha line one\nAlpha line two\n");
        Files.writeString(input2, "Beta line one\nBeta line two\n");
        Files.writeString(input3, "Gamma line one\nGamma line two\n");

        List<Path> inputs = List.of(input1, input2, input3);
        Path summary = dir.resolve("summary.txt");

        writeSummary(inputs, summary);

        System.out.println("=== summary.txt (written concurrently by 3 threads) ===");
        Files.readAllLines(summary).forEach(System.out::println);
    }
}
