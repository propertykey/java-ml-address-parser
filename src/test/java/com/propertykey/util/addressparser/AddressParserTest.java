package com.propertykey.util.addressparser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class AddressParserTest {

    @Test
    public void testTokenizer() {
        AddressTokenizer tokenizer = new AddressTokenizer();

        String address = "123 Main St Miami FL 33101";
        List<String> tokens = tokenizer.tokenize(address);

        assertEquals(6, tokens.size());
        assertEquals("123", tokens.get(0));
        assertEquals("Main", tokens.get(1));
        assertEquals("St", tokens.get(2));
        assertEquals("Miami", tokens.get(3));
        assertEquals("FL", tokens.get(4));
        assertEquals("33101", tokens.get(5));
    }

    @Test
    public void testTokenizerWithPunctuation() {
        AddressTokenizer tokenizer = new AddressTokenizer();

        String address = "456 Oak Ave, Tampa, FL";
        List<String> tokens = tokenizer.tokenize(address);

        assertTrue(tokens.contains("Ave,"));
        assertTrue(tokens.contains("Tampa,"));
    }

    @Test
    public void testTokenizerWithUnit() {
        AddressTokenizer tokenizer = new AddressTokenizer();

        String address = "789 Park Blvd Apt#5";
        List<String> tokens = tokenizer.tokenize(address);

        assertTrue(tokens.contains("Apt"));
        assertTrue(tokens.contains("#"));
        assertTrue(tokens.contains("5"));
    }

    @Test
    public void testFeatureExtractor() {
        AddressFeatureExtractor extractor = new AddressFeatureExtractor();

        Map<String, String> features = extractor.extractFeatures("123", 0, 5, null, "Main");

        assertEquals("all", features.get("digits"));
        assertEquals("first", features.get("position"));
        assertEquals("BOS", features.get("prev_token"));
        assertNotNull(features.get("next_token"));
    }

    @Test
    public void testFeatureExtractorStreetType() {
        AddressFeatureExtractor extractor = new AddressFeatureExtractor();

        Map<String, String> features = extractor.extractFeatures("St", 2, 5, "Main", "Miami");

        assertEquals("true", features.get("street_type"));
        assertEquals("middle", features.get("position"));
    }

    @Test
    public void testFeatureExtractorState() {
        AddressFeatureExtractor extractor = new AddressFeatureExtractor();

        Map<String, String> features = extractor.extractFeatures("FL", 4, 6, "Miami", "33101");

        assertEquals("true", features.get("state"));
    }

    @Test
    public void testParsedAddressComponents() {
        List<AddressToken> tokens = Arrays.asList(new AddressToken("123", "AddressNumber", 1.0),
                new AddressToken("Main", "StreetName", 1.0),
                new AddressToken("St", "StreetNamePostType", 1.0),
                new AddressToken("Miami", "PlaceName", 1.0),
                new AddressToken("FL", "StateName", 1.0),
                new AddressToken("33101", "ZipCode", 1.0));

        ParsedAddress parsed = new ParsedAddress(tokens);
        Map<String, String> components = parsed.getComponents();

        assertEquals("123", components.get("AddressNumber"));
        assertEquals("Main", components.get("StreetName"));
        assertEquals("St", components.get("StreetNamePostType"));
        assertEquals("Miami", components.get("PlaceName"));
        assertEquals("FL", components.get("StateName"));
        assertEquals("33101", components.get("ZipCode"));
    }

    @Test
    public void testTrainingExampleCreation() {
        List<AddressParserTrainer.TrainingExample> examples = SampleTrainingData.generateSamples();

        assertFalse(examples.isEmpty());
        assertTrue(examples.size() >= 10);

        // Check first example structure
        AddressParserTrainer.TrainingExample first = examples.get(0);
        assertNotNull(first.getAddress());
        assertNotNull(first.getLabels());
        assertFalse(first.getLabels().isEmpty());
    }
}
