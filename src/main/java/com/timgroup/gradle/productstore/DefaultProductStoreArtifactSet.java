package com.timgroup.gradle.productstore;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.DefaultDomainObjectSet;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.collections.MinimalFileSet;
import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.publish.internal.PublicationArtifactSet;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultProductStoreArtifactSet extends DefaultDomainObjectSet<ProductStoreArtifact> implements ProductStoreArtifactSet, PublicationArtifactSet<ProductStoreArtifact> {
    private final TaskDependencyInternal builtBy = new ArtifactsTaskDependency();
    private final String publicationName;
    private final FileCollection files;

    public DefaultProductStoreArtifactSet(String publicationName, FileCollectionFactory fileCollectionFactory) {
        super(ProductStoreArtifact.class);
        this.publicationName = publicationName;
        this.files = fileCollectionFactory.create(builtBy, new ArtifactsFileCollection());
    }

    @Override
    public FileCollection getFiles() {
        return files;
    }

    @Override
    public ProductStoreArtifact artifact(Object source) {
        if (source instanceof AbstractArchiveTask) {
            ArchiveTaskBasedProductStoreArtifact artifact = new ArchiveTaskBasedProductStoreArtifact((AbstractArchiveTask) source);
            add(artifact);
            return artifact;
        }
        else {
            throw new IllegalArgumentException("Unhandled artifact for product store publication: " + source);
        }
    }

    @Override
    public ProductStoreArtifact artifact(Object source, Action<? super ProductStoreArtifact> config) {
        ProductStoreArtifact artifact = artifact(source);
        config.execute(artifact);
        return artifact;
    }

    @Override
    public File getArtifactFile() {
        if (isEmpty()) {
            throw new IllegalStateException("No artifact specified to get file location of");
        }
        return iterator().next().getFile();
    }

    private final class ArtifactsFileCollection implements MinimalFileSet {
        @Override
        public String getDisplayName() {
            return "artifacts for ProductStore publication '" + publicationName + "'";
        }

        @Override
        @Nonnull
        public Set<File> getFiles() {
            Set<File> files = new LinkedHashSet<>();
            for (ProductStoreArtifact artifact : DefaultProductStoreArtifactSet.this) {
                files.add(artifact.getFile());
            }
            return files;
        }
    }

    private final class ArtifactsTaskDependency extends AbstractTaskDependency {
        @Override
        public void visitDependencies(TaskDependencyResolveContext context) {
            for (ProductStoreArtifact artifact : DefaultProductStoreArtifactSet.this) {
                context.add(artifact);
            }
        }
    }
}
