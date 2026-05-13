package com.propertykey.util.addressparser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes address strings into individual tokens Handles punctuation, special characters, and
 * preserves important formatting
 */
public class AddressTokenizer {

    // Tokenization pattern: splits on whitespace but keeps punctuation attached
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\S+");

    /**
     * Tokenize an address string into individual tokens Preserves punctuation attached to words for
     * better feature extraction
     */
    public List<String> tokenize(String address) {
        if (address == null || address.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(address.trim());

        while (matcher.find()) {
            String token = matcher.group();
            // Further split if needed (e.g., "Apt#5" -> "Apt", "#", "5")
            tokens.addAll(splitToken(token));
        }

        return tokens;
    }

    /**
     * Split a single token if it contains certain patterns Example: "Apt#5" -> ["Apt", "#", "5"]
     */
    private List<String> splitToken(String token) {
        List<String> result = new ArrayList<>();

        // Handle hash/pound sign for unit numbers
        if (token.contains("#") && !token.equals("#")) {
            String[] parts = token.split("#", -1);
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    result.add(parts[i]);
                }
                if (i < parts.length - 1) {
                    result.add("#");
                }
            }
        } else {
            result.add(token);
        }

        return result;
    }

    /**
     * Clean and normalize an address string before tokenization Removes excessive whitespace,
     * normalizes punctuation
     */
    public String normalize(String address) {
        if (address == null) {
            return "";
        }

        // Normalize whitespace
        String normalized = address.replaceAll("\\s+", " ").trim();

        // Normalize common variations
        normalized = normalized.replaceAll("\\s+,\\s+", ", ");

        return normalized;
    }
}
