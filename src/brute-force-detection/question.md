# Question 4: Brute-Force Detection (Sliding Window)

Given a chronologically-sorted stream of failed login events
`(user, timestampEpochSeconds)`, flag any user who has **5 or more failed
logins within any rolling 60-second window**.

## Requirements
- The window is *rolling*, not fixed-bucket: 5 failures at
  `t=0,10,20,30,40` trips the alert, but the same 5 failures spread across
  `t=0,15,35,55,75` (a 75s span) should NOT trip it — only check windows
  where `latest - earliest <= 60`.
- Return the set (or list) of users flagged, plus optionally the timestamp
  at which each first tripped the threshold.
- Events for different users are interleaved in the input; don't assume
  they're pre-grouped.

## Interview discussion points
1. **Per-user sliding window via Deque.** Keep a `Map<String, Deque<Long>>`
   of each user's recent failure timestamps. On a new event, push it, then
   pop from the front while `newest - front > 60` — the deque only ever
   holds timestamps inside the current window, so `size() >= 5` after
   trimming is the trip condition.
2. **Why a Deque and not a List?** Both ends matter: push at the back
   (new event), pop from the front (expired events) — `ArrayDeque` gives
   O(1) for both; an `ArrayList` would be O(n) to remove from the front.
3. **Global vs. per-user state.** Emphasize that the window is scoped per
   user (an attacker's failures for user A shouldn't count against user B).
4. **What if input isn't sorted?** Clarify that assumption up front — an
   unsorted stream would need a different structure (e.g. a min-heap per
   user, or sort by timestamp first) since a simple deque relies on
   monotonically increasing timestamps.

## Complexity
- Each event is pushed once and popped at most once -> O(n) total across
  all events, not O(n) per event.
- Space: O(n) worst case (all events for one user still in-window).
