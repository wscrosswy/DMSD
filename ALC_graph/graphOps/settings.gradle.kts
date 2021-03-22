/*
 * The settings file is used to specify which projects to include in your build.
 * 
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user guide at https://docs.gradle.org/4.10.2/userguide/multi_project_builds.html
 */

rootProject.name = "webgme_extract"

include ("aa_extract")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    }
}