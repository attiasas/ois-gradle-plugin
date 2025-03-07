package org.ois.plugin.utils;

import org.gradle.api.Project;
import org.ois.core.project.Assets;
import org.ois.core.project.Entities;
import org.ois.core.project.SimulationManifest;
import org.ois.core.project.States;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.plugin.Const;
import org.ois.plugin.PluginConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * OIS Simulation Utilities
 */
public class SimulationUtils {
    private static final Logger log = LoggerFactory.getLogger(SimulationUtils.class);

    /**
     * Get the 'simulation' directory path, contains all the resources needed to configure and run the project simulation.
     * @param project - the OIS project
     * @return the path to its 'simulation' directory
     */
    public static Path getProjectSimulationConfigDirectory(Project project) {
        return project.getProjectDir().toPath().resolve("simulation");
    }

    /**
     * Get the 'build' directory path of the project
     * @param project - the OIS project
     * @return the path to its 'build' directory
     */
    public static Path getProjectBuildDirectory(Project project) {
        return project.getProjectDir().toPath().resolve("build");
    }

    /**
     * Get the 'resources' directory path of the project
     * @param project - the OIS project
     * @return the path to its 'resources' directory
     */
    public static Path getProjectResourcesDirectory(Project project) {
        return project.getProjectDir().toPath().resolve("src").resolve("main").resolve("resources");
    }

    /**
     * Get the 'ois' directory path for the project, the main directory generated and used by the plugin tasks.
     * @param project - the OIS project
     * @return the path to its 'ois' directory
     */
    public static Path getSimulationDirectory(Project project) {
        return getProjectBuildDirectory(project).resolve("ois");
    }

    /**
     * Get the 'resources' directory path, contains all the needed files for the simulation and the assets of the project simulation.
     * @param project - the OIS project
     * @return the path to its 'resources' directory
     */
    public static Path getSimulationRunnersResourcesDirectory(Project project) {
        return getSimulationDirectory(project).resolve("resources");
    }

    /**
     * Get the 'assets' directory path, contains all the assets of the project simulation.
     * @param project - the OIS project
     * @return the path to its 'assets' directory
     */
    public static Path getSimulationRunnersAssetsDirectory(Project project) {
        return getSimulationRunnersResourcesDirectory(project).resolve(Assets.ASSETS_DIRECTORY);
    }

    /**
     * Get the 'entities' directory path, contains all the entities definitions of the project simulation.
     * @param project - the OIS project
     * @return the path to its 'entities' directory
     */
    public static Path getSimulationRunnersEntitiesDirectory(Project project) {
        return getSimulationRunnersResourcesDirectory(project).resolve(Entities.ENTITIES_DIRECTORY);
    }

    /**
     * Get the 'states' directory path, contains all the states manifests of the project simulation.
     * @param project - the OIS project
     * @return the path to its 'states' directory
     */
    public static Path getSimulationRunnersStatesDirectory(Project project) {
        return getSimulationRunnersResourcesDirectory(project).resolve(States.STATES_DIRECTORY);
    }

    public static Path getSimulationRunnersIconsDirectory(Project project) {
        return getSimulationRunnersResourcesDirectory(project).resolve("icons");
    }

    public static Path getSimulationRunnersManifestFile(Project project) {
        return getSimulationRunnersResourcesDirectory(project).resolve(SimulationManifest.DEFAULT_FILE_NAME);
    }

    /**
     * Get the 'runners' directory path, contains all the runners that were used in the plugins tasks
     * @param project - the OIS project
     * @return the path to its 'runners' directory
     */
    public static Path getSimulationRunnersDirectory(Project project) {
        return getSimulationDirectory(project).resolve("runners");
    }

    /**
     * Get the 'distribution' directory path, contains all the generated production artifacts of the simulation
     * @param project - the OIS project
     * @return the path to its 'distribution' directory
     */
    public static Path getSimulationDistributionDirectory(Project project) {
        return getSimulationDirectory(project).resolve("distribution");
    }

    /** for tasks after Prepare, gets the actual simulation manifest in the runner that will be used **/
    public static SimulationManifest getSimulationManifest(Project project) throws IOException {
        // in the project build dir
        Path manifestPath = getSimulationRunnersManifestFile(project);
        try (InputStream in = Files.newInputStream(manifestPath)) {
            return JsonFormat.humanReadable().load(new SimulationManifest(), in);
        }
    }

    /**
     * General information about the simulation runner of a given project
     */
    public static class SimulationRunner {
        /** The working directory of the runners project **/
        public final Path workingDirectory;
        /** The version of the runner, if custom local source is used the version is 'custom' **/
        public final String version;
        /** Optional, if exists, is the path to local runner project used to run the project **/
        public final Path customSourceDir;

        /**
         * Create the simulation runner base on a given project configurations
         * @param project - to generate a runner for
         */
        public SimulationRunner(Project project) {
            this.customSourceDir = PluginConfiguration.getCustomRunnerPath(project);
            String version = PluginConfiguration.getCustomRunnerVersion(project);
            if (this.customSourceDir != null) {
                // custom source takes priority over custom version
                version = "custom";
            } else if (version == null) {
                version = Const.Versions.OIS_RUNNERS_VERSION;
            }
            this.version = version;
            this.workingDirectory = getSimulationRunnersDirectory(project).resolve(version);
        }

