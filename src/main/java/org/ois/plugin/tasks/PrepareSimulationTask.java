package org.ois.plugin.tasks;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.project.Assets;
import org.ois.core.project.SimulationManifest;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.core.utils.io.FileUtils;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.plugin.Const;
import org.ois.plugin.PluginConfiguration;
import org.ois.plugin.tools.IconHandler;
import org.ois.plugin.tools.JavaFileContentReplacer;
import org.ois.plugin.utils.DesktopUtils;
import org.ois.plugin.utils.GitUtils;
import org.ois.plugin.utils.HtmlUtils;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
    public void prepareSimulation() throws IOException, GitAPIException, URISyntaxException {
        log.info("Prepare components and initialize environment");
        if (!SimulationUtils.getProjectBuildDirectory(getProject()).toFile().exists()) {
            throw new IllegalStateException("Project must be built before preparing its simulation");
        }
        Path oisSimulationDirPath = SimulationUtils.getSimulationDirectory(getProject());
        if (FileUtils.createDirIfNotExists(oisSimulationDirPath, true)) {
            log.debug("Created project simulation directory");
        }
        // Prepare runner and resources for the simulation
        SimulationUtils.SimulationRunner runner = prepareRunners(getProject());
        SimulationManifest manifest = prepareResources(getProject());
        // Prepare Html extra steps
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Html)) {
            log.debug("Prepare html resources...");
            prepareHtmlResources(getProject(), runner, manifest);
        }
        // Prepare Desktop extra steps
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Desktop)) {
            log.debug("Prepare Desktop resources....");
            prepareDesktopResources(getProject(), runner);
        }
        log.info("Simulation environment is ready");
    }

    private SimulationUtils.SimulationRunner prepareRunners(Project project) throws IOException, GitAPIException {
        Path oisRunnersDirPath = SimulationUtils.getSimulationRunnersDirectory(project);
        if (FileUtils.createDirIfNotExists(oisRunnersDirPath, true)) {
            log.debug("Created simulation runners directory");
        }
        SimulationUtils.SimulationRunner runner = SimulationUtils.getRunner(project);
        if (FileUtils.createDirIfNotExists(runner.workingDirectory, true)) {
            log.debug("Created simulation runner '{}' directory {}", runner.version, runner.workingDirectory);
            if (!runner.isCustom()) {
                // Fetch runner only if not exists in project cache
                try (Git git = GitUtils.cloneRepoByTag(Const.OIS_RUNNERS_GIT_REPO_URL, runner.version, runner.workingDirectory)) {
                    log.debug("Runner content downloaded successfully");
                }
            }
        }
        if (runner.isCustom()) {
            // Always copy custom source dir
            FileUtils.copyDirectoryContent(runner.customSourceDir, runner.workingDirectory, "**/*.git*/**");
            log.debug("Runner custom content copied successfully");
        }
        log.info("Using simulation runner: {}", runner);
        return runner;
    }

    private SimulationManifest prepareResources(Project project) throws IOException, URISyntaxException {
        Path oisResourcesDirPath = SimulationUtils.getSimulationRunnersResourcesDirectory(project);
        if (FileUtils.createDirIfNotExists(oisResourcesDirPath, true)) {
            log.debug("Created ois simulation 'resources' directory");
        }
        log.info("Copy project simulation resources...");
        // Copy project assets
        Path projectSimulationDir = PluginConfiguration.getCustomSimulationDirPath(project);
        if (projectSimulationDir == null) {
            projectSimulationDir = SimulationUtils.getProjectSimulationConfigDirectory(project);
        }
        log.debug("Project simulation directory: {}", projectSimulationDir);
        Path projectAssetsDir = projectSimulationDir.resolve(Assets.ASSETS_DIRECTORY);
        if (projectAssetsDir.toFile().exists() && projectAssetsDir.toFile().isDirectory()) {
            log.debug("'assets' directory located, copy content");
            FileUtils.copyDirectoryContent(projectAssetsDir, SimulationUtils.getSimulationRunnersAssetsDirectory(project));
        }
        Path projectIconsDir = projectSimulationDir.resolve("icons");
        if (projectIconsDir.toFile().exists() && projectIconsDir.toFile().isDirectory()) {
            log.info("'icons' directory located, copy content");
            FileUtils.copyDirectoryContent(projectIconsDir, SimulationUtils.getSimulationRunnersIconsDirectory(project));
        } else {
            log.info("Using default icons");
            IconHandler.copyDefaultIcons(SimulationUtils.getSimulationRunnersIconsDirectory(project));
        }
        // Create simulation manifest that will be used by runners
        return transferManifestToRunner(projectSimulationDir);
    }

    private SimulationManifest transferManifestToRunner(Path projectSimulationDir) throws IOException {
        SimulationManifest manifest = loadProjectManifest(projectSimulationDir);
        String manifestData = JsonFormat.humanReadable().serialize(manifest);
        log.debug("Runners simulation manifest:\n{}", manifestData);
        Files.writeString(SimulationUtils.getSimulationRunnersManifestFile(getProject()), manifestData);
        return manifest;
    }

    private SimulationManifest loadProjectManifest(Path projectSimulationDir) throws IOException {
        try (InputStream in = Files.newInputStream(projectSimulationDir.resolve(SimulationManifest.DEFAULT_FILE_NAME))) {
            SimulationManifest manifest = JsonFormat.humanReadable().load(new SimulationManifest(), in);
            // Default value for optional attribute 'title'
            if (manifest.getTitle().isBlank()) {
                manifest.setTitle("OIS");
            }
            // Default value for optional attribute 'platforms'
            if (manifest.getPlatforms().isEmpty()) {
                manifest.getPlatforms().addAll(List.of(RunnerConfiguration.RunnerType.values()));
            }
            return manifest;
        }
    }

    private void prepareHtmlResources(Project project, SimulationUtils.SimulationRunner runner, SimulationManifest manifest) throws IOException {
        // Attributes to inject
        Map<String, Object> htmlSimulationConfigFileAttributes = new Hashtable<>(Map.of(
                "TITLE", manifest.getTitle(),
                "SCREEN_WIDTH", manifest.getScreenWidth(),
                "SCREEN_HEIGHT", manifest.getScreenHeight(),
                "LOG_LEVEL", PluginConfiguration.getLogLevel(project)
        ));
        String[] logTopics = PluginConfiguration.getLogTopics(project);
        if (logTopics != null) {
            htmlSimulationConfigFileAttributes.put("LOG_TOPICS", logTopics);
        }
        // Generate new content with injected values
        String updatedContent = JavaFileContentReplacer.replaceJavaStaticFinalVals(HtmlUtils.getSimulationConfigContent(runner.getHtmlRunnerDirectory()), htmlSimulationConfigFileAttributes);
        log.debug("Replacing 'HtmlSimulationConfig.java' content at runner directory with the project config:\n{}", updatedContent);
        // Save
        Files.writeString(HtmlUtils.getSimulationConfigPath(runner.getHtmlRunnerDirectory()), updatedContent);
        log.debug("Generating Reflection items file content");
        String reflectionsContent = HtmlUtils.generateReflectionFileContent(project);
        if (reflectionsContent.isBlank()) {
            log.debug("No items to reflect");
            return;
        }
        Files.writeString(HtmlUtils.getReflectionsItemsFilePath(project), reflectionsContent);
    }

    private void prepareDesktopResources(Project project, SimulationUtils.SimulationRunner runner) throws IOException {
        // Attributes to inject
        Map<String, Object> desktopSimulationConfigFileAttributes = new Hashtable<>(Map.of("LOG_LEVEL", PluginConfiguration.getLogLevel(project)));
        String[] logTopics = PluginConfiguration.getLogTopics(project);
        if (logTopics != null) {
            desktopSimulationConfigFileAttributes.put("LOG_TOPICS", logTopics);
        }
        // Generate new content with injected values
        String updatedContent = JavaFileContentReplacer.replaceJavaStaticFinalVals(DesktopUtils.getSimulationConfigContent(runner.getDesktopRunnerDirectory()), desktopSimulationConfigFileAttributes);
        log.debug("Replacing 'DesktopSimulationConfig.java' content at runner directory with the project config:\n{}", updatedContent);
        // Save
        Files.writeString(DesktopUtils.getSimulationConfigPath(runner.getDesktopRunnerDirectory()), updatedContent);
    }
}
