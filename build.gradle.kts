plugins {
    `java-gradle-plugin`
    groovy
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
    id("com.adarshr.test-logger") version "4.0.0"
}

val buildNumber = providers.environmentVariable("ORIGINAL_BUILD_NUMBER")
    .orElse(providers.environmentVariable("BUILD_NUMBER"))
val githubUrl by extra("https://github.com/tim-group/gradle-productstore-plugin")

group = "com.timgroup"
if (buildNumber.isPresent) version = "1.0.${buildNumber.get()}"
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

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.spockframework:spock-core:2.4-groovy-3.0") {
        exclude(module = "groovy")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
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

val nexusRepoUrl = providers.gradleProperty("repoUrl")
val nexusRepoUsername = providers.gradleProperty("repoUsername")
val nexusRepoPassword = providers.gradleProperty("repoPassword")
val codeartifactUrl = providers.environmentVariable("CODEARTIFACT_URL")
    .orElse(providers.gradleProperty("codeartifact.url"))
    .orElse("https://timgroup-148217964156.d.codeartifact.eu-west-1.amazonaws.com/maven/jars/")
val codeartifactToken = providers.environmentVariable("CODEARTIFACT_TOKEN")
    .orElse(providers.gradleProperty("codeartifact.token"))

publishing {
    repositories {
        if (nexusRepoUrl.isPresent && nexusRepoUsername.isPresent && nexusRepoPassword.isPresent) {
            maven("${nexusRepoUrl.get()}/repositories/yd-release-candidates") {
                name = "nexus"
                credentials {
                    username = nexusRepoUsername.get()
                    password = nexusRepoPassword.get()
                }
                isAllowInsecureProtocol = true
            }
        }
        if (codeartifactUrl.isPresent && codeartifactToken.isPresent) {
            maven(url = codeartifactUrl.get()) {
                name = "codeartifact"
                credentials {
                    username = "aws"
                    password = codeartifactToken.get()
                }
            }
        }
    }
}
