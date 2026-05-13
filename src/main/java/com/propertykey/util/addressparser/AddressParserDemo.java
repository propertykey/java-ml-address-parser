package com.propertykey.util.addressparser;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Demonstration of training and using the address parser
 */
public class AddressParserDemo {

    public static void main(String[] args) {
        try {
            System.out.println("=== Address Parser Demo ===\n");

            // Step 1: Train a model (or load existing)
            String modelPath = "address-parser-model.ser";
            AddressParser parser;
            // for WAR: new File(getClass().getClassLoader().getResource("model.bin").getFile())
            if (new File(modelPath).exists()) {
                System.out.println("Loading existing model...");
                parser = AddressParser.loadModel(modelPath);
            } else {
                System.out.println("Training new model...");
                parser = trainNewModel(modelPath);
            }

            // Step 2: Parse some test addresses
            System.out.println("\n=== Parsing Test Addresses ===\n");

            String[] testAddresses = {"123 Main St Miami FL 33101",
                    "456 N Oak Avenue Apt 5 Tampa FL 33602", "789 Park Blvd, Orlando, FL 32801",
                    "1000 E Bay Street Suite 200 Jacksonville FL 32202",
                    "1500 College Dr Tallahassee FL 32301-1234"};

            for (String address : testAddresses) {
                System.out.println("Address: " + address);

                // Parse and get full result
                ParsedAddress parsed = parser.parse(address);
                System.out.println(parsed.toPrettyString());

                // Or get just components as Map
                Map<String, String> components = parser.tag(address);
                System.out.println("Components Map: " + components);
                System.out.println();
            }

            System.out.println("Demo complete!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Train a new model from sample data or usaddress XML
     */
    private static AddressParser trainNewModel(String modelPath) throws Exception {
        List<AddressParserTrainer.TrainingExample> trainingData;

        // Try loading usaddress XML training data first
        String xmlPath = "training/labeled.xml";
        if (new File(xmlPath).exists()) {
            System.out.println("Loading training data from " + xmlPath + "...");
            trainingData = XmlTrainingDataLoader.loadFromFile(xmlPath);
        } else {
            System.out.println("XML training data not found, using sample data...");
            trainingData = SampleTrainingData.generateSamples();
        }

        System.out.printf("Training on %d examples...%n", trainingData.size());
        AddressParserTrainer trainer = new AddressParserTrainer();

        cc.mallet.fst.CRF model = trainer.train(trainingData);

        System.out.println("Saving model to " + modelPath + "...");
        trainer.saveModel(model, modelPath);

        System.out.println("Training complete!");

        // Create parser with trained model
        return new AddressParser(model, model.getInputPipe());
    }
}
