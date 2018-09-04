package com.timgroup.gradle.productstore;

import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.tasks.TaskDependency;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileBasedProductStoreArtifact implements ProductStoreArtifact {
    private final File file;
    private final Set<Object> additionalBuildDependencies = new LinkedHashSet<>();

    public FileBasedProductStoreArtifact(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void builtBy(Object... tasks) {
        additionalBuildDependencies.addAll(Arrays.asList(tasks));
    }

    @Override
    @Nonnull
    public TaskDependency getBuildDependencies() {
        return new AbstractTaskDependency() {
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                for (Object task : additionalBuildDependencies) {
                    context.add(task);
                }
            }
        };
    }
}
