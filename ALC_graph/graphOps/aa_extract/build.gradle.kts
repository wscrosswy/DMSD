

plugins {
    java
    application
    maven
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java {
            setSrcDirs( listOf("src/main/java"))
        }
        
        resources {
            setSrcDirs( listOf("src/main/resources"))
        }
    }
}

application {
    // this.applicationDefaultJvmArgs
    mainClassName = "edu.vanderbilt.isis.dmsd.aa.CodeGen"
}


tasks {

    register<JavaExec>("run_webgme_tg") {
        dependsOn(":classes")
        group = "application"
        classpath = sourceSets["main"].runtimeClasspath
        main = "${application.mainClassName}"
        args = listOf("--target", "tg", "--pg-dbase", "webgme_out.json")
    }

    register<Jar>("uberJar") {
        archiveClassifier.set("uber")
        group = "application"
        dependsOn(configurations.runtimeClasspath)
        from(sourceSets.main.get().output)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}


dependencies {

    // https://mvnrepository.com/artifact/org.apache.tinkerpop/gremlin-core
    implementation ( group= "org.apache.tinkerpop", name= "gremlin-core", version= "3.4.0" )
    // https://mvnrepository.com/artifact/org.apache.tinkerpop/tinkergraph-gremlin
    implementation ( group= "org.apache.tinkerpop", name= "tinkergraph-gremlin", version= "3.4.0" )

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation ( group= "com.fasterxml.jackson.core", name= "jackson-core", version= "2.9.8" )

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation ( group= "com.fasterxml.jackson.core", name= "jackson-databind", version= "2.9.8" )


    // https://mvnrepository.com/artifact/com.beust/jcommander
    implementation ( group= "com.beust", name= "jcommander", version= "1.72" )

    // https://mvnrepository.com/artifact/org.apache.commons/commons-configuration2
    implementation ( group= "org.apache.commons", name= "commons-configuration2", version= "2.4" )


    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation ( group= "ch.qos.logback", name= "logback-classic", version= "0.9.26" )

    // https://mvnrepository.com/artifact/org.parboiled/parboiled-java
    implementation ( group= "org.parboiled", name= "parboiled-java", version= "1.3.0" )

    // https://mvnrepository.com/artifact/org.apache.tinkerpop/neo4j-gremlin
    testCompile ( group= "org.apache.tinkerpop", name= "neo4j-gremlin", version= "3.4.0" )

    // implementation fileTree(dir: ‘/opt/orientdb/latest/’, include: ‘*.jar’)
    // https://mvnrepository.com/artifact/com.orientechnologies/orientdb-core
    implementation ( group= "com.orientechnologies", name= "orientdb-core", version= "3.0.16" )

    // https://mvnrepository.com/artifact/com.orientechnologies/orientdb-client
    implementation ( group= "com.orientechnologies", name= "orientdb-client", version= "3.0.16" )

    // https://mvnrepository.com/artifact/com.orientechnologies/orientdb-gremlin
    implementation ( group= "com.orientechnologies", name= "orientdb-gremlin", version= "3.0.16" )


}
