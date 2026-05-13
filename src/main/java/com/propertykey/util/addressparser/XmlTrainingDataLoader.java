package com.propertykey.util.addressparser;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * Loads training data from usaddress-format XML files at runtime. Reads labeled.xml from the
 * usaddress Python package directly, avoiding the need to generate a large Java source file.
 */
public class XmlTrainingDataLoader {

    /**
     * Load training examples from an XML file path.
     *
     * @param filePath path to a usaddress labeled.xml file
     * @return list of training examples
     */
    public static List<AddressParserTrainer.TrainingExample> loadFromFile(String filePath)
            throws Exception {
        try (InputStream is = new FileInputStream(filePath)) {
            return loadFromStream(is);
        }
    }

    /**
     * Load training examples from a classpath resource. Place the XML file in src/main/resources
     * and reference it e.g. loadFromResource("labeled.xml")
     *
     * @param resourcePath classpath resource path
     * @return list of training examples
     */
    public static List<AddressParserTrainer.TrainingExample> loadFromResource(String resourcePath)
            throws Exception {
        try (InputStream is =
                XmlTrainingDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            return loadFromStream(is);
        }
    }

    /**
     * Load training examples from an InputStream.
     *
     * @param inputStream stream containing usaddress XML data
     * @return list of training examples
     */
    public static List<AddressParserTrainer.TrainingExample> loadFromStream(InputStream inputStream)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        List<AddressParserTrainer.TrainingExample> examples = new ArrayList<>();

        NodeList addressStrings = doc.getElementsByTagName("AddressString");
        for (int i = 0; i < addressStrings.getLength(); i++) {
            Element addressElement = (Element) addressStrings.item(i);

            List<String> tokens = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            NodeList children = addressElement.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String token = child.getTextContent().trim();
                    if (!token.isEmpty()) {
                        tokens.add(token);
                        labels.add(child.getNodeName());
                    }
                }
            }

            if (!tokens.isEmpty()) {
                String address = String.join(" ", tokens);
                examples.add(new AddressParserTrainer.TrainingExample(address, labels));
            }
        }

        return examples;
    }
}
