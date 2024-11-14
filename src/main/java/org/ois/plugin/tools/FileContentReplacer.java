package org.ois.plugin.tools;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileContentReplacer {

    public interface ReplaceStrategy {
        /**
         * Given a line of content and attribute name and value,
         * Replace the content if the attribute exists or return null
         * @param line the content to change
         * @param attributeName the attribute to search in the line
         * @param value the new value of the attribute
         * @return the replaced content or null if attribute not exists at line
         */
        String replaceAttribute(String line, String attributeName, Object value);
    }

    /**
     * For a given content, an attribute map and replace strategy.
     * It will replace the variables (attribute map keys) values (attribute map values) and return the updated content of the file after the changes.
     *
     * @param source the content to replace
     * @param attributeMap the variable names as written at the content mapped to their new values
     * @param replaceStrategy for each line to process the strategy will be called to generate the content
     * @return the updated content after replacing the values. will throw exception on attribute key that not exists in content
     */
    public static String replaceContent(String source, Map<String, Object> attributeMap, ReplaceStrategy replaceStrategy) {
        // Process each line to update existing attributes
        StringBuilder updatedContent = processLines(source, attributeMap, replaceStrategy);
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
    private static StringBuilder processLines(String fileContent, Map<String, Object> attributeMap, ReplaceStrategy replaceStrategy) {
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

                String modifiedLine = replaceStrategy.replaceAttribute(line, attributeName, newValue);
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

    public static class Java {

        /**
         * For a given Java file content and an attribute map of static final variables (names) and their values,
         * It will replace the variables values and return the updated content of the file after replacing.
         * @param source - the Java file content to replace
         * @param attributeMap - the variable names as written at the content mapped to their new values
         * @return - the updated content after replacing the values. will throw exception on attribute key that not exists in content
         */
        public static String replaceJavaStaticFinalVals(String source, Map<String, Object> attributeMap) {
            return replaceContent(source, attributeMap, Java::replaceStaticFinalAttribute);
        }

        /**
         * Replaces the attribute's value in a given line if it matches.
         * @param line The line of code being examined.
         * @param attributeName The name of the attribute to update.
         * @param newValue The new value for the attribute.
         * @return The modified line with the new attribute value, or null if no match.
         */
        private static String replaceStaticFinalAttribute(String line, String attributeName, Object newValue) {
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
    }

    public static class Xml {
        /**
         * For a given XML content and an attribute map of attribute names and their new values,
         * It will replace the attribute values and return the updated content of the file after replacing.
         * @param source The XML file content to replace
         * @param attributeMap The attribute names mapped to their new values
         * @return The updated content after replacing the values
         */
        public static String replaceXmlAttributes(String source, Map<String, Object> attributeMap) {
            return replaceContent(source, attributeMap, Xml::replaceXmlAttributeOrElementContent);
        }

        /**
         * Replaces the attribute's value or inner content in a given line if it matches.
         * @param line The line of XML being examined.
         * @param attributeName The name of the attribute to update (e.g., "name", "android:icon").
         * @param newValue The new value for the attribute or element content.
         * @return The modified line with the new attribute value or element content, or null if no match.
         */
        private static String replaceXmlAttributeOrElementContent(String line, String attributeName, Object newValue) {
            String replacementValue = newValue.toString();

            // 1. Try to replace attribute value (e.g., android:icon="...")
            String attributeRegex = "(\\s" + Pattern.quote(attributeName) + "=\")([^\"]*)(\")";
            Pattern attributePattern = Pattern.compile(attributeRegex);
            Matcher attributeMatcher = attributePattern.matcher(line);
            if (attributeMatcher.find()) {
                return attributeMatcher.replaceFirst("$1" + replacementValue + "$3");
            }

            // 2. Try to replace element content based on 'name' attribute (e.g., <string name="app_name">...</string>)
            String elementContentRegex = "<(\\w+)\\s+name=\"" + Pattern.quote(attributeName) + "\"\\s*>[^<]*</\\1>";
            Pattern elementContentPattern = Pattern.compile(elementContentRegex);
            Matcher elementContentMatcher = elementContentPattern.matcher(line);
            if (elementContentMatcher.find()) {
                return elementContentMatcher.replaceFirst("<$1 name=\"" + attributeName + "\">" + replacementValue + "</$1>");
            }

            return null;  // No match found in this line
        }
    }
}
