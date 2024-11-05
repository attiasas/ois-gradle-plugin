package org.ois.plugin.utils;

import org.gradle.api.GradleException;
import org.gradle.api.invocation.Gradle;
import org.gradle.tooling.*;
import org.ois.plugin.Const;
import org.ois.core.utils.Version;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;

/**
 * Gradle utilities
 */
public class GradleUtils {

    /**
     * Check if a given gradle version is supported
     * @param gradle - project gradle information
     * @throws GradleException - if version is not supported
     */
    public static void checkGradleVersionSupported(Gradle gradle) throws GradleException {
        String gradleVersion = gradle.getGradleVersion();
        if (!new Version(gradleVersion).isAtLeast(Const.Versions.MIN_GRADLE_VERSION)) {
            throw new GradleException("Can't apply OIS deployer plugin on Gradle version " + gradleVersion + ". Minimum supported Gradle is " + Const.Versions.MIN_GRADLE_VERSION);
        }
    }

    /**
     * Run a given Gradle tasks
     * @param workingDir - the project directory to run the gradle tasks on
     * @param environmentVariables - the environment variables that will be injected to the gradle tasks process
     * @param log - the gradle tasks will output their logs to it.
     * @param gradleTasks - the tasks to run
     */
    public static void runTasks(Path workingDir, Map<String, String> environmentVariables, Logger log, String... gradleTasks) {
        try (ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(workingDir.toFile()).connect()){
            BuildLauncher launcher = connection.newBuild().forTasks(gradleTasks);
            // Set environment variables for the task execution
            launcher.setEnvironmentVariables(environmentVariables);
            // Redirect Gradle output to SLF4J logger
            launcher.setStandardOutput(LogUtils.getRedirectOutToLogInfo(log));
            launcher.setStandardError(LogUtils.getRedirectOutToLogErr(log));
            // Run
            launcher.setStandardInput(System.in);
            launcher.run();
        }
    }
}
