# Question 3: Top-N IPs by Failed Login Attempts

You are given an authentication log where each line has the form:

```
2026-07-05T10:00:01Z user=alice ip=203.0.113.7 status=FAILED
2026-07-05T10:00:02Z user=bob   ip=198.51.100.4 status=SUCCESS
```

Parse the log and return the top N source IPs ranked by number of `FAILED`
login attempts.

## Requirements
- Only count lines with `status=FAILED`.
- Group by `ip`, not by `user` (an attacker rotates users, reuses the IP).
- Return the top N IPs, most failures first, with a deterministic tie-break.

## Interview discussion points
1. **Why group by IP and not by user?** In credential-stuffing/brute-force
   traffic the attacker tries many usernames from one source; grouping by
   user would hide the pattern. Grouping by IP is the signal that matters
   for this kind of detection.
2. **HashMap counting + PriorityQueue vs. full sort.** For a fixed small N,
   a bounded min-heap of size N is O(k log N) instead of O(k log k) for
   sorting all k unique IPs — worth mentioning when k is huge (millions of
   distinct IPs) and N is small (top 10).
3. **Parsing robustness.** Real logs have malformed/partial lines — the
   parser should skip a line it can't parse rather than crash the whole job.

## Complexity
- Counting: O(n) where n = number of log lines.
- Top-N via heap: O(k log N) where k = unique IPs.
- Space: O(k).
