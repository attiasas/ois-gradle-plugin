package org.ois.plugin;

import org.gradle.api.*;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;
import org.ois.core.utils.Version;
import org.ois.plugin.tasks.PrepareSimulationTask;
import org.ois.plugin.tasks.ValidateProjectTask;
import org.ois.plugin.utils.GradleUtils;
import org.ois.plugin.utils.TaskUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plugin entry point
 */
public class SimulationPlugin implements Plugin<Project> {
    private static final Logger log = LoggerFactory.getLogger(SimulationPlugin.class);

    @Override
    public void apply(@NotNull Project target) {
        validateProjectCompatible(target);
        PluginConfiguration.createDefaultConfigurationsIfNotExists(target);
        addRequiredDependencies(target);
        addPluginTasks(target);
    }

    private void validateProjectCompatible(Project target) {
        log.info("[OIS] Validate plugin requirements");
        // Assert Project Gradle version is compatible
        GradleUtils.checkGradleVersionSupported(target.getGradle());
        // Assert Project Java version is compatible
        String javaVersion = JavaVersion.current().toString();
        if (!new Version(javaVersion).isAtLeast(Const.Versions.MIN_JAVA_VERSION)) {
            throw new GradleException("Java version 15 or higher is required. (version = " + javaVersion + ")");
        }
    }

    private void addRequiredDependencies(Project target) {
        log.info("[OIS] Adding required dependencies");
        target.getDependencies().add("implementation", "org.ois:open-interactive-simulation-core:" + Const.Versions.OIS_CORE_VERSION);
    }

    private void addPluginTasks(Project target) {
        log.info("[OIS] Adding plugin tasks");
        TaskUtils.addCleanTask(target);
        TaskProvider<ValidateProjectTask> validationTask = TaskUtils.addProjectValidationTask(target);
        TaskProvider<PrepareSimulationTask> prepareSimulationTask = TaskUtils.addPrepareSimulationTask(target, validationTask);

        TaskUtils.addRunHtmlSimulationTask(target, prepareSimulationTask);
        TaskUtils.addDistributeSimulationTask(target, prepareSimulationTask);
    }
}
