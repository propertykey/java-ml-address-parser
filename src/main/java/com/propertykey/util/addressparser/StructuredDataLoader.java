package com.propertykey.util.addressparser;

import java.sql.*;
import java.util.*;

/**
 * Example showing how to generate training data from your structured database
 * 
 * Assumes you have a PostgreSQL database with a table like: CREATE TABLE addresses ( id SERIAL
 * PRIMARY KEY, address_number VARCHAR(10), predirectional VARCHAR(10), street_name VARCHAR(100),
 * street_suffix VARCHAR(20), city VARCHAR(100), state VARCHAR(2), zip VARCHAR(10) );
 */
public class StructuredDataLoader {

    /**
     * Generate training examples from structured database Produces multiple format variations for
     * better model robustness
     */
    public static List<AddressParserTrainer.TrainingExample> loadFromDatabase(Connection conn,
            int limit) throws SQLException {

        List<AddressParserTrainer.TrainingExample> examples = new ArrayList<>();

        String query = "SELECT address_number, predirectional, street_name, "
                + "street_suffix, city, state, zip FROM addresses " + "ORDER BY RANDOM() LIMIT ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String number = rs.getString("address_number");
                    String predir = rs.getString("predirectional");
                    String street = rs.getString("street_name");
                    String suffix = rs.getString("street_suffix");
                    String city = rs.getString("city");
                    String state = rs.getString("state");
                    String zip = rs.getString("zip");

                    // Generate multiple format variations
                    examples.addAll(
                            generateVariations(number, predir, street, suffix, city, state, zip));
                }
            }
        }

        return examples;
    }

    /**
     * Generate different address format variations from components
     */
    private static List<AddressParserTrainer.TrainingExample> generateVariations(String number,
            String predir, String street, String suffix, String city, String state, String zip) {

        List<AddressParserTrainer.TrainingExample> variations = new ArrayList<>();

        // Handle nulls/empties
        predir = predir == null ? "" : predir.trim();

        // Variation 1: Standard format without commas
        if (predir.isEmpty()) {
            String addr1 =
                    String.format("%s %s %s %s %s %s", number, street, suffix, city, state, zip);
            variations.add(new AddressParserTrainer.TrainingExample(addr1,
                    Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                            "StateName", "ZipCode")));
        } else {
            String addr1 = String.format("%s %s %s %s %s %s %s", number, predir, street, suffix,
                    city, state, zip);
            variations.add(new AddressParserTrainer.TrainingExample(addr1,
                    Arrays.asList("AddressNumber", "StreetNamePreDirectional", "StreetName",
                            "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));
        }

        // Variation 2: With commas
        if (predir.isEmpty()) {
            String addr2 =
                    String.format("%s %s %s, %s, %s %s", number, street, suffix, city, state, zip);
            variations.add(new AddressParserTrainer.TrainingExample(addr2,
                    Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType,",
                            "PlaceName,", "StateName", "ZipCode")));
        } else {
            String addr2 = String.format("%s %s %s %s, %s, %s %s", number, predir, street, suffix,
                    city, state, zip);
            variations.add(new AddressParserTrainer.TrainingExample(addr2,
                    Arrays.asList("AddressNumber", "StreetNamePreDirectional", "StreetName",
                            "StreetNamePostType,", "PlaceName,", "StateName", "ZipCode")));
        }

        // Variation 3: Abbreviated suffix
        String abbrevSuffix = abbreviateSuffix(suffix);
        if (!abbrevSuffix.equals(suffix) && predir.isEmpty()) {
            String addr3 = String.format("%s %s %s %s %s %s", number, street, abbrevSuffix, city,
                    state, zip);
            variations.add(new AddressParserTrainer.TrainingExample(addr3,
                    Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                            "StateName", "ZipCode")));
        }

        return variations;
    }

    /**
     * Abbreviate common street suffixes
     */
    private static String abbreviateSuffix(String suffix) {
        if (suffix == null)
            return "";

        Map<String, String> abbreviations = new HashMap<>();
        abbreviations.put("STREET", "ST");
        abbreviations.put("AVENUE", "AVE");
        abbreviations.put("BOULEVARD", "BLVD");
        abbreviations.put("DRIVE", "DR");
        abbreviations.put("LANE", "LN");
        abbreviations.put("ROAD", "RD");
        abbreviations.put("COURT", "CT");
        abbreviations.put("PLACE", "PL");
        abbreviations.put("PARKWAY", "PKWY");
        abbreviations.put("TERRACE", "TER");
        abbreviations.put("CIRCLE", "CIR");

        return abbreviations.getOrDefault(suffix.toUpperCase(), suffix);
    }

    /**
     * Example usage
     */
    public static void main(String[] args) {
        try {
            // Connect to your PostgreSQL database
            String url = "jdbc:postgresql://localhost:5432/your_database";
            String user = "your_username";
            String password = "your_password";

            Connection conn = DriverManager.getConnection(url, user, password);

            // Load 50,000 training examples (produces ~100-150k with variations)
            System.out.println("Loading training data from database...");
            List<AddressParserTrainer.TrainingExample> trainingData = loadFromDatabase(conn, 50000);

            System.out.printf("Generated %d training examples%n", trainingData.size());

            // Train the model
            System.out.println("Training CRF model...");
            AddressParserTrainer trainer = new AddressParserTrainer();
            cc.mallet.fst.CRF model = trainer.train(trainingData);

            // Save the model
            String modelPath = "florida-addresses-model.ser";
            System.out.println("Saving model to " + modelPath);
            trainer.saveModel(model, modelPath);

            // Test the trained model
            System.out.println("\nTesting trained model:");
            AddressParser parser = new AddressParser(model, model.getInputPipe());

            String[] testAddresses =
                    {"123 N Main Street Miami FL 33101", "456 Ocean Drive Miami Beach FL 33139",
                            "789 University Boulevard Tampa FL 33620"};

            for (String address : testAddresses) {
                System.out.println("\nAddress: " + address);
                Map<String, String> components = parser.tag(address);
                System.out.println("Parsed: " + components);
            }

            conn.close();
            System.out.println("\nTraining complete!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
