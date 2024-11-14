package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.project.SimulationManifest;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.core.utils.io.FileUtils;
import org.ois.core.utils.io.ZipUtils;
import org.ois.plugin.PluginConfiguration;
import org.ois.plugin.utils.AndroidUtils;
import org.ois.plugin.utils.DesktopUtils;
import org.ois.plugin.utils.HtmlUtils;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Generate the production artifacts of the project simulation for each of the configured platforms, ready for distribution.
 */
public class DistributeSimulationTask extends DefaultTask {
    private static final Logger log = LoggerFactory.getLogger(DistributeSimulationTask.class);

    /**
     * Generate the project simulation production artifacts for each of the configured platforms, ready to distribute
     * @throws IOException - in case of errors in generation
     */
    @TaskAction
    public void generateProductionArtifacts() throws IOException {
        log.info("Generating distribution artifacts");

        Path distributionDirPath = getDistributionDirectory();
        SimulationManifest manifest = SimulationUtils.getSimulationManifest(getProject());

        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Html)) {
            log.info("Exporting HTML artifacts");
            generateHtmlArtifacts(manifest, distributionDirPath);
        }
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Desktop)) {
            log.info("Exporting Desktop artifacts");
            generateDesktopArtifacts(manifest, distributionDirPath);
        }
        if (manifest.getPlatforms().contains(RunnerConfiguration.RunnerType.Android)) {
            log.info("Exporting Android artifacts");
            generateAndroidArtifacts(manifest, distributionDirPath);
        }
        log.info("Simulation exported successfully");
    }

    /**
     * Get the distribution directory to export the artifacts to
     * @return a path to the directory to export the artifacts to
     */
    private Path getDistributionDirectory() {
        Path distributionDirPath = PluginConfiguration.getCustomExportDirPath(getProject());
        if (distributionDirPath == null) {
            distributionDirPath = SimulationUtils.getSimulationDistributionDirectory(getProject());
        } else {
            log.info("Using custom export directory {}", distributionDirPath);
        }
        if (FileUtils.createDirIfNotExists(distributionDirPath, true)) {
            log.debug("Created ois 'distribution' directory");
        }
        return distributionDirPath;
    }

    /**
     * Generate the Desktop simulation artifacts in the given directory (it will create a dir named {@link RunnerConfiguration.RunnerType#Desktop})
     * @param distributionDirPath - the directory to generate
     * @throws IOException - in case of errors in generation
     */
    public void generateDesktopArtifacts(SimulationManifest manifest, Path distributionDirPath) throws IOException {
        Path desktopDistDirPath = distributionDirPath.resolve(RunnerConfiguration.RunnerType.Desktop.name());
        if (FileUtils.createDirIfNotExists(desktopDistDirPath, true)) {
            log.debug("Created Desktop distribution directory");
        }
        SimulationUtils.distributeSimulation(getProject(), RunnerConfiguration.RunnerType.Desktop, SimulationUtils.getDistributeSimulationTaskEnvVariables(manifest, getProject()));
        log.info("[Desktop] Collect artifacts...");
        // Copy jar
        FileUtils.copyDirectoryContent(SimulationUtils.getRunner(getProject()).getDesktopRunnerDirectory().resolve("build").resolve("libs"), distributionDirPath);
        // Zip application
        ZipUtils.zipItems(desktopDistDirPath.resolve(manifest.getTitle() + ".zip"), DesktopUtils.getDesktopFilesToZip(getProject()));
        log.info("[Desktop] Artifacts generated successfully at {}", desktopDistDirPath);
    }

    /**
     * Generate the HTML simulation artifacts in the given directory (it will create a dir named {@link RunnerConfiguration.RunnerType#Html})
     * @param distributionDirPath - the directory to generate
     * @throws IOException - in case of errors in generation
     */
    public void generateHtmlArtifacts(SimulationManifest manifest, Path distributionDirPath) throws IOException {
        Path htmlDistDirPath = distributionDirPath.resolve(RunnerConfiguration.RunnerType.Html.name());
        if (FileUtils.createDirIfNotExists(htmlDistDirPath, true)) {
            log.debug("Created Html distribution directory");
        }
        SimulationUtils.distributeSimulation(getProject(), RunnerConfiguration.RunnerType.Html, SimulationUtils.getDistributeSimulationTaskEnvVariables(manifest, getProject()));
        log.info("[HTML] Collect artifacts...");
        ZipUtils.zipItems(htmlDistDirPath.resolve(manifest.getTitle() + ".zip"), HtmlUtils.getHtmlFilesToZip(getProject()));
        log.info("[HTML] Artifacts generated successfully at {}", htmlDistDirPath);
    }

    public void generateAndroidArtifacts(SimulationManifest manifest, Path distributionDirPath) throws IOException {
        Path androidDistDirPath = distributionDirPath.resolve(RunnerConfiguration.RunnerType.Android.name());
        if (FileUtils.createDirIfNotExists(androidDistDirPath, true)) {
            log.debug("Created Android distribution directory");
        }
        SimulationUtils.distributeSimulation(getProject(), RunnerConfiguration.RunnerType.Android, SimulationUtils.getDistributeSimulationTaskEnvVariables(manifest, getProject()));
        log.info("[Android] Collection artifacts...");
        ZipUtils.zipItems(androidDistDirPath.resolve(manifest.getTitle() + ".zip"), AndroidUtils.getAndroidFilesToZip(getProject()));
        log.info("[Android] Artifacts generated successfully at {}", androidDistDirPath);
    }
}
