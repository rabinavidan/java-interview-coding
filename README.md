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

## Cybersecurity-themed exercises

The exercises below are patterned after the kind of Java questions asked at
security/data-protection companies (Varonis, Tenable, AlgoSec style). Most
follow the same shape: read/parse -> aggregate in a `Map` -> filter/sort ->
output — the same skills as the exercises above, applied to security data.

### Log & event analysis

#### [`top-failed-logins`](src/top-failed-logins)
Parse an auth log and find the top N source IPs by failed login count.
Covers `HashMap` counting keyed by IP (not user), plus a bounded
min-`PriorityQueue` as the O(k log N) alternative to sorting all unique IPs.

#### [`brute-force-detection`](src/brute-force-detection)
Flag any user with 5+ failed logins within a rolling 60-second window.
Covers a per-user sliding window built on `ArrayDeque`, and why the window
must be scoped per user and driven by a monotonically increasing timestamp.

#### [`access-anomaly-detection`](src/access-anomaly-detection)
Given a baseline of `(user, resource)` accesses, flag new accesses to a
resource the user has never touched before — a simplified UEBA/insider-threat
signal. Covers `Set`-based membership checks and `getOrDefault` for
users with no baseline at all.

### File system & permissions

#### [`directory-traversal`](src/directory-traversal)
Recursively find files matching a glob pattern under a root directory and
sum bytes per top-level subfolder. Covers hand-rolled recursion vs.
`Files.walkFileTree` + `SimpleFileVisitor`, and handling unreadable
directories/broken symlinks without aborting the whole walk.

#### [`overexposed-files`](src/overexposed-files)
Given `(user, file, permission)` ACL tuples, find files accessible by more
than K distinct users — "overexposed data." Covers `Map<String, Set<String>>`
for free deduplication of repeated user/permission rows.

#### [`permission-escalation`](src/permission-escalation)
Given a nested group-membership graph, determine whether a user can
transitively reach an admin group. Covers BFS (for the shortest escalation
path) over a directed graph, with a `visited` set to guard against cycles.

### String/data validation

#### [`ipv4-validation`](src/ipv4-validation)
Validate IPv4 addresses and extract them from free-form text. Covers both
a regex approach (crafted to reject leading zeros and out-of-range octets)
and a from-scratch manual parser, plus safely extracting from noisy text
without regex-matching a substring of a longer invalid token.

#### [`sensitive-data-masking`](src/sensitive-data-masking)
Redact emails and credit card numbers in a log line (DLP-style scan).
Covers `Matcher.appendReplacement`/`appendTail` for single-pass
find-and-replace, and the false-positive problem with bare digit-run
matching (mentions Luhn validation as the real-world follow-up).

#### [`balanced-brackets`](src/balanced-brackets)
Classic stack-based bracket validation, extended to a lightweight
"does this look like intact JSON" structural check that treats
brackets inside string literals as plain text. Covers why a stack (not a
per-type counter) is required, and the two failure modes: an unmatched
closer and an unclosed opener.

### Dedup & frequency

#### [`duplicate-file-finder`](src/duplicate-file-finder)
Find duplicate files by content hash across nested directories without an
O(f²) all-pairs comparison. Covers grouping by file size first to cheaply
eliminate non-duplicates, then SHA-256 hashing only the remaining
candidates, streamed through a buffer instead of loading whole files into memory.

#### [`first-non-repeated-event`](src/first-non-repeated-event)
Find the first event ID in a stream that occurs exactly once. Covers
`LinkedHashMap` for order-preserving counting, plus a streaming variant
that answers "first non-repeated so far" in amortized O(1) per event using
a lazily-pruned `ArrayDeque` of candidates.
