package org.ois.plugin.tasks;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.utils.io.FileUtils;
import org.ois.plugin.Const;
import org.ois.plugin.utils.GitUtils;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;

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
        if (FileUtils.createDirIfNotExists(oisSimulationDirPath, true)) {
            log.info("Created project simulation directory");
        }
        prepareRunners(getProject());
        prepareResources(getProject());
        log.info("Simulation environment is ready");
    }

    private void prepareRunners(Project project) throws IOException, GitAPIException {
        Path oisRunnersDirPath = SimulationUtils.getSimulationRunnersDirectory(project);
        if (FileUtils.createDirIfNotExists(oisRunnersDirPath, true)) {
            log.info("Created simulation runners directory");
        }
        SimulationUtils.SimulationRunner runner = SimulationUtils.getRunner(project);
        if (FileUtils.createDirIfNotExists(runner.workingDirectory, true)) {
            log.info("Created simulation runner '{}' directory {}", runner.version, runner.workingDirectory);
            if (!runner.isCustom()) {
                // Fetch runner only if not exists in project cache
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
        Path oisResourcesDirPath = SimulationUtils.getSimulationResourcesDirectory(project);
        if (FileUtils.createDirIfNotExists(oisResourcesDirPath, true)) {
            log.info("Created ois simulation 'resources' directory");
        }
//        log.info("Copy project Jar files...");
//        for (Path jarPath : getProjectJars()) {
//            FileUtils.copyFile(jarPath, oisResourcesDirPath, true);
//            log.debug("Copied {}", jarPath);
//        }
        log.info("Copy project simulation resources...");
        FileUtils.copyDirectoryContent(SimulationUtils.getProjectRawAssetsDirectory(project), oisResourcesDirPath);
    }

//    public Path[] getProjectJars() {
//        Set<File> jarFiles = getProject().getTasks().getByName("jar").getOutputs().getFiles().getFiles();
//        if (jarFiles.isEmpty()) {
//            log.warn("Can't find simulation project jars to deploy into the runners");
//            return new Path[]{};
//        }
//        Path[] paths = jarFiles.stream().map(File::toPath).toArray(Path[]::new);
//        log.debug("Using Jars: {}", Arrays.toString(paths));
//        return paths;
//    }
}
