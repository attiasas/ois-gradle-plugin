package org.ois.plugin.tools;

import org.ois.core.utils.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class IconHandler {
    private static final Logger log = LoggerFactory.getLogger(IconHandler.class);
    // Loader for icons from the plugin resource directory
    private static final ClassLoader pluginResourceLoader = Thread.currentThread().getContextClassLoader();

    private static final List<String> icons = List.of(
            "icon32.png", "icon32.ico", "icon32.icns",
            "icon128.png", "icon128.ico", "icon128.icns",
            "logo.png"
    );

    public static void copyDefaultIcons(Path target) throws IOException {
        FileUtils.createDirIfNotExists(target, true);
        for (String icon : icons) {
            String source = "icons" + "/" + icon;
            log.debug("Copy: {} to {}", source, target.resolve(icon));
            try (InputStream in = pluginResourceLoader.getResourceAsStream(source)) {
                FileUtils.copyFile(in, target.resolve(icon) , false);
            }
        }
    }

}
