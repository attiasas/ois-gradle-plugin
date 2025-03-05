package org.ois.plugin.tools;

import org.gradle.api.file.FileCollection;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassImplementationFinder {
    private static final Logger log = LoggerFactory.getLogger(ClassImplementationFinder.class);

    public static Set<String> find(Path projectBuildDirPath, FileCollection classpath, String targetClass) {

        File classesDir = projectBuildDirPath.resolve("classes").resolve( "java").resolve( "main").toFile();

        Map<String, ClassNode> classNodeCache = new HashMap<>();
        Set<String> implementations = new HashSet<>();

        // Load all class files from project and dependencies
        if (classesDir.exists()) {
            loadAllClassesFromDirectory(classesDir, classNodeCache);
        }
        loadAllClassesFromJars(classpath, classNodeCache);

        // Scan the loaded class nodes
        scanProjectClasses(targetClass, classesDir, classNodeCache, implementations);

        // Print results
        log.info(String.format("Classes implementing/extending '%s':", targetClass));
        implementations.forEach(log::info);

        return implementations;
    }

    public static Set<String> findImplementations(String targetClass, File classesDir, FileCollection classpath) {
        Map<String, ClassNode> classNodeCache = new HashMap<>();
        Set<String> implementations = new HashSet<>();

        // Load all class files from project and dependencies
        if (classesDir.exists()) {
            loadAllClassesFromDirectory(classesDir, classNodeCache);
        }
        loadAllClassesFromJars(classpath, classNodeCache);

        // Scan the loaded class nodes
        scanProjectClasses(targetClass, classesDir, classNodeCache, implementations);
        return implementations;
    }

    private static void loadAllClassesFromDirectory(File dir, Map<String, ClassNode> classNodeCache) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadAllClassesFromDirectory(file, classNodeCache);
            } else if (file.getName().endsWith(".class")) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ClassReader reader = new ClassReader(fis);
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    classNodeCache.put(classNode.name.replace("/", "."), classNode);
                } catch (Exception e) {
                    System.err.println("Failed to read class: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void loadAllClassesFromJars(FileCollection classpath, Map<String, ClassNode> classNodeCache) {
        for (File jarFile : classpath.getFiles()) {
            if (jarFile.getName().endsWith(".jar")) {
                try (JarFile jar = new JarFile(jarFile)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".class")) {
                            try (InputStream is = jar.getInputStream(entry)) {
                                ClassReader reader = new ClassReader(is);
                                ClassNode classNode = new ClassNode();
                                reader.accept(classNode, 0);
                                classNodeCache.put(classNode.name.replace("/", "."), classNode);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read JAR: " + jarFile.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    private static void scanProjectClasses(String targetClass, File classesDir, Map<String, ClassNode> classNodeCache, Set<String> implementations) {
        scanClasses(targetClass, classNodeCache, implementations, classesDir);
    }

    private static void scanClasses(String targetClass, Map<String, ClassNode> classNodeCache, Set<String> implementations, File classesDir) {
        for (Map.Entry<String, ClassNode> entry : classNodeCache.entrySet()) {
            String className = entry.getKey();
            ClassNode classNode = entry.getValue();

            if (isProjectClass(className, classesDir) && isSubclassOrImplements(classNode, targetClass, classNodeCache)) {
                implementations.add(className);
            }
        }
    }

    private static boolean isProjectClass(String className, File classesDir) {
        File classFile = new File(classesDir, className.replace(".", "/") + ".class");
        return classFile.exists();
    }

    private static boolean isSubclassOrImplements(ClassNode classNode, String targetClass, Map<String, ClassNode> classNodeCache) {
        if (classNode == null) return false;

        // Check superclass recursively
        if (classNode.superName != null) {
            String superName = classNode.superName.replace("/", ".");
            if (superName.equals(targetClass) || isSubclassOrImplements(classNodeCache.get(superName), targetClass, classNodeCache)) {
                return true;
            }
        }

        // Check interfaces recursively
        if (classNode.interfaces != null) {
            for (String iface : classNode.interfaces) {
                String ifaceName = iface.replace("/", ".");
                if (ifaceName.equals(targetClass) || isSubclassOrImplements(classNodeCache.get(ifaceName), targetClass, classNodeCache)) {
                    return true;
                }
            }
        }

        return false;
    }
}
