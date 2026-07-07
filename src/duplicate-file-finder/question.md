# Question 12: Find Duplicate Files by Content Hash Across Directories

Given a root directory (possibly with many nested subdirectories), find
groups of files that have **identical content**, even if their names or
locations differ. This is standard storage-optimization / data-governance
tooling — "we have 40TB of file shares, how much of it is redundant copies."

## Requirements
- Two files are duplicates iff their **content** is byte-identical — name
  and location don't matter.
- Return only groups with 2+ files (unique files aren't "duplicates").
- Must scale reasonably: don't naively do an O(f²) byte-by-byte comparison
  of every file against every other file.

## Interview discussion points
1. **Size first, then hash.** Two files can only be duplicates if they're
   the same size — group by size first (cheap, from file metadata, no I/O)
   to eliminate most non-duplicate pairs before paying for a content hash.
   Only compute a hash for files that share a size with at least one other file.
2. **Which hash?** SHA-256 (via `MessageDigest`) is the standard interview
   answer — collision-resistant enough that "same hash" is treated as "same
   content" in practice. Mention that a true zero-false-positive system
   would still do a final byte-by-byte compare on hash collisions, but that's
   usually out of scope to implement.
3. **Streaming the hash.** Read files in fixed-size chunks
   (`DigestInputStream` or manual buffer loop) rather than
   `Files.readAllBytes` — large files shouldn't be loaded fully into memory
   just to hash them.
4. **Map<String, List<Path>> is the final data structure** — hash -> paths
   sharing that hash; filter to entries with `size() > 1`.

## Complexity
- Listing files: O(f), f = total files under root.
- Size-based pre-grouping: O(f).
- Hashing only same-size candidates: O(b) total bytes read across those
  candidates (much less than all f files in the common case).
- Space: O(f).
