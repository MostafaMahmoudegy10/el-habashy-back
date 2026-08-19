package com.example.elhabashyback.listing.service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ListingSearchQuery {

    private static final int MAX_TERMS = 8;
    private static final int MAX_TERM_LENGTH = 40;

    private ListingSearchQuery() {
    }

    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(decomposed.length());
        boolean previousWasSpace = true;
        for (int offset = 0; offset < decomposed.length();) {
            int codePoint = decomposed.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (Character.getType(codePoint) == Character.NON_SPACING_MARK || codePoint == '\u0640') {
                continue;
            }
            int mapped = normalizeArabicLetter(codePoint);
            if (Character.isLetterOrDigit(mapped)) {
                normalized.appendCodePoint(mapped);
                previousWasSpace = false;
            } else if (!previousWasSpace) {
                normalized.append(' ');
                previousWasSpace = true;
            }
        }
        return normalized.toString().trim();
    }

    static String toPrefixTsQuery(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }

        Set<String> terms = new LinkedHashSet<>();
        for (String term : normalized.split("\\s+")) {
            if (terms.size() == MAX_TERMS) {
                break;
            }
            terms.add(term.substring(0, Math.min(term.length(), MAX_TERM_LENGTH)));
        }
        return terms.stream()
                .map(ListingSearchQuery::prefixExpression)
                .reduce((left, right) -> left + " & " + right)
                .orElse("");
    }

    private static String prefixExpression(String term) {
        if (!isArabic(term)) {
            return term + ":*";
        }

        String root = stripArabicPrefix(term);
        Set<String> alternatives = new LinkedHashSet<>();
        alternatives.add(term);
        alternatives.add(root);
        alternatives.add("ب" + root);
        alternatives.add("و" + root);
        alternatives.add("ال" + root);
        alternatives.add("بال" + root);
        alternatives.add("وال" + root);
        return alternatives.stream()
                .filter(value -> value.length() > 2)
                .map(value -> value + ":*")
                .reduce((left, right) -> left + " | " + right)
                .map(value -> "(" + value + ")")
                .orElse(term + ":*");
    }

    private static String stripArabicPrefix(String term) {
        for (String prefix : List.of("وال", "بال", "كال", "فال", "لل", "ال")) {
            if (term.startsWith(prefix) && term.length() - prefix.length() >= 3) {
                return term.substring(prefix.length());
            }
        }
        if (term.length() >= 4 && "وفبكل".indexOf(term.charAt(0)) >= 0) {
            return term.substring(1);
        }
        return term;
    }

    private static boolean isArabic(String term) {
        int codePoint = term.codePointAt(0);
        return (codePoint >= 0x0600 && codePoint <= 0x06ff)
                || (codePoint >= 0x0750 && codePoint <= 0x077f)
                || (codePoint >= 0x08a0 && codePoint <= 0x08ff);
    }

    private static int normalizeArabicLetter(int codePoint) {
        return switch (codePoint) {
            case '\u0623', '\u0625', '\u0622', '\u0671' -> '\u0627';
            case '\u0649' -> '\u064A';
            case '\u0624' -> '\u0648';
            case '\u0626' -> '\u064A';
            case '\u0629' -> '\u0647';
            default -> codePoint;
        };
    }
}
