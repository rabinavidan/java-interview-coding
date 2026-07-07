import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Question 9: Validate IPv4 addresses and extract them from free-form text.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity:
 *  - Validation: O(1) (always exactly 4 octets).
 *  - Extraction: O(n), n = length of scanned text.
 */
public class Ipv4Validator {

    // Per-octet: 250-255, 200-249, 100-199, or 1-2 digits (no leading zero unless the octet is "0" itself).
    private static final String OCTET = "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)";
    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^" + OCTET + "\\." + OCTET + "\\." + OCTET + "\\." + OCTET + "$");

    /** Approach 1: regex, with the octet pattern crafted to reject leading zeros and >255. */
    public static boolean isValidIpv4Regex(String s) {
        return IPV4_PATTERN.matcher(s).matches();
    }

    /**
     * Approach 2: manual parsing, no regex. Show this when asked to implement
     * it "from scratch" -- splits on '.', then validates each octet's format
     * and range explicitly.
     */
    public static boolean isValidIpv4Manual(String s) {
        String[] octets = s.split("\\.", -1); // -1 keeps trailing empty strings (e.g. "1.2.3.")
        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3) {
                return false;
            }
            for (char c : octet.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return false; // rejects "1.2.3.4abc" and non-numeric octets
                }
            }
            if (octet.length() > 1 && octet.charAt(0) == '0') {
                return false; // rejects leading zeros like "01"
            }
            int value = Integer.parseInt(octet);
            if (value > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scans free-form text and returns every valid IPv4 address found.
     * Splits on non-IP-forming characters (whitespace/brackets/punctuation)
     * first, then validates each candidate token as a WHOLE address --
     * this avoids accidentally matching "2.3.4.5" out of "1.2.3.4.5".
     */
    public static List<String> extractIpv4Addresses(String text) {
        List<String> found = new ArrayList<>();
        // Candidate tokens: runs of digits and dots, bounded by anything else.
        Matcher tokenMatcher = Pattern.compile("[0-9.]+").matcher(text);

        while (tokenMatcher.find()) {
            String candidate = tokenMatcher.group();
            // Trim stray leading/trailing dots a naive token scan can pick up.
            candidate = candidate.replaceAll("^\\.+|\\.+$", "");
            if (isValidIpv4Manual(candidate)) {
                found.add(candidate);
            }
        }
        return found;
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        String[] testCases = {
                "192.168.1.1", "255.255.255.255", "0.0.0.0",
                "192.168.01.1",   // leading zero -> invalid
                "256.1.1.1",      // out of range -> invalid
                "1.2.3.4.5",      // too many octets -> invalid
                "1.2.3",          // too few octets -> invalid
                "1.2.3.4abc"      // trailing garbage -> invalid
        };

        System.out.println("=== Validation (regex vs manual) ===");
        for (String s : testCases) {
            System.out.printf("%-16s regex=%-5s manual=%-5s%n",
                    s, isValidIpv4Regex(s), isValidIpv4Manual(s));
        }

        String logBlob = "Connection from 10.0.0.5 refused; retry from 192.168.01.1 "
                + "then succeeded from 8.8.8.8, garbage token 1.2.3.4.5 ignored.";
        System.out.println("\n=== Extracted from log text ===");
        extractIpv4Addresses(logBlob).forEach(System.out::println);
    }
}
