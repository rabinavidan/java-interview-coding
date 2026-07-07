# Question 11: Balanced Brackets / Valid JSON-ish Structure Check

1. `isBalanced(String s)`: given a string containing `()[]{}` (possibly mixed
   with other characters, e.g. a config/log blob), return whether every
   bracket is properly opened and closed in the right order.
2. Extension: `isPlausibleJson(String s)` — a lightweight structural check
   (not a full parser) that brackets/braces balance **and** quoted strings
   are properly closed, so a truncated or corrupted JSON payload (a common
   symptom of a log-shipping bug or a tampered config file) is flagged
   before you even try to `JSON.parse` it.

## Requirements
- Ignore non-bracket characters for part 1.
- Track brackets that appear *inside* string literals as literal text, not
  structural characters, for part 2 (i.e. `"[test"` shouldn't count as an
  unclosed `[`).
- Return false (not throw) on malformed input — this is a validator, not a parser.

## Interview discussion points
1. **Stack is the only correct tool here.** Push on open bracket, on a
   close bracket pop and check it matches the expected opener — a counter
   per bracket type is NOT sufficient (`([)]` has balanced counts of each
   bracket type but is not validly nested).
2. **Early exits.** Pop from an empty stack (a closer with no matching
   opener) and a non-empty stack at end of string (an opener never closed)
   are the two failure modes beyond "wrong closer" — make sure both are handled.
3. **Why "JSON-ish" and not a real parser?** A real JSON parser is a much
   bigger exercise (recursive descent, handling numbers/escapes/unicode).
   The interview-sized version is deliberately a structural sanity check:
   good enough to say "this payload looks intact" without building an
   actual parser — call this scoping decision out explicitly.

## Complexity
- O(n) single pass, n = string length.
- Space: O(n) worst case (all-opener string, e.g. `"((((("`).
