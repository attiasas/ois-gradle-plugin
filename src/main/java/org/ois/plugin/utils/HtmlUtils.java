package org.ois.plugin.utils;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.ois.core.utils.io.data.DataNode;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.plugin.tools.ClassImplementationFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle HTML-specific functionalities for the OIS simulation.
 * This includes generating file paths, reading and updating content for configuration files.
 */
public class HtmlUtils {
    private static final Logger log = LoggerFactory.getLogger(HtmlUtils.class);

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
        Set<String> reflectionItems = new HashSet<>();
        // check if exists
        Path reflectionItemsFilePath = getReflectionsItemsFilePath(project);
        if (reflectionItemsFilePath.toFile().exists()) {
            for (DataNode reflectionItem : JsonFormat.compact().deserialize(Files.readString(reflectionItemsFilePath))) {
                reflectionItems.add(reflectionItem.getString());
            }
        }
        Path projectBuildDir = SimulationUtils.getProjectBuildDirectory(project);
        FileCollection classpath = project.getConfigurations().getByName("runtimeClasspath");
        // Find State related implementation
//        reflectionItems.addAll(ClassImplementationFinder.find(projectBuildDir, classpath, "org.ois.core.state.IState"));
//        reflectionItems.addAll(ClassImplementationFinder.find(projectBuildDir, classpath, "org.ois.core.state.managed.StateBlueprint"));
        // Find Entity related implementation
        reflectionItems.addAll(ClassImplementationFinder.find(projectBuildDir, classpath,"org.ois.core.entities.Entity"));
        reflectionItems.addAll(ClassImplementationFinder.find(projectBuildDir, classpath,"org.ois.core.entities.EntityBlueprint"));
        if (reflectionItems.isEmpty()) {
            return "";
        }
        return JsonFormat.humanReadable().serialize(DataNode.Collection(reflectionItems));
    }

    /**
     * Retrieves the content of the HtmlSimulationConfig.java file for the HTML runner.
     * @param htmlRunnerDirectory The directory containing the HTML runner.
     * @return The content of HtmlSimulationConfig.java as a string.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public static String getSimulationConfigContent(Path htmlRunnerDirectory) throws IOException {
        return Files.readString(getSimulationConfigPath(htmlRunnerDirectory));
    }

    /**
     * Gets the path to the HtmlSimulationConfig.java file for the HTML runner.
     * @param htmlRunnerDirectory The directory containing the HTML runner.
     * @return The path to HtmlSimulationConfig.java.
     */
    public static Path getSimulationConfigPath(Path htmlRunnerDirectory) {
        return htmlRunnerDirectory.resolve("src").resolve("main").resolve("java").resolve("org").resolve("ois").resolve("html").resolve("HtmlSimulationConfig.java");
    }

    /**
     * Get the list of artifacts to zip for HTML distribution
     * @return - list of files and directories to zip
     */
    public static Path[] getHtmlFilesToZip(Project project) {
        Path webappDir = SimulationUtils.getRunner(project).getHtmlRunnerDirectory().resolve("build").resolve("dist").resolve("webapp");
        File[] files = webappDir.toFile().listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("[HTML] Can't find any artifacts to zip");
        }
        Path[] filePaths = Arrays.stream(files).map(File::toPath).toArray(Path[]::new);
        log.debug("files to zip: {}", Arrays.toString(filePaths));
        return filePaths;
    }
}
