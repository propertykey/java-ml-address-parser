package com.propertykey.util.addressparser;

import cc.mallet.fst.*;
import cc.mallet.pipe.*;
import cc.mallet.types.*;
import java.io.*;
import java.util.*;

/**
 * Main address parser using CRF model Provides simple API similar to usaddress Python library
 */
public class AddressParser {

    private CRF crfModel;
    private Pipe pipe;
    private AddressTokenizer tokenizer;
    private AddressFeatureExtractor featureExtractor;

    /**
     * Create a new parser with a trained model
     */
    public AddressParser(CRF model, Pipe pipe) {
        this.crfModel = model;
        this.pipe = pipe;
        this.tokenizer = new AddressTokenizer();
        this.featureExtractor = new AddressFeatureExtractor();
    }

    /**
     * Load a trained model from file
     */
    public static AddressParser loadModel(String modelPath)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois =
                new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelPath)))) {
            CRF model = (CRF) ois.readObject();
            Pipe pipe = model.getInputPipe();
            return new AddressParser(model, pipe);
        }
    }

    /**
     * Parse an address string and return labeled components
     */
    public ParsedAddress parse(String address) {
        // Normalize and tokenize
        String normalized = tokenizer.normalize(address);
        List<String> tokens = tokenizer.tokenize(normalized);

        if (tokens.isEmpty()) {
            return new ParsedAddress(Collections.emptyList());
        }

        // Extract features
        List<Map<String, String>> features = featureExtractor.extractAllFeatures(tokens);

        // Create instance for CRF
        Instance instance = createInstance(tokens, features);

        // Run through pipe to convert TokenSequence to FeatureVectorSequence
        Instance piped = pipe.instanceFrom(instance);

        // Run CRF inference
        Sequence<?> input = (Sequence<?>) piped.getData();
        Sequence<?> predictedSequence = crfModel.transduce(input);

        // Convert to AddressToken list
        List<AddressToken> addressTokens = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String label = predictedSequence.get(i).toString();
            addressTokens.add(new AddressToken(tokens.get(i), label, 1.0));
        }

        return new ParsedAddress(addressTokens);
    }

    /**
     * Create a Mallet Instance from tokens and features. Returns a TokenSequence so the pipe can
     * convert it to FeatureVectorSequence.
     */
    private Instance createInstance(List<String> tokens, List<Map<String, String>> features) {
        TokenSequence tokenSeq = new TokenSequence();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = new Token(tokens.get(i));
            Map<String, String> tokenFeatures = features.get(i);
            for (Map.Entry<String, String> entry : tokenFeatures.entrySet()) {
                token.setFeatureValue(entry.getKey() + "=" + entry.getValue(), 1.0);
            }
            tokenSeq.add(token);
        }

        return new Instance(tokenSeq, null, null, null);
    }

    /**
     * Parse address and return as Map of components Convenience method matching usaddress.tag()
     * behavior
     */
    public Map<String, String> tag(String address) {
        ParsedAddress parsed = parse(address);
        return parsed.getComponents();
    }
}
