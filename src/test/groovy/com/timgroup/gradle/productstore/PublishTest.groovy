package com.timgroup.gradle.productstore

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path
import java.util.regex.Pattern

class PublishTest extends Specification {
    @TempDir
    Path testProjectDir
    @TempDir
    Path testTargetDir

    def "output of jar task copied to target directory"() {
        given:
        testProjectDir.resolve('build.gradle') << """
import com.timgroup.gradle.productstore.ProductStorePublication

plugins {
  id 'java-library'
  id 'com.timgroup.productstore'
}

group = "com.example"
version = "0.0.0"

productstore {
  host = "localhost"
  path = testTargetDir
}

publishing {
  publications {
    productStore(ProductStorePublication) {
      application = "test-app"
      artifact jar
    }
  }
}
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("-PtestTargetDir=" + testTargetDir, "publish")
                .withPluginClasspath()
                .build()

        then:
        result.task(":jar").outcome == TaskOutcome.SUCCESS
        result.task(":publishToProductStore").outcome == TaskOutcome.SUCCESS
        result.output.find(Pattern.compile("Copy [^ ]+-0.0.0.jar to [^ ]+/test-app/test-app-0.0.0.jar"))
        testTargetDir.resolve("test-app").isDirectory()
        testTargetDir.resolve("test-app").resolve("test-app-0.0.0.jar").isFile()
    }

    def "arbitrary file copied to target directory"() {
        given:
        testProjectDir.resolve('build.gradle') << """
import com.timgroup.gradle.productstore.ProductStorePublication

plugins {
  id 'java-library'
  id 'com.timgroup.productstore'
}

group = "com.example"
version = "0.0.0"

task buildTestFile {
  outputs.file "build/file.jar"
  doLast {
    file("build").mkdirs()
    file("build/file.jar") << "test file"
  }
}

productstore {
  host = "localhost"
  path = testTargetDir
}

publishing {
  publications {
    productStore(ProductStorePublication) {
      application = "test-app"
      artifact("build/file.jar") {
        builtBy(buildTestFile)
      }
    }
  }
}
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments("-PtestTargetDir=" + testTargetDir, "publish")
                .withPluginClasspath()
                .build()

        then:
        result.task(":buildTestFile").outcome == TaskOutcome.SUCCESS
        result.task(":publishToProductStore").outcome == TaskOutcome.SUCCESS
        result.output.find(Pattern.compile("Copy [^ ]+/build/file.jar to [^ ]+/test-app/test-app-0.0.0.jar"))
        testTargetDir.resolve("test-app").isDirectory()
        testTargetDir.resolve("test-app").resolve("test-app-0.0.0.jar").isFile()
    }
}
