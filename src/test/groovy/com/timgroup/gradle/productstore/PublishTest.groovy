package com.timgroup.gradle.productstore

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern

class PublishTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()
    @Rule public final TemporaryFolder testTargetDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "jar file copied to target directory"() {
        given:
        buildFile << """
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
                .withProjectDir(testProjectDir.root)
                .withArguments("-PtestTargetDir=" + testTargetDir.root, "publish")
                .withPluginClasspath()
                .build()

        then:
        result.task(":publishToProductStore").outcome == TaskOutcome.SUCCESS
        result.output.find(Pattern.compile("Copy [^ ]+-0.0.0.jar to [^ ]+/test-app/test-app-0.0.0.jar"))
        new File(testTargetDir.root, "test-app").isDirectory()
        new File(new File(testTargetDir.root, "test-app"), "test-app-0.0.0.jar").isFile()
    }
}
