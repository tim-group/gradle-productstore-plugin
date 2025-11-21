package com.timgroup.gradle.productstore

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class DescriptionsTest extends Specification {
    @TempDir
    Path testProjectDir

    def "tasks show up in 'gradle tasks' output"() {
        given:
        testProjectDir.resolve('build.gradle') << """
import com.timgroup.gradle.productstore.ProductStorePublication

plugins {
  id 'java-library'
  id 'com.timgroup.productstore'
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
                .withArguments("tasks")
                .withPluginClasspath()
                .build()

        then:
        result.task(":tasks").outcome == TaskOutcome.SUCCESS
        result.output.contains("publishToProductStore")
    }
}
