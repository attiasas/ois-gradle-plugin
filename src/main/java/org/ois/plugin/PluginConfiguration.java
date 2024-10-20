package org.ois.plugin;

import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;

/**
 * The extension to the plugin, used in the build.gradle file, allows you to configure the plugin actions.
 */
public class PluginConfiguration {
    /**
    * If exists, it will use the runners project in the provided directory (mostly used for developing the plugin).
    * Else, will use/download (base on the configured version) the runners in the plugin cached directory.
    * **/
    private String runnersDirPath;
    /**
    * If exists, it will use the specific runners tag/branch provided (mostly used for developing/troubleshooting).
    * Else, it will use the latest version.
    **/
    private String runnersVersion;
    /**
     * If exists, it will use the provided directory as the 'simulation' directory for the project.
     * Else, will try to locate the 'simulation' directory in the project root directory.
    **/
    private String simulationDirPath;

    /**
     * Set a custom runners source directory path
     * @param path - directory with a runners project
     */
    public void setRunnersDirPath(String path) {
        this.runnersDirPath = path;
    }
    /**
     * Get a project custom runners source directory path
     * @return custom runners source directory path if exists
     */
    public String getRunnersDirPath() {
        return this.runnersDirPath;
    }
    /**
     * Set a custom runners version to be used on the project
     * @param tag - the custom runners tag/branch
     */
    public void setRunnersVersion(String tag) {
        this.runnersVersion = tag;
    }
    /**
     * Get a custom runners version to be used on the project
     * @return - the custom runners tag/branch to be used if exists
     */
    public String getRunnersVersion() {
        return this.runnersVersion;
    }
    /**
     * Set a custom 'simulation' directory, containing the resources and configurations for the project simulation
     * @param simulationDirPath - the custom 'simulation' directory path
     */
    public void setSimulationDirPath(String simulationDirPath) {
        this.simulationDirPath = simulationDirPath;
    }
    /**
     * Get a custom 'simulation' directory, containing the resources and configurations for the project simulation
     * @return - the custom 'simulation' directory path, if exists
     */
    public String getSimulationDirPath() {
        return this.simulationDirPath;
    }

    // Static getters and Default object generator by project

    /**
     * Create default plugin configurations to the project if not exists
     * @param project - the project to check its plugin configuration
     */
    public static void createDefaultConfigurationsIfNotExists(Project project) {
        PluginConfiguration config = getPluginConfigurations(project);
        if (config == null) {
            project.getExtensions().create(Const.PLUGIN_EXTENSION_NAME, PluginConfiguration.class);
        }
    }

    /**
     * Get a given plugin configurations if exists
     * @param project - the project to check its plugin configuration
     * @return the project plugin configurations if exists, null otherwise
     */
    public static PluginConfiguration getPluginConfigurations(Project project) {
        while (project != null) {
            PluginConfiguration config = project.getExtensions().findByType(PluginConfiguration.class);
            if (config != null) {
                return config;
            }
            project = project.getParent();
        }
        return null;
    }

    /**
     * Get a given project 'runnersDirPath' plugins configuration attribute value if exists
     * @param project - the project to get its plugin configuration value
     * @return - runnersDirPath value for the project if exists, null otherwise.
     */
    public static Path getCustomRunnerPath(Project project) {
        PluginConfiguration extension = getPluginConfigurations(project);
        String customRunnersPath = extension.getRunnersDirPath();
        if (customRunnersPath == null || customRunnersPath.trim().isEmpty()) {
            return null;
        }
        File runnerDir = new File(customRunnersPath.trim());
        return runnerDir.exists() && runnerDir.isDirectory() ? runnerDir.toPath() : null;
    }

    /**
     * Get a given project 'runnersVersion' plugins configuration attribute value if exists
     * @param project - the project to get its plugin configuration value
     * @return - runnersVersion value for the project if exists, null otherwise.
     */
    public static String getCustomRunnerVersion(Project project) {
        PluginConfiguration extension = getPluginConfigurations(project);
        String customRunnerVersion = extension.getRunnersVersion();
        if (customRunnerVersion == null || customRunnerVersion.trim().isEmpty()) {
            return null;
        }
        return customRunnerVersion.trim();
    }

    /**
     * Get a given project 'simulationDirPath' plugins configuration attribute value if exists
     * @param project - the project to get its plugin configuration value
     * @return - simulationDirPath value for the project if exists, null otherwise.
     */
    public static Path getCustomSimulationDirPath(Project project) {
        PluginConfiguration extension = getPluginConfigurations(project);
        String customSimulationPath = extension.getSimulationDirPath();
        if (customSimulationPath == null || customSimulationPath.trim().isEmpty()) {
            return null;
        }
        File simulationDir = new File(customSimulationPath.trim());
        return simulationDir.exists() && simulationDir.isDirectory() ? simulationDir.toPath() : null;
    }
}
