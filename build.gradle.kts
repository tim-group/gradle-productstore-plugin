plugins {
    `java-gradle-plugin`
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.11.0"
    id("com.timgroup.jarmangit") version "1.1.86"
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    testImplementation("junit:junit:4.13")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5") {
        exclude(module = "groovy-all")
    }
}

tasks {
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
                name = "timgroup"
                credentials {
                    username = repoUsername.toString()
                    password = repoPassword.toString()
                }
            }
        }
    }
}
