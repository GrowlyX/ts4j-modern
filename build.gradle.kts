import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.liftgate.robotics.ts4j"
version = "2.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.apisense:rhino-android:1.2.0")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly(fileTree("ftc-libs"))
    implementation("commons-io:commons-io:2.4")
    compileOnly("com.google.code.gson:gson:2.10.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.fork()
        options.encoding = "UTF-8"
    }

    withType<ShadowJar> {
        archiveClassifier.set("")
        archiveFileName.set(
            "ts4j-${project.name}.jar"
        )
    }
}

publishing {
    repositories {
        configureLiftgateRepository()
    }

    publications {
        register(
            name = "mavenJava",
            type = MavenPublication::class,
            configurationAction = shadow::component
        )
    }
}

fun RepositoryHandler.configureLiftgateRepository()
{
    val contextUrl = runCatching {
        property("liftgate_artifactory_contextUrl")
    }.getOrNull() ?: run {
        println("Skipping Artifactory configuration.")
        return
    }

    maven("$contextUrl/opensource") {
        name = "liftgate"

        credentials {
            username = property("liftgate_artifactory_user").toString()
            password = property("liftgate_artifactory_password").toString()
        }
    }
}
