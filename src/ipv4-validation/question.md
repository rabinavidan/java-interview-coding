# Question 9: Validate and Extract IPv4 Addresses

1. Write `isValidIpv4(String s)`: returns true iff `s` is a valid dotted-quad
   IPv4 address (`0-255` per octet, no leading zeros like `01`, exactly 4
   octets).
2. Write `extractIpv4Addresses(String text)`: scan free-form text (e.g. a
   raw log blob) and return every substring that is a valid IPv4 address.

## Requirements
- Reject: too few/many octets, octet > 255, leading zeros (`192.168.01.1`
  is invalid — this trips people up), non-numeric octets, trailing garbage
  (`1.2.3.4.5`, `1.2.3.4abc`).
- Accept: `0.0.0.0`, `255.255.255.255`, `10.0.0.1`.
- Be ready to implement validation **without regex** too — interviewers at
  security companies often ask this specifically to test whether you
  understand what the regex is actually doing under the hood.

## Interview discussion points
1. **The leading-zero trap.** `"192.168.01.1"` looks fine to a naive
   regex like `\d{1,3}` — the fix is either a smarter regex
   (`(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)`) or, more robustly, split on `.`
   and validate each octet manually (check for leading `0` with length > 1,
   then parse and range-check).
2. **Manual validation is more auditable.** `split("\\.")` + per-octet
   numeric/range checks is easier to reason about and test than a dense
   regex, and it's the version you can write from scratch under pressure.
3. **Extraction vs. validation.** Extracting from free text can't just
   regex-search for the pattern and stop — you must ensure the match isn't
   part of a longer non-IP token (`1.2.3.4.5` shouldn't yield `2.3.4.5`
   internally) — anchor / boundary-check carefully, or validate candidate
   tokens split on whitespace instead of blindly regex-searching.

## Complexity
- Validation: O(1) — fixed at 4 octets.
- Extraction: O(n) where n = length of the text being scanned.
