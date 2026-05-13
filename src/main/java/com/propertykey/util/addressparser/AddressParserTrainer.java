package com.propertykey.util.addressparser;

import cc.mallet.fst.*;
import cc.mallet.pipe.*;
import cc.mallet.types.*;
import java.io.*;
import java.util.*;

/**
 * Trains CRF model on labeled address data Supports training data in XML format matching usaddress
 * schema
 */
public class AddressParserTrainer {

    private Pipe pipe;
    private AddressTokenizer tokenizer;
    private AddressFeatureExtractor featureExtractor;

    public AddressParserTrainer() {
        this.tokenizer = new AddressTokenizer();
        this.featureExtractor = new AddressFeatureExtractor();
        this.pipe = buildPipe();
    }

    /**
     * Build the feature extraction pipeline
     */
    private Pipe buildPipe() {
        ArrayList<Pipe> pipes = new ArrayList<>();

        // The pipe expects instances with TokenSequence as data
        // and LabelSequence as target
        pipes.add(new TokenSequence2FeatureVectorSequence(true, true));
        pipes.add(new Target2LabelSequence());

        return new SerialPipes(pipes);
    }

    /**
     * Train a CRF model on labeled data
     * 
     * @param trainingData List of (address, labels) pairs Each label list must match the token
     *        count
     * @return Trained CRF model
     */
    public CRF train(List<TrainingExample> trainingData) {
        // Create instance list
        InstanceList trainingInstances = new InstanceList(pipe);

        for (TrainingExample example : trainingData) {
            Instance instance = createTrainingInstance(example);
            if (instance != null) {
                trainingInstances.addThruPipe(instance);
            }
        }

        // Train CRF
        CRF crf = new CRF(pipe, null);

        // Add states and transitions
        setupCRFStructure(crf, trainingInstances);

        // Use CRF trainer
        CRFTrainerByLabelLikelihood trainer = new CRFTrainerByLabelLikelihood(crf);
        trainer.setGaussianPriorVariance(10.0);

        // Train for specified iterations
        boolean converged = false;
        for (int i = 0; i < 100 && !converged; i++) {
            converged = trainer.train(trainingInstances, 1);
            System.out.printf("Iteration %d: converged=%b%n", i, converged);
        }

        return crf;
    }

    /**
     * Setup CRF states and transitions based on training data
     */
    private void setupCRFStructure(CRF crf, InstanceList data) {
        // Add fully connected states for all labels seen in training data
        crf.addFullyConnectedStatesForLabels();
    }

    /**
     * Create a training instance from an example
     */
    private Instance createTrainingInstance(TrainingExample example) {
        List<String> tokens = tokenizer.tokenize(example.getAddress());
        List<String> labels = example.getLabels();

        if (tokens.size() != labels.size()) {
            System.err.printf("Warning: Token count (%d) != label count (%d) for: %s%n",
                    tokens.size(), labels.size(), example.getAddress());
            return null;
        }

        // Create token sequence with features
        List<Map<String, String>> features = featureExtractor.extractAllFeatures(tokens);

        TokenSequence tokenSeq = new TokenSequence();
        TokenSequence labelSeq = new TokenSequence();

        for (int i = 0; i < tokens.size(); i++) {
            // Create token with features as property list
            Token token = new Token(tokens.get(i));

            // Add features as properties
            Map<String, String> tokenFeatures = features.get(i);
            for (Map.Entry<String, String> entry : tokenFeatures.entrySet()) {
                token.setFeatureValue(entry.getKey() + "=" + entry.getValue(), 1.0);
            }

            tokenSeq.add(token);
            labelSeq.add(new Token(labels.get(i)));
        }

        return new Instance(tokenSeq, labelSeq, example.getAddress(), null);
    }

    /**
     * Save trained model to file
     */
    public void saveModel(CRF model, String filepath) throws IOException {
        try (ObjectOutputStream oos =
                new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filepath)))) {
            oos.writeObject(model);
        }
    }

    /**
     * Represents a single training example
     */
    public static class TrainingExample {
        private final String address;
        private final List<String> labels;

        public TrainingExample(String address, List<String> labels) {
            this.address = address;
            this.labels = labels;
        }

        public String getAddress() {
            return address;
        }

        public List<String> getLabels() {
            return labels;
        }
    }
}
