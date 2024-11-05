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
 * Run Html simulation of the project
 */
public class RunHtmlSimulationTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(RunHtmlSimulationTask.class);

    /**
     * Generate the Html JS project from the project source,
     * Set up a Server at '<a href="http://localhost:8080/">localhost:8080</a>' to serve the Html simulation.
     */
    @TaskAction
    public void runHtml() throws IOException {
        log.info("Running html simulation");
        Project project = getProject();
        SimulationUtils.runSimulation(
                project,
                RunnerConfiguration.RunnerType.Html,
                SimulationUtils.getRunSimulationTaskEnvVariables(SimulationUtils.getSimulationManifest(project), project)
        );
    }
}
