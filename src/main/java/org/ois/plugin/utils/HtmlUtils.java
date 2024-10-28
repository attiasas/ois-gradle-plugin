package org.ois.plugin.utils;

import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle HTML-specific functionalities for the OIS simulation.
 * This includes generating file paths, reading and updating content for configuration files.
 */
public class HtmlUtils {

    /**
     * Gets the path to the reflections items file inside the OIS simulation directory.
     * @param project The current Gradle project.
     * @return The path to the reflection.ois file.
     */
    public static Path getReflectionsItemsFilePath(Project project) {
        return SimulationUtils.getSimulationRunnersResourcesDirectory(project).resolve("reflection.ois");
    }

    /**
     * Generates the content of the reflections file. If the file does not exist, returns an empty string.
     * @param project The current Gradle project.
     * @return The content of the reflections file as a string, or an empty string if the file does not exist.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public static String generateReflectionFileContent(Project project) throws IOException {
        // check if exists
        Path reflectionItemsFilePath = getReflectionsItemsFilePath(project);
        if (reflectionItemsFilePath.toFile().exists()) {
            return Files.readString(reflectionItemsFilePath);
        }
        return "";
    }

    /**
     * Retrieves the content of the SimulationConfig.java file for the HTML runner.
     * @param htmlRunnerDirectory The directory containing the HTML runner.
     * @return The content of SimulationConfig.java as a string.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public static String getSimulationConfigContent(Path htmlRunnerDirectory) throws IOException {
        return Files.readString(getSimulationConfigPath(htmlRunnerDirectory));
    }

    /**
     * Gets the path to the SimulationConfig.java file for the HTML runner.
     * @param htmlRunnerDirectory The directory containing the HTML runner.
     * @return The path to SimulationConfig.java.
     */
    public static Path getSimulationConfigPath(Path htmlRunnerDirectory) {
        return htmlRunnerDirectory.resolve("src").resolve("main").resolve("java").resolve("org").resolve("ois").resolve("html").resolve("SimulationConfig.java");
    }

    /**
     * Updates the content of SimulationConfig.java by replacing specific attributes with new values.
     * @param fileContent The original content of the SimulationConfig.java file.
     * @param attributeMap A map where keys are attribute names and values are the new values.
     * @return The updated file content as a string.
     * @throws IllegalArgumentException if any attribute in the map was not found in the file.
     */
    public static String getUpdateConfigFileContent(String fileContent, Map<String, Object> attributeMap) {
        // Process each line to update existing attributes
        StringBuilder updatedContent = processLines(fileContent, attributeMap);
        // Check if any attributes in attributeMap were not found and replaced
        checkNotReplacedAttributes(attributeMap);
        return updatedContent.toString();
    }

    /**
     * Processes each line of the file content, updating attributes that match those in attributeMap.
     * @param fileContent The original content of the SimulationConfig.java file.
     * @param attributeMap Map of attribute names and their new values.
     * @return A StringBuilder containing the updated file content.
     */
    private static StringBuilder processLines(String fileContent, Map<String, Object> attributeMap) {
        StringBuilder updatedContent = new StringBuilder();
        String[] lines = fileContent.split("\n");

        // Track which attributes were modified
        Map<String, Boolean> modifiedAttributes = new java.util.HashMap<>();
        for (String key : attributeMap.keySet()) {
            modifiedAttributes.put(key, false);
        }

        for (String line : lines) {
            boolean lineModified = false;

            // Attempt to update each attribute in the map
            for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
                String attributeName = entry.getKey();
                Object newValue = entry.getValue();

                String modifiedLine = replaceAttribute(line, attributeName, newValue);
                if (modifiedLine != null) {
                    updatedContent.append(modifiedLine).append("\n");
                    modifiedAttributes.put(attributeName, true);
                    lineModified = true;
                    break;
                }
            }

            // If no modification was made, keep the line as is
            if (!lineModified) {
                updatedContent.append(line).append("\n");
            }
        }

        // Update attributeMap to reflect which attributes were modified
        attributeMap.putAll(modifiedAttributes);
        return updatedContent;
    }

    /**
     * Replaces the attribute's value in a given line if it matches.
     * @param line The line of code being examined.
     * @param attributeName The name of the attribute to update.
     * @param newValue The new value for the attribute.
     * @return The modified line with the new attribute value, or null if no match.
     */
    private static String replaceAttribute(String line, String attributeName, Object newValue) {
        String replacementValue = formatReplacementValue(newValue);
        String regex = String.format("public\\s+static\\s+final\\s+\\w+(\\[\\])?\\s+%s\\s*=\\s*[^;]+;", attributeName);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line.trim());

        if (matcher.find()) {
            return line.replaceFirst("=\\s*[^;]+;", "= " + replacementValue + ";");
        }
        return null;
    }

    /**
     * Formats the replacement value based on its type.
     * @param value The value to format.
     * @return The formatted string representing the value.
     */
    private static String formatReplacementValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof String[]) {
            String[] array = (String[]) value;
            StringBuilder formattedArray = new StringBuilder("new String[] {");
            for (int i = 0; i < array.length; i++) {
                formattedArray.append("\"").append(array[i]).append("\"");
                if (i < array.length - 1) {
                    formattedArray.append(", ");
                }
            }
            formattedArray.append("}");
            return formattedArray.toString();
        } else {
            return value.toString(); // For int, float, boolean, etc.
        }
    }

    /**
     * Checks for any attributes in attributeMap that were not found in the fileContent.
     * Throws an IllegalArgumentException if any attributes were not replaced.
     * @param attributeMap Map of attribute names and their values, with a Boolean flag for whether they were modified.
     * @throws IllegalArgumentException if an attribute in attributeMap was not found and replaced.
     */
    private static void checkNotReplacedAttributes(Map<String, Object> attributeMap) {
        StringBuilder missingAttributes = new StringBuilder();
        for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                if (!missingAttributes.isEmpty()) {
                    missingAttributes.append(", ");
                }
                missingAttributes.append(entry.getKey());
            }
        }

        // If there are any missing attributes, throw an exception
        if (!missingAttributes.isEmpty()) {
            throw new IllegalArgumentException("The following attributes were not found in the file content: " + missingAttributes);
        }
    }

//    public static void main(String[] args) {
//        String fileContent = """
//                package org.ois.html;
//
//                import org.ois.core.runner.RunnerConfiguration;
//
//                public class SimulationConfig extends RunnerConfiguration {
//                    public static final String TITLE = "OIS";
//                    public static final int SCREEN_WIDTH = 0;
//                    public static final String[] SUPPORTED_FORMATS = new String[] {"html", "json"};
//
//                    public SimulationConfig() {
//                        super();
//                        setType(RunnerType.Html);
//                    }
//                }
//                """;
//
//        Map<String, Object> attributeMap = new HashMap<>();
//        attributeMap.put("TITLE", "New Simulation Title");
//        attributeMap.put("SCREEN_WIDTH", 1920);
//        attributeMap.put("SUPPORTED_FORMATS", new String[] {"html", "xml", "txt"});
//
//        String updatedContent = HtmlUtils.updateConfigContent(fileContent, attributeMap);
//        System.out.println(updatedContent);
//    }
}
