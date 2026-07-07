import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Question 11: Balanced-bracket validation, plus a lightweight "does this
 * look like intact JSON" structural check.
 *
 * See question.md for the full problem statement and discussion points.
 *
 * Complexity: O(n) single pass, O(n) space worst case (all openers).
 */
public class BalancedBrackets {

    private static final Map<Character, Character> CLOSE_TO_OPEN = Map.of(
            ')', '(',
            ']', '[',
            '}', '{'
    );

    /** True iff every bracket in s is properly opened, nested, and closed. Non-bracket chars are ignored. */
    public static boolean isBalanced(String s) {
        Deque<Character> stack = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (CLOSE_TO_OPEN.containsKey(c)) {
                // A closer with nothing open, or the wrong opener on top -> invalid.
                if (stack.isEmpty() || stack.pop() != CLOSE_TO_OPEN.get(c)) {
                    return false;
                }
            }
            // Any other character is irrelevant structural noise -> ignored.
        }
        return stack.isEmpty(); // non-empty means some opener was never closed
    }

    /**
     * Lightweight structural check: brackets balance AND every quoted string
     * is properly closed, so brackets inside string literals ("[test") are
     * treated as literal text, not structural characters. Not a real parser.
     */
    public static boolean isPlausibleJson(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        boolean inString = false;
        boolean escaped = false;

        for (char c : s.toCharArray()) {
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue; // brackets inside a string literal are just text
            }

            if (c == '"') {
                inString = true;
            } else if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (CLOSE_TO_OPEN.containsKey(c)) {
                if (stack.isEmpty() || stack.pop() != CLOSE_TO_OPEN.get(c)) {
                    return false;
                }
            }
        }
        return stack.isEmpty() && !inString; // unterminated string -> also invalid
    }

    /* ========================= DEMO ========================= */
    public static void main(String[] args) {
        String[] bracketCases = {
                "()[]{}",   // balanced
                "([{}])",   // balanced, nested
                "([)]",     // WRONG: crossed nesting despite matching counts
                "(((",      // WRONG: unclosed
                ")",        // WRONG: closer with nothing open
                "foo(bar[baz]);" // balanced, ignoring non-bracket noise
        };
        System.out.println("=== isBalanced ===");
        for (String s : bracketCases) {
            System.out.printf("%-20s -> %b%n", s, isBalanced(s));
        }

        String[] jsonCases = {
                "{\"key\": \"[not a bracket]\"}", // balanced; bracket-looking text is inside a string
                "{\"key\": \"value\"",             // WRONG: missing closing brace
                "{\"key\": \"unterminated"         // WRONG: unterminated string
        };
        System.out.println("\n=== isPlausibleJson ===");
        for (String s : jsonCases) {
            System.out.printf("%-32s -> %b%n", s, isPlausibleJson(s));
        }
    }
}
