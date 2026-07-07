# Question 5: New-Resource Access Anomaly Detection

You have a historical baseline of `(user, resource)` access pairs — "things
this user has legitimately touched before" — and a new batch of access
events. Find every `(user, resource)` pair in the new batch where the user
is accessing a resource **they have never accessed in the baseline**.

This is a simplified version of Varonis-style UEBA (user & entity behavior
analytics): a user suddenly touching a file share they've never touched
before is a classic insider-threat / lateral-movement signal.

## Requirements
- Baseline: `Map<String, Set<String>>` of user -> resources previously accessed.
- New events: a list of `(user, resource)` pairs.
- Output: the new events that are anomalous (resource not in that user's
  baseline set), preserving input order.
- A user with **no baseline at all** (brand-new account) should have every
  access flagged as anomalous.

## Interview discussion points
1. **Why `Set<String>` and not `List<String>`?** Membership check is the
   only operation needed -> `HashSet.contains` is O(1) average vs O(n) for
   a list scan. With potentially millions of resources per user, this
   matters.
2. **Baseline miss vs. empty set.** `Map.getOrDefault(user, Set.of())` cleanly
   handles "no baseline" without a null check or an extra `containsKey`.
3. **Extending it:** a real system would also track *when* the baseline was
   built and expire old entries, weight by resource sensitivity, and rate
   anomalies rather than binary flag them — good follow-up discussion but
   out of scope for the core exercise.

## Complexity
- Building lookups: O(1) (baseline is already provided as a map).
- Scanning new events: O(m) where m = number of new events, each check O(1) average.
- Space: O(1) extra beyond the input structures.
