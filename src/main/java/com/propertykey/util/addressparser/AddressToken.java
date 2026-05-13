package com.propertykey.util.addressparser;

/**
 * Represents a single token in an address with its assigned label
 */
public class AddressToken {
    private final String token;
    private final String label;
    private final double confidence;

    public AddressToken(String token, String label, double confidence) {
        this.token = token;
        this.label = label;
        this.confidence = confidence;
    }

    public String getToken() {
        return token;
    }

    public String getLabel() {
        return label;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %.3f)", token, label, confidence);
    }
}
