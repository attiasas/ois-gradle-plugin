package org.ois.plugin.utils;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.TaskProvider;
import org.ois.plugin.Const;
import org.ois.plugin.tasks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task Utilities
 */
public class TaskUtils {
    private static final Logger log = LoggerFactory.getLogger(TaskUtils.class);

    /**
     * Register a task to the project if not already registered.
     * @param project - the project to register the task to
     */
    private static <T extends Task> TaskProvider<T> registerTaskInProject(String taskName, Class<T> taskClass, String taskDescription, Project project) {
        log.info("[OIS] Registering '{}' task to the project", taskName);
        return project.getTasks().register(taskName, taskClass, task -> {
            task.setDescription(taskDescription);
            task.setGroup(Const.PLUGIN_GROUP_NAME);
        });
    }

    /**
     * Register OIS-Project-Validation task to the project if not already registered.
     * Checks if the project is a valid OIS project and output issues if exists
     * @param project - the project to register the task to
     * @return validation task that was registered
     */
    public static TaskProvider<ValidateProjectTask> addProjectValidationTask(Project project) {
        try {
            return project.getTasks().named(Const.Tasks.VALIDATE_PROJECT_TASK_NAME, ValidateProjectTask.class);
        } catch (UnknownTaskException ignored) {}
        return registerTaskInProject(Const.Tasks.VALIDATE_PROJECT_TASK_NAME, ValidateProjectTask.class, Const.Tasks.VALIDATE_PROJECT_TASK_DESCRIPTION, project);
    }

    /**
     * Register OIS-Prepare-Simulation task to the project if not already registered.
     * Prepare the environment required for the OIS simulation actions
     * @param project - the project to register the task to
     * @param validationTask - the validation task that is required before the task
     * @return the task provider of the task
     */
    public static TaskProvider<PrepareSimulationTask> addPrepareSimulationTask(Project project, TaskProvider<ValidateProjectTask> validationTask) {
        try {
            return project.getTasks().named(Const.Tasks.PREPARE_SIMULATION_TASK_NAME, PrepareSimulationTask.class);
        } catch (UnknownTaskException ignored) {}
        TaskProvider<PrepareSimulationTask> task = registerTaskInProject(Const.Tasks.PREPARE_SIMULATION_TASK_NAME, PrepareSimulationTask.class, Const.Tasks.PREPARE_SIMULATION_TASK_DESCRIPTION, project);
        task.configure(prepareSimulationTask -> {
            prepareSimulationTask.dependsOn(validationTask);
            prepareSimulationTask.dependsOn(project.getTasks().named("build"));
        });
        return task;
    }

    /**
     * Register OIS-run-html-simulation task to the project if not already registered.
     * Prepare the environment required for the OIS simulation actions
     * @param project - the project to register the task to
     * @param prepareSimulationTask- the required task before this
     */
    public static void addRunHtmlSimulationTask(Project project, TaskProvider<PrepareSimulationTask> prepareSimulationTask) {
        try {
            project.getTasks().named(Const.Tasks.RUN_HTML_SIMULATION_TASK_NAME, RunHtmlSimulationTask.class);
            return;
        } catch (UnknownTaskException ignored) {}
        TaskProvider<RunHtmlSimulationTask> task = registerTaskInProject(Const.Tasks.RUN_HTML_SIMULATION_TASK_NAME, RunHtmlSimulationTask.class, Const.Tasks.RUN_HTML_SIMULATION_TASK_DESCRIPTION, project);
        task.configure(runSimulationTask -> runSimulationTask.dependsOn(prepareSimulationTask));
    }

    /**
     * Register OIS-run-desktop-simulation task to the project if not already registered.
     * Prepare the environment required for the OIS simulation actions
     * @param project - the project to register the task to
     * @param prepareSimulationTask- the required task before this
     */
    public static void addRunDesktopSimulationTask(Project project, TaskProvider<PrepareSimulationTask> prepareSimulationTask) {
        try {
            project.getTasks().named(Const.Tasks.RUN_DESKTOP_SIMULATION_TASK_NAME, RunDesktopSimulationTask.class);
            return;
        } catch (UnknownTaskException ignored) {}
        TaskProvider<RunDesktopSimulationTask> task = registerTaskInProject(Const.Tasks.RUN_DESKTOP_SIMULATION_TASK_NAME, RunDesktopSimulationTask.class, Const.Tasks.RUN_DESKTOP_SIMULATION_TASK_DESCRIPTION, project);
        task.configure(runSimulationTask -> runSimulationTask.dependsOn(prepareSimulationTask));
    }

    /**
     * Register Distribute-Simulation task to the project if not already registered.
     * Generate the production artifacts for each configured platforms, ready to distribute.
     * @param project - the project to register the task to
     * @param prepareSimulationTask - the required task before this
     */
    public static void addDistributeSimulationTask(Project project, TaskProvider<PrepareSimulationTask> prepareSimulationTask) {
        try {
            project.getTasks().named(Const.Tasks.DISTRIBUTE_SIMULATION_TASK_NAME, DistributeSimulationTask.class);
            return;
        } catch (UnknownTaskException ignored) {}
        TaskProvider<DistributeSimulationTask> task = registerTaskInProject(Const.Tasks.DISTRIBUTE_SIMULATION_TASK_NAME, DistributeSimulationTask.class, Const.Tasks.DISTRIBUTE_SIMULATION_TASK_DESCRIPTION, project);
        task.configure(distributeTask -> distributeTask.dependsOn(prepareSimulationTask));
    }

    /**
     * Register Clean-Cache task to the project if not already registered.
     * Delete ois plugin caches
     * @param project - the project to register the task to
     */
    public static void addCleanTask(Project project) {
        try {
            project.getTasks().named(Const.Tasks.CLEAN_CACHES_TASK_NAME, CleanCachesTask.class);
            return;
        } catch (UnknownTaskException ignored) {}
        registerTaskInProject(Const.Tasks.CLEAN_CACHES_TASK_NAME, CleanCachesTask.class, Const.Tasks.CLEAN_CACHES_TASK_DESCRIPTION, project);
    }
}
