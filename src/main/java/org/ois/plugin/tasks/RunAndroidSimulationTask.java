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
 * Run Android simulation of the project
 */
public class RunAndroidSimulationTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(RunAndroidSimulationTask.class);

    /**
     * Runs the simulation on an android device (user need to configure the connected device).
     */
    @TaskAction
    public void runAndroid() throws IOException {
        log.info("Running android simulation");
        Project project = getProject();
        SimulationUtils.runSimulation(
                project,
                RunnerConfiguration.RunnerType.Android,
                SimulationUtils.getRunSimulationTaskEnvVariables(SimulationUtils.getSimulationManifest(project), project)
        );
    }
}
