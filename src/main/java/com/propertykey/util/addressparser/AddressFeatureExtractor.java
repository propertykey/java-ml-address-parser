package com.propertykey.util.addressparser;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Extracts features from address tokens for CRF model Features match usaddress's approach: token
 * characteristics, position, context
 */
public class AddressFeatureExtractor {

    private static final Pattern NUMERIC = Pattern.compile("\\d");
    private static final Pattern ALL_DIGITS = Pattern.compile("^\\d+$");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern ALL_CAPS = Pattern.compile("^[A-Z]+$");
    private static final Pattern INITIAL_CAP = Pattern.compile("^[A-Z][a-z]");
    private static final Pattern ALL_LOWER = Pattern.compile("^[a-z]+$");
    private static final Pattern HAS_PUNCTUATION = Pattern.compile(".*[,.;:!?].*");

    // Common street suffixes
    private static final Set<String> STREET_TYPES = new HashSet<>(Arrays.asList("ALLEY", "ALY",
            "AVENUE", "AVE", "BOULEVARD", "BLVD", "CIRCLE", "CIR", "COURT", "CT", "DRIVE", "DR",
            "LANE", "LN", "PARKWAY", "PKWY", "PLACE", "PL", "ROAD", "RD", "SQUARE", "SQ", "STREET",
            "ST", "TERRACE", "TER", "TRAIL", "TRL", "WAY"));

    // Directionals
    private static final Set<String> DIRECTIONALS =
            new HashSet<>(Arrays.asList("N", "S", "E", "W", "NE", "NW", "SE", "SW", "NORTH",
                    "SOUTH", "EAST", "WEST", "NORTHEAST", "NORTHWEST", "SOUTHEAST", "SOUTHWEST"));

    // Occupancy types
    private static final Set<String> OCCUPANCY_TYPES =
            new HashSet<>(Arrays.asList("APT", "APARTMENT", "SUITE", "STE", "UNIT", "BLDG",
                    "BUILDING", "FLOOR", "FL", "ROOM", "RM", "#"));

    // US State abbreviations
    private static final Set<String> STATES = new HashSet<>(
            Arrays.asList("AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID",
                    "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO",
                    "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA",
                    "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY", "DC"));

    /**
     * Extract features for a single token given its context
     */
    public Map<String, String> extractFeatures(String token, int position, int totalTokens,
            String prevToken, String nextToken) {
        Map<String, String> features = new HashMap<>();

        String cleanToken = token.replaceAll("[,.]", ""); // Remove trailing punctuation for feature
                                                          // extraction
        String upperToken = cleanToken.toUpperCase();

        // Token identity features
        features.put("token", token.toLowerCase());
        features.put("token.upper", upperToken);

        // Length features
        features.put("length", String.valueOf(token.length()));
        features.put("short", token.length() <= 3 ? "true" : "false");

        // Numeric features
        features.put("digits", ALL_DIGITS.matcher(cleanToken).matches() ? "all"
                : HAS_DIGIT.matcher(cleanToken).matches() ? "some" : "none");

        // Case features
        if (ALL_CAPS.matcher(cleanToken).matches()) {
            features.put("case", "upper");
        } else if (INITIAL_CAP.matcher(cleanToken).matches()) {
            features.put("case", "title");
        } else if (ALL_LOWER.matcher(cleanToken).matches()) {
            features.put("case", "lower");
        } else {
            features.put("case", "mixed");
        }

        // Punctuation
        features.put("has_punct", HAS_PUNCTUATION.matcher(token).matches() ? "true" : "false");

        // Word type features
        features.put("street_type", STREET_TYPES.contains(upperToken) ? "true" : "false");
        features.put("directional", DIRECTIONALS.contains(upperToken) ? "true" : "false");
        features.put("occupancy_type", OCCUPANCY_TYPES.contains(upperToken) ? "true" : "false");
        features.put("state", STATES.contains(upperToken) ? "true" : "false");

        // Position features
        features.put("position",
                position == 0 ? "first" : position == totalTokens - 1 ? "last" : "middle");
        features.put("position_idx", String.valueOf(position));

        // Context features (previous token)
        if (prevToken != null) {
            String prevUpper = prevToken.replaceAll("[,.]", "").toUpperCase();
            features.put("prev_token", prevToken.toLowerCase());
            features.put("prev_digits", ALL_DIGITS.matcher(prevToken).matches() ? "true" : "false");
            features.put("prev_street_type", STREET_TYPES.contains(prevUpper) ? "true" : "false");
        } else {
            features.put("prev_token", "BOS"); // Beginning of sequence
        }

        // Context features (next token)
        if (nextToken != null) {
            String nextUpper = nextToken.replaceAll("[,.]", "").toUpperCase();
            features.put("next_token", nextToken.toLowerCase());
            features.put("next_digits", ALL_DIGITS.matcher(nextToken).matches() ? "true" : "false");
            features.put("next_street_type", STREET_TYPES.contains(nextUpper) ? "true" : "false");
        } else {
            features.put("next_token", "EOS"); // End of sequence
        }

        // Zip code pattern (5 or 9 digits)
        if (cleanToken.matches("^\\d{5}(-\\d{4})?$")) {
            features.put("zipcode_pattern", "true");
        }

        return features;
    }

    /**
     * Extract features for all tokens in a sequence
     */
    public List<Map<String, String>> extractAllFeatures(List<String> tokens) {
        List<Map<String, String>> allFeatures = new ArrayList<>();
        int size = tokens.size();

        for (int i = 0; i < size; i++) {
            String prevToken = i > 0 ? tokens.get(i - 1) : null;
            String nextToken = i < size - 1 ? tokens.get(i + 1) : null;

            Map<String, String> features =
                    extractFeatures(tokens.get(i), i, size, prevToken, nextToken);
            allFeatures.add(features);
        }

        return allFeatures;
    }
}
