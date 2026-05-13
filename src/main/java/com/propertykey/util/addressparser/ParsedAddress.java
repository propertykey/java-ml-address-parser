package com.propertykey.util.addressparser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a parsed address with component tokens and their labels
 */
public class ParsedAddress {
    private final List<AddressToken> tokens;
    private final Map<String, String> components;

    public ParsedAddress(List<AddressToken> tokens) {
        this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
        this.components = extractComponents(tokens);
    }

    /**
     * Extract components by grouping consecutive tokens with the same label
     */
    private Map<String, String> extractComponents(List<AddressToken> tokens) {
        Map<String, List<String>> componentParts = new LinkedHashMap<>();

        for (AddressToken token : tokens) {
            componentParts.computeIfAbsent(token.getLabel(), k -> new ArrayList<>())
                    .add(token.getToken());
        }

        // Join multiple tokens for same component
        return componentParts.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(" ", e.getValue()),
                        (v1, v2) -> v1, // Keep first occurrence in case of duplicates
                        LinkedHashMap::new));
    }

    public List<AddressToken> getTokens() {
        return tokens;
    }

    public Map<String, String> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    public String getComponent(String label) {
        return components.get(label);
    }

    @Override
    public String toString() {
        return components.toString();
    }

    /**
     * Pretty print the parsed address
     */
    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parsed Address Components:\n");
        for (Map.Entry<String, String> entry : components.entrySet()) {
            sb.append(String.format("  %-25s: %s\n", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}
