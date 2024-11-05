package org.ois.plugin;

import org.ois.core.utils.io.FileUtils;
import org.ois.plugin.tools.JavaFileContentReplacer;
import org.ois.plugin.utils.HtmlUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class JavaFileContentReplacerTest {

    @Test
    public void testGetSimulationConfigContent() throws IOException {
        // Arrange
        Path testDir = createTestDirectory();
        Path simulationConfigPath = testDir.resolve("src/main/java/org/ois/html/SimulationConfig.java");
        String expectedContent = """
                package org.ois.html;

                public class SimulationConfig {
                    public static final String TITLE = "OIS";
                    public static final int SCREEN_WIDTH = 800;
                }
                """;

        // Create the SimulationConfig.java file with expected content
        Files.createDirectories(simulationConfigPath.getParent());
        Files.writeString(simulationConfigPath, expectedContent);

        // Act
        String actualContent = HtmlUtils.getSimulationConfigContent(testDir);

        // Assert
        Assert.assertEquals(actualContent.trim(), expectedContent.trim());

        // Cleanup
        deleteTestDirectory(testDir);
    }

    @Test(dataProvider = "updateConfigProvider")
    public void testGetUpdateConfigFileContent(String originalContent, Map<String, Object> attributeMap, String expectedUpdatedContent) {
        // Act
        String actualUpdatedContent = JavaFileContentReplacer.replaceJavaStaticFinalVals(originalContent, attributeMap);

        // Assert
        Assert.assertEquals(actualUpdatedContent.trim(), expectedUpdatedContent.trim());
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "The following attributes were not found in the file content: NEW_ATTRIBUTE")
    public void testGetUpdateConfigFileContentThrowsException() {
        // Arrange
        String originalContent = """
                package org.ois.html;

                public class SimulationConfig {
                    public static final String TITLE = "OIS";
                    public static final int SCREEN_WIDTH = 800;
                }
                """;
        Map<String, Object> attributeMap = new HashMap<>(Map.of(
                "TITLE", "Updated Title",
                "NEW_ATTRIBUTE", "Some Value" // This attribute does not exist
        ));

        // Act
        JavaFileContentReplacer.replaceJavaStaticFinalVals(originalContent, attributeMap); // Should throw exception
    }

    @DataProvider(name = "updateConfigProvider")
    public Object[][] updateConfigProvider() {
        return new Object[][]{
                {
                        """
                        package org.ois.html;

                        public class SimulationConfig {
                            public static final String TITLE = "OIS";
                            public static final int SCREEN_WIDTH = 800;
                        }
                        """,
                        new HashMap<>(Map.of(
                                "TITLE", "New Simulation Title",
                                "SCREEN_WIDTH", 1024
                        )),
                        """
                        package org.ois.html;

                        public class SimulationConfig {
                            public static final String TITLE = "New Simulation Title";
                            public static final int SCREEN_WIDTH = 1024;
                        }
                        """
                },
                {
                        """
                        package org.ois.html;

                        public class SimulationConfig {
                            public static final String TITLE = "OIS";
                           \s
                            public static final String[] LOG_TOPICS = new String[]{};
                        }
                       \s""",
                        new HashMap<>(Map.of("LOG_TOPICS", new String[]{"Html", "Other"})),
                        """
                        package org.ois.html;

                        public class SimulationConfig {
                            public static final String TITLE = "OIS";
                           \s
                            public static final String[] LOG_TOPICS = new String[] {"Html", "Other"};
                        }
                       \s"""
                }
        };
    }

    // Helper method to create a temporary directory for testing
    private Path createTestDirectory() throws IOException {
        return Files.createTempDirectory("testDir");
    }

    // Helper method to delete the test directory
    private void deleteTestDirectory(Path path) {
        assertTrue(FileUtils.deleteDirectoryContent(path));
        assertTrue(path.toFile().delete());
    }

}
