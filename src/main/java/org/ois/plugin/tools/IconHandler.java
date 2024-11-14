package org.ois.plugin.tools;

import org.ois.core.utils.io.FileUtils;
import org.ois.plugin.utils.AndroidUtils;
import org.ois.plugin.utils.DesktopUtils;
import org.ois.plugin.utils.SimulationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IconHandler {
    private static final Logger log = LoggerFactory.getLogger(IconHandler.class);
    // Loader for icons from the plugin resource directory
    private static final ClassLoader pluginResourceLoader = Thread.currentThread().getContextClassLoader();

    public enum DesktopIconExtension {
        png /*windows*/, ico /*linux*/, icns /*mac*/
    }

    private static final Map<Integer,String> androidIconToDirMap = Map.of(
            192, "xxxhdpi",
            144,     "xxhdpi",
            96,     "xhdpi",
            72,     "hdpi",
            48,     "mdpi"
    );

    // icon<number>.png
    private static final Pattern androidIconNamePattern = Pattern.compile("icon(\\d+)\\.png");

    public static void copyDefaultIcons(SimulationUtils.SimulationRunner runner, Path target) throws IOException {
        copyDefaultDesktopIcons(target);
        copyDefaultAndroidIcons(runner.getAndroidRunnerDirectory().resolve("res"));
    }

    public static void copyDefaultDesktopIcons(Path target) throws IOException {
        FileUtils.createDirIfNotExists(target, true);
        for (String icon : DesktopUtils.icons) {
            copyDefaultDesktopIcon(icon, target.resolve(icon));
        }
    }

    private static void copyDefaultDesktopIcon(String icon, Path target) throws IOException {
        String source = "icons" + "/" + icon;
        log.info("Copy desktop icon: {} to {}", source, target);
        try (InputStream in = pluginResourceLoader.getResourceAsStream(source)) {
            FileUtils.copyFile(in, target, true, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void copyDefaultAndroidIcons(Path target) throws IOException {
        FileUtils.createDirIfNotExists(target, true);
        for (String icon : AndroidUtils.icons) {
            String source = "icons" + "/" + icon;
            Matcher matcher = androidIconNamePattern.matcher(icon);
            if (matcher.find()) {
                int iconSize = Integer.parseInt(matcher.group(1));
                if (!androidIconToDirMap.containsKey(iconSize)) {
                    log.warn("Icon with dim {}x{} should not exists in android", iconSize, iconSize);
                    continue;
                }
                Path targetDir = target.resolve("drawable-" + androidIconToDirMap.get(iconSize));
                FileUtils.createDirIfNotExists(targetDir, true);
                log.info("Copy android icon: {} to {}", source, targetDir);
                try (InputStream in = pluginResourceLoader.getResourceAsStream(source)) {
                    FileUtils.copyFile(in, targetDir.resolve("ic_launcher.png"), true, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static void copyCustomDesktopIcons(Path sourceIconDir, Path target) throws IOException {
        FileUtils.copyDirectoryContent(sourceIconDir, target);
    }
}
