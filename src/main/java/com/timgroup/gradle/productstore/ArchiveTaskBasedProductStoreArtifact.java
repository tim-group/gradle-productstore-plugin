package com.timgroup.gradle.productstore;

import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ArchiveTaskBasedProductStoreArtifact implements ProductStoreArtifact {
    private final AbstractArchiveTask archiveTask;
    private final TaskDependencyInternal buildDependencies;
    private final Set<Object> additionalBuildDependencies = new LinkedHashSet<>();

    ArchiveTaskBasedProductStoreArtifact(AbstractArchiveTask archiveTask) {
        this.archiveTask = archiveTask;
        this.buildDependencies = new AbstractTaskDependency() {
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                context.add(archiveTask);
            }
        };
    }

    @Override
    public File getFile() {
        return archiveTask.getArchivePath();
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
                buildDependencies.visitDependencies(context);
                for (Object task : additionalBuildDependencies) {
                    context.add(task);
                }
            }
        };
    }
}
