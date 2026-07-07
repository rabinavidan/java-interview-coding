# Question 13: First Non-Repeated Event ID in a Stream

Given a stream (list) of event IDs as they arrive, return the first event
ID that occurs **exactly once**, preserving the original arrival order. If
every ID repeats, return `null`/empty.

A common framing: SIEM event stream where you want the first "unique,
never-repeated" event to investigate, as opposed to noisy repeated events.

## Requirements
- Preserve arrival order when picking among candidates for "first".
- O(n) time — no repeated scans of the whole input per candidate.
- Also support the **streaming** variant: process one event at a time and
  be able to answer "what is the first non-repeated ID so far?" at any
  point, without re-scanning from the start each time.

## Interview discussion points
1. **`LinkedHashMap<Long, Integer>` is the key insight.** It's a HashMap
   (O(1) counting) that also preserves insertion order (so the first
   candidate in iteration order among count==1 entries is the correct
   answer) — a plain `HashMap` would give you the right *count* but the
   wrong *order* when you scan for "first."
2. **One-pass vs two-pass.** For the batch version you still need two
   passes minimum: one to count everything, one to find the first with
   count 1 (you can't know an ID is "non-repeated" until you've seen the
   whole stream, or at least know no more of that ID is coming).
3. **Streaming variant ("first non-repeated so far").** Maintain the same
   `LinkedHashMap<Long, Integer>` counts *and* a separate ordered structure
   (e.g. a `Deque` of candidates) that you lazily prune: peek the front, if
   its count is no longer 1 pop it and check the next. Each ID is pushed
   once and popped at most once -> amortized O(1) per new event, not O(n)
   per query.

## Complexity
- Batch version: O(n) time, O(k) space (k = unique IDs).
- Streaming version: amortized O(1) per event.
