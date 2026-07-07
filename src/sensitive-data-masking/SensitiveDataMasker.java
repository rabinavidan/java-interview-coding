import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Question 10: Redact emails and credit card numbers in a log line (DLP-style scan).
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity: O(n) single pass over the line per pattern, n = line length.
 */
public class SensitiveDataMasker {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})");

    // 13-19 digits total, optionally grouped in blocks separated by a single space or hyphen.
    // NOTE: a real system would follow this with a Luhn checksum pass to cut false positives
    // (phone numbers / order IDs also match a bare digit run of this length).
    private static final Pattern CARD_PATTERN =
            Pattern.compile("\\b\\d(?:[ -]?\\d){11,17}\\d\\b");

    /** Masks an email's local part, keeping only the first character: john@x.com -> j***@x.com. */
    public static String maskEmails(String line) {
        Matcher matcher = EMAIL_PATTERN.matcher(line);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String localPart = matcher.group(1);
            String domain = matcher.group(2);
            String masked = localPart.charAt(0) + "*".repeat(localPart.length() - 1) + "@" + domain;
            matcher.appendReplacement(result, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Masks all but the last 4 digits of any credit-card-shaped digit run. */
    public static String maskCreditCards(String line) {
        Matcher matcher = CARD_PATTERN.matcher(line);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(maskDigitsKeepLast4(matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Replaces every digit with '*' except the last 4 digit characters; separators (space/hyphen) untouched. */
    private static String maskDigitsKeepLast4(String cardMatch) {
        int totalDigits = (int) cardMatch.chars().filter(Character::isDigit).count();
        StringBuilder masked = new StringBuilder(cardMatch.length());
        int digitsSeen = 0;

        for (char c : cardMatch.toCharArray()) {
            if (Character.isDigit(c)) {
                digitsSeen++;
                masked.append(digitsSeen > totalDigits - 4 ? c : '*');
            } else {
                masked.append(c); // preserve spaces/hyphens as-is
            }
        }
        return masked.toString();
    }

    public static String redact(String line) {
        return maskCreditCards(maskEmails(line));
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        String logLine = "User john.doe@example.com paid with card 4111 1111 1111 1111 "
                + "(backup contact: jane_smith@corp.co, backup card 4111-1111-1111-1111)";

        System.out.println("=== Original ===");
        System.out.println(logLine);

        System.out.println("\n=== Redacted ===");
        System.out.println(redact(logLine));
    }
}
