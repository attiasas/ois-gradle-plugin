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
 * Clean up the 'ois' directory in the project 'build' directory.
 * Mostly used for developing or when issues occurs.
 */
public class CleanCachesTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(CleanCachesTask.class);

    /**
     * Default constructor
     */
    public CleanCachesTask(){super();}

    /**
     * Clean up OIS cache directories in the project 'build' directory
     */
    @TaskAction
    public void cleanCache() {
        log.info("Clean OIS cache items");
        Project project = getProject();
        if (!SimulationUtils.getProjectBuildDirectory(project).toFile().exists()) {
            log.info("Project 'build' directory not exists, Nothing to do.");
            return;
        }
        if (!SimulationUtils.getSimulationDirectory(project).toFile().exists()) {
            log.info("OIS directory not exists in project 'build' directory, Nothing to do.");
            return;
        }
        // Clean
        Path oisRunnersDirPath = SimulationUtils.getSimulationRunnersDirectory(project);
        if (oisRunnersDirPath.toFile().exists() && FileUtils.deleteDirectoryContent(oisRunnersDirPath) && oisRunnersDirPath.toFile().delete()) {
            log.info("Deleted cached runners directory.");
        }
        Path oisAssetsDirPath = SimulationUtils.getSimulationResourcesDirectory(project);
        if (oisAssetsDirPath.toFile().exists() && FileUtils.deleteDirectoryContent(oisAssetsDirPath) && oisAssetsDirPath.toFile().delete()) {
            log.info("Deleted generated assets directory");
        }
        Path oisDistributionDirPath = SimulationUtils.getSimulationDistributionDirectory(project);
        if (oisDistributionDirPath.toFile().exists() && FileUtils.deleteDirectoryContent(oisDistributionDirPath) && oisDistributionDirPath.toFile().delete()) {
            log.info("Deleted generated distribution artifacts");
        }
    }
}
