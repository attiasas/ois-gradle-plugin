package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Run Desktop simulation of the project
 */
public class RunDesktopSimulationTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(RunDesktopSimulationTask.class);

    /**
     * Runs the simulation on a desktop (opens a window and runs the simulation on it).
     */
    @TaskAction
    public void runDesktop() throws IOException {
        log.info("Running desktop simulation");
        Project project = getProject();
        SimulationUtils.runSimulation(
                project,
                RunnerConfiguration.RunnerType.Desktop,
                SimulationUtils.getRunSimulationTaskEnvVariables(SimulationUtils.getSimulationManifest(project), project)
        );
    }
}
