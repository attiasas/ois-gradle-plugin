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

<details>
<summary> <h2><a href="https://github.com/attiasas/ois-core/wiki">Home</a></h2> </summary>

* [Getting Started](https://github.com/attiasas/ois-core/wiki#getting-started)
* [Developer's Guide](https://github.com/attiasas/ois-core/wiki#developers-guide)

</details>

---

<details>
<summary> <a href="https://github.com/attiasas/ois-core/wiki#getting-started">Getting Started</a> </summary>

* [Setting Up a Dev Environment](https://github.com/attiasas/ois-core/wiki/Setting-Up-a-Dev-Environment)
* [Configure Your Simulation Project](https://github.com/attiasas/ois-core/wiki/Configure-Your-Simulation-Project)
* [Running and Debugging the Simulation](https://github.com/attiasas/ois-core/wiki/Running-and-Debugging-the-Simulation)
* [Exporting the Simulation](https://github.com/attiasas/ois-core/wiki/Exporting-the-Simulation)

</details>

---

<details>
<summary> <a href="https://github.com/attiasas/ois-core/wiki#developers-guide">Developer's Guide</a> </summary>

* [Managing States](https://github.com/attiasas/ois-core/wiki/Managing-States)
* [Access Simulation Assets](https://github.com/attiasas/ois-core/wiki/Access-Simulation-Assets)

</details>

c