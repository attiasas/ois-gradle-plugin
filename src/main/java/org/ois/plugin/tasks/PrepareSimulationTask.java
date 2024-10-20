package org.ois.plugin.tasks;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.utils.FileUtils;
import org.ois.plugin.Const;
import org.ois.plugin.utils.GitUtils;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Make sure the needed components for the deployer are ready to be used for running/debugging/exporting the OIS project
 */
public class PrepareSimulationTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(PrepareSimulationTask.class);

    /**
     * Prepare the components and environment for OIS simulation actions
     * @throws IOException - In case of error when preparing the local env
     * @throws GitAPIException - In case of error when downloading the runners repository
     */
    @TaskAction
    public void prepareSimulation() throws IOException, GitAPIException {
        log.info("Prepare components and initialize environment");
        if (!SimulationUtils.getProjectBuildDirectory(getProject()).toFile().exists()) {
            throw new IllegalStateException("Project must be built before preparing its simulation");
        }
        Path oisSimulationDirPath = SimulationUtils.getSimulationDirectory(getProject());
        if (!oisSimulationDirPath.toFile().exists()) {
            if (oisSimulationDirPath.toFile().mkdir()) {
                log.info("Created project simulation directory");
            }
        }
        prepareRunners(getProject());
        prepareResources(getProject());
    }

    private void prepareRunners(Project project) throws IOException, GitAPIException {
        Path oisRunnersDirPath = SimulationUtils.getSimulationRunnersDirectory(project);
        if (!oisRunnersDirPath.toFile().exists() && oisRunnersDirPath.toFile().mkdir()) {
            log.info("Created simulation runners directory");
        }
        // Fetch runner if not exists in project cache
        SimulationUtils.SimulationRunner runner = SimulationUtils.getRunner(project);
        if (!runner.workingDirectory.toFile().exists() && runner.workingDirectory.toFile().mkdir()) {
            log.info("Created simulation runner '{}' directory {}", runner.version, runner.workingDirectory);
            if (!runner.isCustom()) {
                try (Git git = GitUtils.cloneRepoByTag(Const.OIS_RUNNERS_GIT_REPO_URL, runner.version, runner.workingDirectory)) {
                    log.info("Runner content downloaded successfully");
                }
            }
        }
        if (runner.isCustom()) {
            // Always copy custom source dir
            FileUtils.copyDirectoryContent(runner.customSourceDir, runner.workingDirectory, "**/*.git*/**");
            log.info("Runner custom content copied successfully");
        }
        log.info("Using simulation runner version '{}'", runner.version);
    }

    private void prepareResources(Project project) throws IOException {
        Path oisAssetsDirPath = SimulationUtils.getSimulationAssetsDirectory(project);
        if (!oisAssetsDirPath.toFile().exists() && oisAssetsDirPath.toFile().mkdir()) {
            log.info("Created ois simulation assets directory");
        }
        // copy content of 'simulation' directory (already validated that exists)
        FileUtils.copyDirectoryContent(SimulationUtils.getProjectRawAssetsDirectory(project), oisAssetsDirPath);
    }
}
