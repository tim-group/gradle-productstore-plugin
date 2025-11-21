plugins {
    `java-gradle-plugin`
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
    id("com.timgroup.jarmangit") version "1.1.117"
}

val repoUrl: String? by project
val repoUsername: String? by project
val repoPassword: String? by project

val buildNumber: String? by extra { System.getenv("ORIGINAL_BUILD_NUMBER") ?: System.getenv("BUILD_NUMBER") }
val githubUrl by extra("https://github.com/tim-group/gradle-webpack-plugin")

group = "com.timgroup"
if (buildNumber != null) version = "1.0.$buildNumber"
description = "Publish fat jars to ProductStore"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0") {
        exclude(module = "groovy-all")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    "test"(Test::class) {
        maxParallelForks = 4
    }
}

gradlePlugin {
    plugins {
        create("productstore") {
            id = "com.timgroup.productstore"
            implementationClass = "com.timgroup.gradle.productstore.ProductStorePublishPlugin"
            description = project.description
            displayName = "Publish fat jars to ProductStore"
            tags.add("publish")
        }
    }
}

pluginBundle {
    website = githubUrl
    vcsUrl = githubUrl
    tags = setOf("publication")
}

publishing {
    repositories {
        if (project.hasProperty("repoUrl")) {
            maven("$repoUrl/repositories/yd-release-candidates") {
                name = "nexus"
                credentials {
                    username = repoUsername.toString()
                    password = repoPassword.toString()
                }
                isAllowInsecureProtocol = true
            }
        }
    }
}
