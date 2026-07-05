# Question 2: Multithreaded File Reading -> Summary File

You are given 3 text files. Spawn **one thread per file**. Each thread reads
its own file and writes its contents **directly** into a single shared
summary file. Wait for all threads to finish, then the summary file should
contain the contents of all 3 files, without any corruption or interleaved
garbage.

## Requirements
- One thread per input file.
- Each thread reads its file and writes into the *same* shared summary file
  (not just returns data to be written later by main).
- No corrupted/interleaved output, even though multiple threads write to the
  same file "at the same time".

## Interview discussion points
1. **Where's the race condition?** Multiple threads writing to the same
   `BufferedWriter`/file at once can interleave partial writes (e.g. one
   thread's `write()` call gets split by another thread's `write()` call),
   corrupting the output. `BufferedWriter` itself is not safe for this kind
   of unsynchronized concurrent use.
2. **Fix: synchronize the shared resource.** Every thread reads its file
   independently (safe, no shared state there), but the *write* to the
   shared writer happens inside a `synchronized` block on a common lock
   object, so only one thread writes at a time and each thread's block of
   lines is written atomically (no interleaving between threads).
3. **Coordinating completion.** Use a `CountDownLatch` (or `executor.awaitTermination`)
   so the main thread knows all writer threads are done before it closes the
   file and reads it back.
4. **Trade-off to call out in an interview:** synchronizing the write means
   the actual writing is effectively serialized — parallelism only speeds up
   the *reading* phase, not the writing phase. That's expected and fine;
   the point is correctness (no corruption), not maximum write throughput.

## Complexity
- Reading: O(n) total across all files, parallelized across 3 threads.
- Writing: O(n), serialized by the lock.
- Space: O(1) extra (we stream lines through, we don't hold everything in memory).
