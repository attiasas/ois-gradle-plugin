package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.project.SimulationManifest;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Validate OIS project requirements task
 */
public class ValidateProjectTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(ValidateProjectTask.class);

    /**
     * Validate that the project is compatible with OIS requirements to be run as simulation
     */
    @TaskAction
    public void validateProject() throws IOException {
        log.info("Validate project simulation configurations");
        // Check if 'simulation' directory exists in the project
        if (!SimulationUtils.getProjectSimulationConfigDirectory(getProject()).toFile().exists()) {
            throw new RuntimeException("Can't find 'simulation' directory in the project directory " + getProject().getProjectDir());
        }
        // Check if 'simulation.ois' file exists
        Path projectSimulationManifestPath = SimulationUtils.getProjectSimulationConfigDirectory(getProject()).resolve(SimulationManifest.DEFAULT_FILE_NAME);
        if(!projectSimulationManifestPath.toFile().exists()) {
            throw new RuntimeException("Can't find 'simulation.ois' manifest file at the project 'simulation' directory");
        }
        SimulationManifest manifest;
        try (InputStream in = Files.newInputStream(projectSimulationManifestPath)) {
            manifest = JsonFormat.humanReadable().load(new SimulationManifest(), in);
        }
        // Check that states exists
        if (manifest.getStates().isEmpty()) {
            throw new RuntimeException("'states' attribute in the simulation manifest ('simulation.ois') can't be empty");
        }
        // Check that initialState is a valid value that exists as a key in states attribute
        if (manifest.getInitialState() == null || !manifest.getStates().containsKey(manifest.getInitialState())) {
            throw new RuntimeException("You must specify a valid 'initialState' attribute in the simulation manifest ('simulation.ois') that is a key in 'states'");
        }
    }
}
