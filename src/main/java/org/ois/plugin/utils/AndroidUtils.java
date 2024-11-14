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

public class AndroidUtils {
    private static final Logger log = LoggerFactory.getLogger(AndroidUtils.class);

    public static final List<String> icons = List.of("icon48.png", "icon72.png", "icon96.png", "icon144.png", "icon192.png");

    /**
     * Retrieves the content of the AndroidSimulationConfig.java file for the Android runner.
     * @param androidRunnerDirectory The directory containing the Android runner.
     * @return The content of AndroidSimulationConfig.java as a string.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    public static String getSimulationConfigContent(Path androidRunnerDirectory) throws IOException {
        return Files.readString(getSimulationConfigPath(androidRunnerDirectory));
    }

    /**
     * Gets the path to the AndroidSimulationConfig.java file for the Android runner.
     * @param androidRunnerDirectory The directory containing the Android runner.
     * @return The path to AndroidSimulationConfig.java.
     */
    public static Path getSimulationConfigPath(Path androidRunnerDirectory) {
        return androidRunnerDirectory.resolve("src").resolve("main").resolve("java").resolve("org").resolve("ois").resolve("android").resolve("AndroidSimulationConfig.java");
    }

    public static Path getAndroidRunnerResourceInnerDir(Path androidRunnerDirectory) {
        return androidRunnerDirectory.resolve("res");
    }

    public static Path getAndroidRunnerResourceStringValuesPath(Path androidRunnerDirectory) {
        return getAndroidRunnerResourceInnerDir(androidRunnerDirectory).resolve("values").resolve("strings.xml");
    }

    public static String getAndroidRunnerResourceStringValuesContent(Path androidRunnerDirectory) throws IOException {
        return Files.readString(getAndroidRunnerResourceStringValuesPath(androidRunnerDirectory));
    }

    /**
     * Get the list of artifacts to zip for Android distribution
     * @return - list of files and directories to zip
     */
    public static Path[] getAndroidFilesToZip(Project project) {
        Path releaseDir = SimulationUtils.getRunner(project).getAndroidRunnerDirectory().resolve("build").resolve("outputs").resolve("apk").resolve("release");
        File[] files = releaseDir.toFile().listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("[Android] Can't find any artifacts to zip");
        }
        Path[] filePaths = Arrays.stream(files).map(File::toPath).toArray(Path[]::new);
        log.debug("files to zip: {}", Arrays.toString(filePaths));
        return filePaths;
    }
}
