package com.example.factoryguard.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultIdExtractor {

    private static final Pattern[] PATTERNS = {
        Pattern.compile("result_?id\\s*[=:]\\s*(\\d+)"),
        Pattern.compile("검사결과\\s*(?:번호|id)?\\s*[=:]\\s*(\\d+)"),
        Pattern.compile("(?:검사|검색)?결과\\s+(\\d+)\\s*번?"),
        Pattern.compile("검사결과\\s*[=:]\\s*(\\d+)"),
    };

    public static Long extractResultId(String question) {
        if (question == null || question.isBlank()) {
            return null;
        }

        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(question);
            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }
}
