package com.timgroup.gradle.productstore

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DescriptionsTest extends Specification {
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "tasks show up in 'gradle tasks' output"() {
        given:
        buildFile << """
import com.timgroup.gradle.productstore.ProductStorePublication

plugins {
  id 'java-library'
  id 'com.timgroup.productstore'
}

publishing {
  publications {
    productStore(ProductStorePublication) {
      application = "test-app"
      from components.java
      artifact jar
    }
  }
}
"""

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("tasks")
                .withPluginClasspath()
                .build()

        then:
        result.task(":tasks").outcome == TaskOutcome.SUCCESS
        result.output.contains("publishToProductStore")
    }
}
