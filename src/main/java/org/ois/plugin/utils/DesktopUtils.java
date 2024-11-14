package org.ois.plugin.utils;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class DesktopUtils {
    private static final Logger log = LoggerFactory.getLogger(DesktopUtils.class);

    public static final List<String> icons = List.of(
            "icon32.png", "icon32.ico", "icon32.icns",
            "icon128.png", "icon128.ico", "icon128.icns",
            "logo.png"
    );

    /**
     * Retrieves the content of the DesktopSimulationConfig.java file for the Desktop runner.
     * @param htmlRunnerDirectory The directory containing the Desktop runner.
     * @return The content of DesktopSimulationConfig.java as a string.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public static String getSimulationConfigContent(Path htmlRunnerDirectory) throws IOException {
        return Files.readString(getSimulationConfigPath(htmlRunnerDirectory));
    }

    /**
     * Gets the path to the DesktopSimulationConfig.java file for the Desktop runner.
     * @param desktopRunnerDirectory The directory containing the Desktop runner.
     * @return The path to DesktopSimulationConfig.java.
     */
    public static Path getSimulationConfigPath(Path desktopRunnerDirectory) {
        return desktopRunnerDirectory.resolve("src").resolve("main").resolve("java").resolve("org").resolve("ois").resolve("desktop").resolve("DesktopSimulationConfig.java");
    }

    /**
     * Get the list of artifacts to zip for Desktop distribution
     * @return - list of files and directories to zip
     */
    public static Path[] getDesktopFilesToZip(Project project) {
        Path jpackageDir = SimulationUtils.getRunner(project).getDesktopRunnerDirectory().resolve("build").resolve("jpackage");
        File[] files = jpackageDir.toFile().listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("[Desktop] Can't find any artifacts to zip");
        }
        Path[] filePaths = Arrays.stream(files).map(File::toPath).toArray(Path[]::new);
        log.debug("files to zip: {}", Arrays.toString(filePaths));
        return filePaths;
    }
}
