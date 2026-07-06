# Java Interview Coding

Practice solutions for common coding-interview questions, with a focus on
explaining the *why* (complexity, trade-offs, common pitfalls) rather than
just the *what*.

## Exercises

### [`log-word-frequency`](src/log-word-frequency)
Count word frequency in a log file and return the top N most frequent
words. Covers `HashMap` counting, sorting a map by value, and the
classic imperative vs. Streams (`groupingBy` + `counting`) approaches.

### [`multithread-file-writing`](src/multithread-file-writing)
Read 3 text files concurrently (one thread per file) and merge their
contents into a single summary file. Covers synchronizing concurrent
writes to a shared resource, `CountDownLatch` coordination, and the
distinction between atomicity (guaranteed) and ordering (not guaranteed)
when multiple threads race to write. See
[`question.md`](src/multithread-file-writing/question.md) for the full
problem statement and discussion points.

### [`multithread-file-writing-python`](src/multithread-file-writing-python)
Python port of the exercise above, using `threading.Thread` and
`threading.Lock`. Also discusses why the GIL alone doesn't make
synchronization unnecessary for this kind of shared-resource write.
