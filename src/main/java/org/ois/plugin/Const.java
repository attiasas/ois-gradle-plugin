package org.ois.plugin;

import org.ois.core.utils.Version;

/**
 * All the constants used in the plugin
 */
public class Const {

    /**
     * All constants related to versions
     */
    public static class Versions {
        /** Minimum Gradle version to run the plugin - for desktop publish **/
        public static final Version MIN_GRADLE_VERSION = new Version("7.0.0");
         /** Minimum Java version to run the plugin - for desktop publish **/
        public static final Version MIN_JAVA_VERSION = new Version("15");
         /** Default version of the 'ois-runners' repository to be used. **/
        public static final String OIS_RUNNERS_VERSION = "main";
         /** Default version of the 'ois-core' library to be used. **/
        public static final Version OIS_CORE_VERSION = new Version("0.1-SNAPSHOT");
    }

    public static class SimulationEnvVar {
        public static final String PROJECT_GROUP = "OIS_PROJECT_GROUP";
        public static final String PROJECT_VERSION = "OIS_PROJECT_VERSION";
        public static final String PROJECT_TITLE = "OIS_PROJECT_TITLE";
    }

    /** The name of the extension object to configure the plugin **/
    public static final String PLUGIN_EXTENSION_NAME = "simulation";
    /** The name of the group the plugin tasks **/
    public static final String PLUGIN_GROUP_NAME = "ois";
    /** The URL of the runners repository that will be used to run the simulation **/
    public static final String OIS_RUNNERS_GIT_REPO_URL = "https://github.com/attiasas/ois-runners.git";

    /**
     * All constants related to tasks
     */
    public static class Tasks {
        /** The 'Clean Cache' task name **/
        public static final String CLEAN_CACHES_TASK_NAME = "cleanSimulationEnv";
        /** The 'Clean Cache' task description **/
        public static final String CLEAN_CACHES_TASK_DESCRIPTION = "Clean all cached items in OIS environment";
        /** The 'Validate OIS Project' task name **/
        public static final String VALIDATE_PROJECT_TASK_NAME = "validateProject";
        /** The 'Validate OIS Project' task description **/
        public static final String VALIDATE_PROJECT_TASK_DESCRIPTION = "Validate that all the needed OIS project configurations are valid";
        /** The 'Prepare Simulation' task name **/
        public static final String PREPARE_SIMULATION_TASK_NAME = "prepareSimulation";
        /** The 'Prepare Simulation' task description **/
        public static final String PREPARE_SIMULATION_TASK_DESCRIPTION = "Prepare the simulation environment to use the project";
        /** The 'Run HTML Simulation' task name **/
        public static final String RUN_HTML_SIMULATION_TASK_NAME = "runHtml";
        /** The 'Run HTML Simulation' task description **/
        public static final String RUN_HTML_SIMULATION_TASK_DESCRIPTION = "Run the simulation as html (In dev mode), will be hosted at http://localhost:8080/";
        /** The 'Run HTML Simulation' task name **/
        public static final String RUN_DESKTOP_SIMULATION_TASK_NAME = "runDesktop";
        /** The 'Run HTML Simulation' task description **/
        public static final String RUN_DESKTOP_SIMULATION_TASK_DESCRIPTION = "Run the simulation (In dev mode) in a desktop window";
        /** The 'Distribute Simulation task name **/
        public static final String DISTRIBUTE_SIMULATION_TASK_NAME = "export";
        /** The 'Distribute Simulation task description **/
        public static final String DISTRIBUTE_SIMULATION_TASK_DESCRIPTION = "Generate the simulation production artifacts, ready for distribution at './build/ois/distribution'";
    }

}
