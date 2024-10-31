package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run Desktop simulation of the project
 */
public class RunDesktopSimulationTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(RunDesktopSimulationTask.class);

    /**
     * Runs the simulation on a desktop (opens a window and runs the simulation on it).
     */
    @TaskAction
    public void runDesktop() {
        log.info("Running desktop simulation");
        SimulationUtils.runSimulation(getProject(), RunnerConfiguration.RunnerType.Desktop, SimulationUtils.getRunSimulationTaskEnvVariables(getProject()));
    }
}
