package com.training.loan.ai;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Defence-in-depth guardrail: the domain model never collects protected
 * characteristics, but free-text fields and uploaded documents may mention
 * them. Anything sent to the model passes through this sanitizer so the
 * model cannot observe - and therefore cannot use - protected attributes.
 */
public final class ProtectedDataSanitizer {

    private static final List<Pattern> PROTECTED_TERMS = List.of(
            Pattern.compile("\\b(race|racial|ethnicity|ethnic|religion|religious|gender|sex|"
                    + "sexual orientation|disability|disabled|marital status|married|single|"
                    + "divorced|widowed|pregnant|pregnancy|national origin|citizenship status|"
                    + "age|date of birth|veteran status)\\b", Pattern.CASE_INSENSITIVE)
    );

    private ProtectedDataSanitizer() {
    }

    public static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        String result = text;
        for (Pattern p : PROTECTED_TERMS) {
            result = p.matcher(result).replaceAll("[redacted]");
        }
        return result;
    }
}
