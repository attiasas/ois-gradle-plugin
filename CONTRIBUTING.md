# ⚒️ Building the library

To build the library sources, please follow these steps:

1. Clone the https://github.com/attiasas/ois-core code from Git.
2. Build the library sources and publish it locally, run the following Gradle command:
    <details open>
    <summary>windows</summary>
    
    ```bash
    ./gradlew.bat publishToMavenLocal
    ```
    </details>
    <details>
    <summary>Mac / Linux</summary>
    
    ```bash
    ./gradlew publishToMavenLocal
    ```
    </details>
3. Clone the plugin code from Git.
4. Build the plugin sources and publish it locally, run the following Gradle command:
    <details open>
    <summary>windows</summary>

    ```bash
    ./gradlew.bat publishToMavenLocal
    ```
    </details>
    <details>
    <summary>Mac / Linux</summary>

    ```bash
    ./gradlew publishToMavenLocal
    ```
    </details>

---

# 🧪 Testing the plugin

To test the library sources, please follow these steps:
<details open>
<summary>windows</summary>

```bash
./gradlew.bat clean check
```
</details>
<details>
<summary>Mac / Linux</summary>

```bash
./gradlew clean check
```
</details>
