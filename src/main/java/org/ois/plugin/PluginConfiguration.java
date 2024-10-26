package org.ois.plugin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.ois.core.utils.log.ILogger;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;

/**
 * The extension to the plugin, used in the build.gradle file, allows you to configure the plugin actions.
 */
public class PluginConfiguration {

    /** If exists, it will be used as the log level in the development **/
    private String logLevel;
    /** If exists, it will filter the logs with topics to only show the provided **/
    private String[] logTopics;
    /** OIS Runners configurations **/
    private RunnerConfig runner;
    /** OIS Project configurations **/
    private ProjectConfig projectConfig;
    /** OIS Project export configurations **/
    private ExportConfig exportConfig;

    /** OIS Runners configurations **/
    public static class RunnerConfig {
        /**
         * If exists, it will use the runners project in the provided directory (mostly used for developing the plugin).
         * Else, will use/download (base on the configured version) the runners in the plugin cached directory.
         * **/
        private String runnerDirectory;
        /**
         * If exists, it will use the specific runners tag/branch provided (mostly used for developing/troubleshooting).
         * Else, it will use the latest version.
         **/
        private String runnerVersion;

        public void setRunnerDirectory(String runnerDirectory) {
            this.runnerDirectory = runnerDirectory;
        }

        public void setRunnerVersion(String runnerVersion) {
            this.runnerVersion = runnerVersion;
        }

        public String getRunnerDirectory() {
            return this.runnerDirectory;
        }

        public String getRunnerVersion() {
            return this.runnerVersion;
        }
    }

    /** OIS Project configurations **/
    public static class ProjectConfig {

        /**
         * If exists, it will use the provided directory as the 'simulation' directory for the project.
         * Else, will try to locate the 'simulation' directory in the project root directory.
         **/
        private String directory;

        @Inject
        public ProjectConfig(){}

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getDirectory() {
            return this.directory;
        }
    }

    /** OIS Project export configurations **/
    public static class ExportConfig {
        /**
         * If exists, it will use the provided directory as the 'export' directory for the project.
         * Else, artifacts will be created at {project-directory}/build/ois/distribution
         **/
        private String directory;

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getDirectory() {
            return this.directory;
        }

    }

    public void runner(Action<RunnerConfig> runnerConfigAction) {
        this.runner = new RunnerConfig();
        runnerConfigAction.execute(this.runner);
    }

    public void projectConfig(Action<ProjectConfig> projectConfigAction) {
        this.projectConfig = new ProjectConfig();
        projectConfigAction.execute(this.projectConfig);
    }

    public void export(Action<ExportConfig> exportConfigAction) {
        this.exportConfig = new ExportConfig();
        exportConfigAction.execute(this.exportConfig);
    }

    public ProjectConfig getProjectConfig() {
        if (this.projectConfig == null) {
            // Optional configs, we create if not exists
            this.projectConfig = new ProjectConfig();
        }
        return this.projectConfig;
    }

    public RunnerConfig getRunner() {
        if (this.runner == null) {
            // Optional configs, we create if not exists
            this.runner = new RunnerConfig();
        }
        return this.runner;
    }

    public ExportConfig getExport() {
        if (this.exportConfig == null) {
            // Optional config, we create if not exists
            this.exportConfig = new ExportConfig();
        }
        return this.exportConfig;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public String[] getLogTopics() {
        return logTopics;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setLogTopics(String[] logTopics) {
        this.logTopics = logTopics;
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
        String customRunnersPath = extension.getRunner().getRunnerDirectory();
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
        String customRunnerVersion = extension.getRunner().getRunnerVersion();
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
        String customSimulationPath = extension.getProjectConfig().getDirectory();
        if (customSimulationPath == null || customSimulationPath.trim().isEmpty()) {
            return null;
        }
        File simulationDir = new File(customSimulationPath.trim());
        return simulationDir.exists() && simulationDir.isDirectory() ? simulationDir.toPath() : null;
    }

    public static String getLogLevel(Project project) {
        PluginConfiguration extension = getPluginConfigurations(project);
        String logLevel = extension.getLogLevel();
        if (logLevel == null || logLevel.trim().isEmpty()) {
            return ILogger.Level.Info.name();
        }
        return logLevel;
    }

    public static String[] getLogTopics(Project project) {
        PluginConfiguration extension = getPluginConfigurations(project);
        String[] logTopics = extension.getLogTopics();
        if (logTopics == null || logTopics.length == 0) {
            return null;
        }
        return logTopics;
    }

    public static Path getCustomExportDirPath(Project project) {
        PluginConfiguration extension = getPluginConfigurations(project);
        String customExportDirPath = extension.getExport().getDirectory();
        if (customExportDirPath == null || customExportDirPath.trim().isEmpty()) {
            return null;
        }
        File customExportDir = new File(customExportDirPath.trim());
        return customExportDir.exists() && customExportDir.isDirectory() ? customExportDir.toPath() : null;
    }
}
