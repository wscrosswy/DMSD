
/**
 * https://guides.gradle.org/creating-multi-project-builds/
 */

plugins {
    kotlin("jvm") version "1.3.21"
    kotlin("kapt") version "1.3.21"

    id("org.asciidoctor.convert") version "1.5.6" apply false
}

allprojects {
    repositories {
        jcenter()
        mavenLocal()
        maven {
            url = uri("https://nexus.isis.vanderbilt.edu/repository/maven-public/")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }

}


subprojects {
    version = "2019.02"
    group = "edu.vu.isis.bns"
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "5.3.1"
}

