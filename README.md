# ois-gradle-plugin

## Table Of Content

- [📦 Installation](#-installation)

## 📦 Installation

Clone project to your local machine:
```bash
git clone https://github.com/attiasas/ois-gradle-plugin.git
```
Move to the cloned directory and publish it to your local maven repository:
```bash
./gradlew publishToMavenLocal
```

Add the following to your `build.gradle` file:
```groovy
plugins {
    id 'org.ois.simulation' version '1.0-SNAPSHOT'
}
```
Add the following to your `settings.gradle` file (Plugin is not public available):
```groovy
pluginManagement {
    repositories {
        mavenLocal()
    }
}
```