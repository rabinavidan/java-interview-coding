# Question 10: Redact Sensitive Data in Log Lines

Given a raw log line, find and mask:
- **Email addresses** -> keep the first character of the local part, mask
  the rest: `john.doe@example.com` -> `j*******@example.com`.
- **Credit card numbers** (13-19 digits, optionally grouped with spaces or
  hyphens in blocks of 4) -> keep only the last 4 digits:
  `4111 1111 1111 1111` -> `**** **** **** 1111`.

This is a classic DLP (data-loss-prevention) exercise — Varonis-style
products scan logs/files for exactly this kind of exposure.

## Requirements
- Preserve the rest of the log line untouched — only the matched spans get masked.
- Multiple matches per line must all get masked.
- Be ready to do it **without regex**: same idea, but you scan character-by-character
  looking for `@` (email) or runs of digit-ish characters of the right length (card number).

## Interview discussion points
1. **Why keep the domain but mask the local part?** Practical DLP balance —
   the domain is often useful for triage (is this even our email domain?)
   while the local part is the actual PII.
2. **Credit card false positives.** A naive `\d{13,19}` will also match
   phone numbers, order IDs, timestamps concatenated together, etc. — in a
   real system you'd add a Luhn checksum validation pass before treating a
   digit run as a real card number (mention this even if you don't
   implement full Luhn, it shows awareness of the FP problem).
3. **Regex vs. manual scan.** The regex version is shorter; the manual scan
   version is what proves you understand the underlying character
   classification — have both ready.

## Complexity
- O(n) where n = length of the log line (single pass with regex `Matcher.find` in a loop,
  or a manual single-pass scan).
