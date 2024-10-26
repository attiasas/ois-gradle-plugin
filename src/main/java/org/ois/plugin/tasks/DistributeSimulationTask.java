package org.ois.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.ois.core.project.SimulationManifest;
import org.ois.core.runner.RunnerConfiguration;
import org.ois.core.utils.io.FileUtils;
import org.ois.core.utils.io.ZipUtils;
import org.ois.core.utils.io.data.formats.JsonFormat;
import org.ois.plugin.PluginConfiguration;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
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
        Path distributionDirPath = PluginConfiguration.getCustomExportDirPath(getProject());
        if (distributionDirPath == null) {
            distributionDirPath = SimulationUtils.getSimulationDistributionDirectory(getProject());
            if (FileUtils.createDirIfNotExists(distributionDirPath, true)) {
                log.info("Created ois 'distribution' directory");
            }
        } else {
            log.info("Using custom export directory {}", distributionDirPath);
        }
        SimulationManifest manifest = SimulationUtils.getSimulationManifest(getProject());
        Set<RunnerConfiguration.RunnerType> platforms = manifest.getPlatforms();
        if (platforms.contains(RunnerConfiguration.RunnerType.Html)) {
            log.info("Exporting HTML artifacts");
            generateHtmlArtifacts(manifest, distributionDirPath);
        }
    }

    /**
     * Generate the HTML simulation artifacts in the given directory (it will create a dir named {@link RunnerConfiguration.RunnerType#Html})
     * @param distributionDirPath - the directory to generate
     * @throws IOException - in case of errors in generation
     */
    public void generateHtmlArtifacts(SimulationManifest manifest, Path distributionDirPath) throws IOException {
        Path htmlDistDirPath = distributionDirPath.resolve(RunnerConfiguration.RunnerType.Html.name());
        if (FileUtils.createDirIfNotExists(htmlDistDirPath, true)) {
            log.info("Created html distribution directory");
        }
        SimulationUtils.distributeSimulation(getProject(), RunnerConfiguration.RunnerType.Html, SimulationUtils.getDistributeSimulationTaskEnvVariables(getProject()));
        log.info("[HTML] Collect artifacts...");
        ZipUtils.zipItems(htmlDistDirPath.resolve(manifest.getTitle() + ".zip"), getHtmlFilesToZip());
        log.info("[HTML] Artifacts generated successfully");
    }

    /**
     * Get the list of artifacts to zip for HTML distribution
     * @return - list of files and directories to zip
     */
    private Path[] getHtmlFilesToZip() {
        Path webappDir = SimulationUtils.getRunner(getProject()).getHtmlRunnerDirectory().resolve("build").resolve("dist").resolve("webapp");
        File[] files = webappDir.toFile().listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("[HTML] Can't find any artifacts to zip");
        }
        Path[] filePaths = Arrays.stream(files).map(File::toPath).toArray(Path[]::new);
        log.debug("files to zip: {}", Arrays.toString(filePaths));
        return filePaths;
    }
}
