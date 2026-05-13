package com.propertykey.util.addressparser;

import java.util.*;

/**
 * Generates sample training data for demonstration In production, this would load from usaddress's
 * labeled.xml or your structured database
 */
public class SampleTrainingData {

    /**
     * Generate sample labeled training examples Labels follow usaddress schema: - AddressNumber:
     * Street number - StreetNamePreDirectional: N, S, E, W before street name - StreetName: Name of
     * the street - StreetNamePostType: St, Ave, Blvd, etc. - PlaceName: City - StateName: State -
     * ZipCode: Zip code - OccupancyType: Apt, Suite, Unit, etc. - OccupancyIdentifier:
     * Apartment/suite number
     */
    public static List<AddressParserTrainer.TrainingExample> generateSamples() {
        List<AddressParserTrainer.TrainingExample> examples = new ArrayList<>();

        // Simple addresses
        examples.add(new AddressParserTrainer.TrainingExample("123 Main St Miami FL 33101",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                        "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("456 Oak Avenue Tampa FL 33602",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                        "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("789 Pine Road Jacksonville FL 32099",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                        "StateName", "ZipCode")));

        // With directionals
        examples.add(new AddressParserTrainer.TrainingExample("100 N Elm Street Orlando FL 32801",
                Arrays.asList("AddressNumber", "StreetNamePreDirectional", "StreetName",
                        "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("200 S Beach Blvd Miami FL 33139",
                Arrays.asList("AddressNumber", "StreetNamePreDirectional", "StreetName",
                        "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("300 E Bay St Jacksonville FL 32202",
                Arrays.asList("AddressNumber", "StreetNamePreDirectional", "StreetName",
                        "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        // With apartment/unit numbers
        examples.add(
                new AddressParserTrainer.TrainingExample("500 Park Avenue Apt 5 Tampa FL 33602",
                        Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType",
                                "OccupancyType", "OccupancyIdentifier", "PlaceName", "StateName",
                                "ZipCode")));

        examples.add(
                new AddressParserTrainer.TrainingExample("600 Lake Dr Suite 200 Orlando FL 32801",
                        Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType",
                                "OccupancyType", "OccupancyIdentifier", "PlaceName", "StateName",
                                "ZipCode")));

        examples.add(
                new AddressParserTrainer.TrainingExample("700 River Road Unit 10 Miami FL 33130",
                        Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType",
                                "OccupancyType", "OccupancyIdentifier", "PlaceName", "StateName",
                                "ZipCode")));

        // Different formats with commas
        examples.add(new AddressParserTrainer.TrainingExample("800 Market Street, Tampa, FL 33602",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType,", "PlaceName,",
                        "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("900 Church Ave, Miami, FL 33101",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType,", "PlaceName,",
                        "StateName", "ZipCode")));

        // More variations
        examples.add(new AddressParserTrainer.TrainingExample("1000 Broadway Jacksonville FL 32204",
                Arrays.asList("AddressNumber", "StreetName", "PlaceName", "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("1100 Central Pkwy Orlando FL 32801",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                        "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("1200 University Blvd Tampa FL 33620",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                        "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample(
                "1300 College Dr Tallahassee FL 32301", Arrays.asList("AddressNumber", "StreetName",
                        "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample("1400 Airport Rd Fort Myers FL 33901",
                Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType", "PlaceName",
                        "StateName", "ZipCode")));

        // Extended zip codes
        examples.add(new AddressParserTrainer.TrainingExample(
                "1500 Harbor Blvd Miami FL 33101-5432", Arrays.asList("AddressNumber", "StreetName",
                        "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        examples.add(new AddressParserTrainer.TrainingExample(
                "1600 Sunset Dr Key West FL 33040-1234", Arrays.asList("AddressNumber",
                        "StreetName", "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        // Complex multi-word street names
        examples.add(new AddressParserTrainer.TrainingExample(
                "1700 Martin Luther King Blvd Tampa FL 33602",
                Arrays.asList("AddressNumber", "StreetName", "StreetName", "StreetName",
                        "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        examples.add(
                new AddressParserTrainer.TrainingExample("1800 John F Kennedy Dr Miami FL 33125",
                        Arrays.asList("AddressNumber", "StreetName", "StreetName", "StreetName",
                                "StreetNamePostType", "PlaceName", "StateName", "ZipCode")));

        examples.add(
                new AddressParserTrainer.TrainingExample("1900 Ocean Drive Miami Beach FL 33139",
                        Arrays.asList("AddressNumber", "StreetName", "StreetNamePostType",
                                "PlaceName", "PlaceName", "StateName", "ZipCode")));

        return examples;
    }
}
