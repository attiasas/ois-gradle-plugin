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
import org.ois.plugin.tools.FileContentReplacer;
import org.ois.plugin.utils.*;
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
        SimulationManifest manifest = prepareResources(runner, getProject());
        // Prepare Html extra steps
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Html)) {
            log.debug("Prepare html resources...");
            prepareHtmlResources(getProject(), runner, manifest);
        }
        // Prepare Desktop extra steps
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Desktop)) {
            log.debug("Prepare Desktop resources...");
            prepareDesktopResources(getProject(), runner);
        }
        // Prepare Android extra steps
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Android)) {
            log.debug("Prepare Android resources...");
            prepareAndroidResources(getProject(), runner, manifest);
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

    private SimulationManifest prepareResources(SimulationUtils.SimulationRunner runner, Project project) throws IOException, URISyntaxException {
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
        // Prepare Assets
        prepareAssets(project, projectSimulationDir);
        // Prepare Entities information
        prepareEntitiesResources(project, projectSimulationDir);
        // Prepare Icons
        prepareIcons(runner, projectSimulationDir);
        // Create simulation manifest that will be used by runners
        return transferManifestToRunner(projectSimulationDir);
    }

    private void prepareEntitiesResources(Project project, Path projectSimulationDir) throws IOException {

    }

    private void prepareAssets(Project project, Path projectSimulationDir) throws IOException {
        // Copy files from assets directory in simulation dir
        Path projectAssetsDir = projectSimulationDir.resolve(Assets.ASSETS_DIRECTORY);
        if (projectAssetsDir.toFile().exists() && projectAssetsDir.toFile().isDirectory()) {
            log.debug("'assets' directory located, copy content");
            FileUtils.copyDirectoryContent(projectAssetsDir, SimulationUtils.getSimulationRunnersAssetsDirectory(project));
        }
        // Copy files from resources directory in the project
        Path projectResourceDir = SimulationUtils.getProjectResourcesDirectory(project);
        if (projectResourceDir.toFile().exists() && projectResourceDir.toFile().isDirectory()) {
            log.debug("'resources' directory located, copy content");
            FileUtils.copyDirectoryContent(projectResourceDir, SimulationUtils.getSimulationRunnersAssetsDirectory(project));
        }
    }

    private void prepareIcons(SimulationUtils.SimulationRunner runner, Path projectSimulationDir) throws IOException {
        Path projectIconsDir = projectSimulationDir.resolve("icons");
        Path targetIconDir = SimulationUtils.getSimulationRunnersIconsDirectory(getProject());
        // First copy default icons if any are missing
        IconHandler.copyDefaultIcons(runner, targetIconDir);
        // Handle custom if exists
        if (!projectIconsDir.toFile().exists() || !projectIconsDir.toFile().isDirectory()) {
            return;
        }
        log.info("'icons' directory located, copy content");
        IconHandler.copyCustomDesktopIcons(projectIconsDir, targetIconDir);
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
                "LOG_LEVEL", PluginConfiguration.getLogLevel(project),
                "DEBUG_MODE", PluginConfiguration.getDebugMode(project)
        ));
        String[] logTopics = PluginConfiguration.getLogTopics(project);
        if (logTopics != null) {
            htmlSimulationConfigFileAttributes.put("LOG_TOPICS", logTopics);
        }
        // Generate new content with injected values
        String updatedContent = FileContentReplacer.Java.replaceJavaStaticFinalVals(HtmlUtils.getSimulationConfigContent(runner.getHtmlRunnerDirectory()), htmlSimulationConfigFileAttributes);
        log.debug("Replacing 'HtmlSimulationConfig.java' content at runner directory with the project config:\n{}", updatedContent);
        Files.writeString(HtmlUtils.getSimulationConfigPath(runner.getHtmlRunnerDirectory()), updatedContent);
        // Generate reflections file
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
        Map<String, Object> desktopSimulationConfigFileAttributes = new Hashtable<>(Map.of(
                "LOG_LEVEL", PluginConfiguration.getLogLevel(project),
                "DEBUG_MODE", PluginConfiguration.getDebugMode(project)
        ));
        String[] logTopics = PluginConfiguration.getLogTopics(project);
        if (logTopics != null) {
            desktopSimulationConfigFileAttributes.put("LOG_TOPICS", logTopics);
        }
        String devModeDir = PluginConfiguration.getDevModeDir(project);
        if (devModeDir != null && !devModeDir.isBlank()) {
            desktopSimulationConfigFileAttributes.put("DEV_MODE_DIR", devModeDir);
        }
        // Generate new content with injected values
        String updatedContent = FileContentReplacer.Java.replaceJavaStaticFinalVals(DesktopUtils.getSimulationConfigContent(runner.getDesktopRunnerDirectory()), desktopSimulationConfigFileAttributes);
        log.debug("Replacing 'DesktopSimulationConfig.java' content at runner directory with the project config:\n{}", updatedContent);
        Files.writeString(DesktopUtils.getSimulationConfigPath(runner.getDesktopRunnerDirectory()), updatedContent);
    }

    private void prepareAndroidResources(Project project, SimulationUtils.SimulationRunner runner, SimulationManifest manifest) throws IOException {
        // Attributes to inject
        Map<String, Object> androidSimulationConfigFileAttributes = new Hashtable<>(Map.of(
                "LOG_LEVEL", PluginConfiguration.getLogLevel(project),
                "DEBUG_MODE", PluginConfiguration.getDebugMode(project)
        ));
        String[] logTopics = PluginConfiguration.getLogTopics(project);
        if (logTopics != null) {
            androidSimulationConfigFileAttributes.put("LOG_TOPICS", logTopics);
        }
        // Generate new content with injected values
        String updatedContent = FileContentReplacer.Java.replaceJavaStaticFinalVals(AndroidUtils.getSimulationConfigContent(runner.getAndroidRunnerDirectory()), androidSimulationConfigFileAttributes);
        log.debug("Replacing 'AndroidSimulationConfig.java' content at runner directory with the project config:\n{}", updatedContent);
        Files.writeString(AndroidUtils.getSimulationConfigPath(runner.getAndroidRunnerDirectory()), updatedContent);
        // Inject title
        updatedContent = FileContentReplacer.Xml.replaceXmlAttributes(AndroidUtils.getAndroidRunnerResourceStringValuesContent(runner.getAndroidRunnerDirectory()), new Hashtable<>(Map.of("app_name", manifest.getTitle())));
        log.debug("Replacing 'strings.xml' content at runner directory with the project title:\n{}", updatedContent);
        Files.writeString(AndroidUtils.getAndroidRunnerResourceStringValuesPath(runner.getAndroidRunnerDirectory()), updatedContent);
    }
}
