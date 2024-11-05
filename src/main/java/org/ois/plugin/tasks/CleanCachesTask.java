package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import org.ois.core.utils.io.FileUtils;

/**
 * Task to clean up the OIS cache directories in the project's 'build' directory.
 * This is typically useful when developing or resolving issues with the simulation.
 */
public class CleanCachesTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(CleanCachesTask.class);

    /**
     * Default constructor for the clean cache task.
     */
    public CleanCachesTask() {
        super();
    }

    /**
     * Cleans up the OIS cache directories in the project 'build' directory.
     * This includes removing cache files related to OIS runners, assets, and distribution artifacts.
     * If the directories do not exist, appropriate log messages will be displayed.
     */
    @TaskAction
    public void cleanCache() {
        log.info("Clean OIS cache items");
        Project project = getProject();

        // Check if the build directory exists
        if (!SimulationUtils.getProjectBuildDirectory(project).toFile().exists()) {
            log.info("Project 'build' directory not exists, Nothing to do.");
            return;
        }

        // Check if the OIS directory exists
        if (!SimulationUtils.getSimulationDirectory(project).toFile().exists()) {
            log.info("OIS directory not exists in project 'build' directory, Nothing to do.");
            return;
        }

        // Clean runners cache
        Path oisRunnersDirPath = SimulationUtils.getSimulationRunnersDirectory(project);
        if (oisRunnersDirPath.toFile().exists() && FileUtils.deleteDirectoryContent(oisRunnersDirPath) && oisRunnersDirPath.toFile().delete()) {
            log.info("Deleted cached runners directory.");
        }

        // Clean simulation resources
        Path oisAssetsDirPath = SimulationUtils.getSimulationRunnersResourcesDirectory(project);
        if (oisAssetsDirPath.toFile().exists() && FileUtils.deleteDirectoryContent(oisAssetsDirPath) && oisAssetsDirPath.toFile().delete()) {
            log.info("Deleted generated assets directory");
        }

        // Clean distribution artifacts
        Path oisDistributionDirPath = SimulationUtils.getSimulationDistributionDirectory(project);
        if (oisDistributionDirPath.toFile().exists() && FileUtils.deleteDirectoryContent(oisDistributionDirPath) && oisDistributionDirPath.toFile().delete()) {
            log.info("Deleted generated distribution artifacts");
        }
    }
}
