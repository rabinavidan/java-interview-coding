# Question 6: Recursive Directory Traversal — Matching Files & Per-Folder Size

Given a root directory, recursively find all files whose name matches a
glob pattern (e.g. `*.log`), and report the **total size in bytes per
immediate subfolder** of the root, plus the full list of matches.

This mirrors real Varonis/data-classification work: crawl a file share,
find files of interest, and roll up size/count per folder for a report.

## Requirements
- Recurse into arbitrarily nested subdirectories.
- Match files by a simple glob (`*` wildcard) against the file name, not
  the full path.
- Report total bytes per **top-level** subfolder of the root (not every
  nested folder — keep the aggregation level simple and clarify this
  up front in an interview).
- Handle symlinks / unreadable directories without crashing the whole walk.

## Interview discussion points
1. **`Files.walk` vs. hand-rolled recursion.** `Files.walk(root)` is the
   idiomatic NIO.2 answer and handles depth-first traversal for you, but
   interviewers often want to see you write the recursion by hand first
   (base case: regular file; recursive case: directory) to prove you
   understand the mechanics — show both.
   `Files.walkFileTree` with a `SimpleFileVisitor` is the production-grade
   answer since it lets you handle `visitFileFailed` (permission errors,
   broken symlinks) instead of the walk throwing and aborting.
2. **Why glob and not regex directly?** `PathMatcher` with `"glob:" + pattern`
   is what `java.nio.file` gives you out of the box and is what users
   actually type (`*.log`), but knowing you could translate glob -> regex
   by hand (`*` -> `.*`, escape everything else) is a good fallback if
   asked to implement it without library support.
3. **Aggregation key choice.** Rolling up by *immediate child of root*
   (not full nested path) mirrors "give me a report per top-level share"
   — a natural follow-up is "now do it per-folder at every depth," which
   just changes the map key to the parent path instead of the top ancestor.

## Complexity
- O(f) where f = total number of files/directories under root (each visited once).
- Space: O(f) worst case for the match list, O(k) for k top-level subfolders.