        /**
         * Check if the simulation runner is a custom runner provided by the local file system or official snapshot
         * @return - true if the runner is custom or false otherwise
         */
        public boolean isCustom() {
            return this.customSourceDir != null;
        }

        public Path getHtmlRunnerDirectory() { return this.workingDirectory.resolve("html-runner"); }

        public Path getDesktopRunnerDirectory() { return this.workingDirectory.resolve("desktop-runner"); }

        public Path getAndroidRunnerDirectory() { return this.workingDirectory.resolve("android-runner"); }

        @Override
        public String toString() {
            return "SimulationRunner{" +
                    "workingDirectory=" + workingDirectory +
                    ", version='" + version + '\'' +
                    ", customSourceDir=" + customSourceDir +
                    '}';
        }
    }

    /**
     * Get the simulation runner base on a given project configurations
     * @param project - to generate a runner for
     * @return A simulation runner for the OIS project
     */
    public static SimulationRunner getRunner(Project project) {
        return new SimulationRunner(project);
    }

    /**
     * Get the simulation runner expected environment variables required by the runners project to 'Run' a simulation.
     * @param manifest - the simulation manifest of the project.
     * @param project - the project to get its configurations and generate the env vars.
     * @return map of environment variables used to execute 'Run simulation' task in the runner project
     */
    public static Map<String, String> getRunSimulationTaskEnvVariables(SimulationManifest manifest, Project project) {
        return getDistributeSimulationTaskEnvVariables(manifest, project);
    }

    /**
     * Get the simulation 'Run' gradle tasks base on the given platform
     * @param platform - the platform (runner type project) to get its 'Run' gradle tasks
     * @return - the tasks to preform in order to 'Run' simulation on the given platform
     */
    public static String[] getRunnerRunSimulationGradleTasks(RunnerConfiguration.RunnerType platform) {
        switch (platform) {
            case Html -> {
                return new String[]{"serveHtml"};
            }
            case Desktop -> {
                return new String[]{"run"};
            }
            case Android -> {
                return new String[]{"installDebug", "runAndroid"};
            }
        }
        throw new RuntimeException("Unsupported platform type '" + platform + "'");
    }

    /**
     * Execute the 'Run Simulation' gradle task for a given project using an ois-runner
     * @param project - the OIS project to run its simulation
     * @param platform - the platform (ois-runner type) to run the simulation on
     * @param envVariables - the extra environment variables used in the task process
     */
    public static void runSimulation(Project project, RunnerConfiguration.RunnerType platform, Map<String, String> envVariables) {
        envVariables.putAll(System.getenv());
        GradleUtils.runTasks(getRunner(project).workingDirectory, envVariables, log, RunnerConfiguration.RunnerType.Android.equals(platform), getRunnerRunSimulationGradleTasks(platform));
    }

    /**
     * Get the simulation runner expected environment variables required by the runners project to 'Distribute' a simulation.
     * @param project - the project to get its configurations and generate the env vars.
     * @return map of environment variables used to execute 'Distribute simulation' task in the runner project
     */
    public static Map<String, String> getDistributeSimulationTaskEnvVariables(SimulationManifest manifest, Project project) {
        Map<String, String> env = new HashMap<>();
        env.put(Const.SimulationEnvVar.PROJECT_TITLE, manifest.getTitle());
        env.put(Const.SimulationEnvVar.PROJECT_VERSION, project.getVersion().toString());
        env.put(Const.SimulationEnvVar.PROJECT_VERSION_NUMBER, String.valueOf(((Map<String,Object>) project.getProperties()).getOrDefault("versionCode", 1)));
        env.put(Const.SimulationEnvVar.PROJECT_GROUP, project.getGroup().toString());
        return env;
    }

    /**
     * Get the simulation 'Distribute' gradle tasks base on the given platform
     * @param platform - the platform (runner type project) to get its 'Distribution' gradle tasks
     * @return - the tasks to preform in order to 'Distribute' simulation on the given platform
     */
    public static String[] getRunnerDistributionGradleTasks(RunnerConfiguration.RunnerType platform) {
        switch (platform) {
            case Html -> {
                return new String[]{"build"};
            }
            case Desktop -> {
                return new String[]{"jpackageImage"};
            }
            case Android -> {
                return new String[]{"packageRelease"};
            }
        }
        throw new RuntimeException("Unsupported platform type '" + platform + "'");
    }

    /**
     * Execute the 'Distribute Simulation' gradle task for a given project using an ois-runner
     * @param project - the OIS project to run its simulation
     * @param platform - the platform (ois-runner type) to run the simulation on
     * @param envVariables - the extra environment variables used in the task process
     */
    public static void distributeSimulation(Project project, RunnerConfiguration.RunnerType platform, Map<String, String> envVariables) {
        envVariables.putAll(System.getenv());
        GradleUtils.runTasks(getRunner(project).workingDirectory, envVariables, log, false, getRunnerDistributionGradleTasks(platform));
    }
}
