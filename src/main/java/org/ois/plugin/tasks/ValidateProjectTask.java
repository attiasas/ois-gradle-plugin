package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.project.SimulationManifest;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate OIS project requirements task
 */
public class ValidateProjectTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(ValidateProjectTask.class);

    /**
     * Validate that the project is compatible with OIS requirements to be run as simulation
     */
    @TaskAction
    public void validateProject() {
        log.info("Validate project simulation configurations");
        // Check if 'simulation' directory exists in the project
        if (!SimulationUtils.getProjectRawAssetsDirectory(getProject()).toFile().exists()) {
            throw new RuntimeException("Can't find 'simulation' directory in the project directory " + getProject().getProjectDir());
        }
        // Check if 'simulation.ois' file exists
        if(!SimulationUtils.getProjectRawAssetsDirectory(getProject()).resolve(SimulationManifest.DEFAULT_FILE_NAME).toFile().exists()) {
            throw new RuntimeException("Can't find 'simulation.ois' manifest file at the project 'simulation' directory");
        }
    }
}
