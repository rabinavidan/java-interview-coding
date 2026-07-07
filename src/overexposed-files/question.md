# Question 7: Overexposed Data — Files Accessible by More Than K Users

You're given a list of `(user, file, permission)` tuples describing an
access-control list dump. Find every file that is accessible (any
permission level) by **more than K distinct users** — Varonis calls this
"overexposed data," a top signal for excessive/stale permissions.

## Requirements
- Input: `List<AclEntry(user, file, permission)>`.
- A file counts a user once even if it has multiple permission rows for
  that user (e.g. both `READ` and `WRITE`).
- Output: files with `> K` distinct users, sorted by exposure descending.

## Interview discussion points
1. **`Map<String, Set<String>>` is the whole trick.** file -> set of users;
   inserting into a `Set` naturally dedupes repeated `(user, file)` rows
   with different permissions — no need to dedupe the input first.
2. **"More than K" vs "at least K".** Get the exact boundary from the
   interviewer and write it into a variable/comment (`> K` here) — off-by-one
   on this kind of filter is a classic real bug (K itself should NOT be
   flagged if the spec says "more than").
3. **Real-world extension:** groups. In practice permissions are often
   granted to a *group*, and a user's effective access is the union of
   their own grants plus every group they belong to (transitive, since
   groups can nest) — that's exactly the follow-up graph problem
   (`permission-escalation`) in this repo.

## Complexity
- O(n) to build the file -> users map, where n = number of ACL tuples.
- O(k log k) to sort k distinct files by exposure count.
- Space: O(n) worst case (every user has a distinct file/user pair).
