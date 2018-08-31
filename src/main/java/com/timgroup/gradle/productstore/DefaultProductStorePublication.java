package com.timgroup.gradle.productstore;

import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.publish.internal.PublicationArtifactSet;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.Factory;
import org.gradle.internal.reflect.Instantiator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class DefaultProductStorePublication implements ProductStorePublicationInternal {
    private final String name;
    private final ImmutableAttributesFactory immutableAttributesFactory;
    private final DefaultProductStoreArtifactSet artifacts;

    private String application;
    private String version;
    private boolean alias;

    DefaultProductStorePublication(String name, String moduleVersion,
                                   ImmutableAttributesFactory immutableAttributesFactory,
                                   Instantiator instantiator,
                                   FileCollectionFactory fileCollectionFactory) {
        this.name = name;
        this.version = moduleVersion;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.artifacts = instantiator.newInstance(DefaultProductStoreArtifactSet.class, name, fileCollectionFactory);
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean isAlias() {
        return alias;
    }

    @Override
    public void setAlias(boolean alias) {
        this.alias = alias;
    }

    @Override
    public ModuleVersionIdentifier getCoordinates() {
        return DefaultModuleVersionIdentifier.newId("com.timgroup.gradle.productstore.ARTIFACTS", getApplication(), getApplicationVersion());
    }

    @Override
    public void artifact(Object source) {
        artifacts.artifact(source);
    }

    @Override
    public void artifact(Object source, Action<? super ProductStoreArtifact> config) {
        artifacts.artifact(source, config);
    }

    @Nullable
    @Override
    public <T> T getCoordinates(Class<T> type) {
        if (type.isAssignableFrom(ModuleVersionIdentifier.class)) {
            return type.cast(getCoordinates());
        }
        return null;
    }

    @Override
    public ImmutableAttributes getAttributes() {
        return immutableAttributesFactory.of(ProjectInternal.STATUS_ATTRIBUTE, version == null ? "snapshot" : "release");
    }

    @Override
    public DisplayName getDisplayName() {
        return Describables.withTypeAndName("ProductStore publication", name);
    }

    @Override
    public boolean isLegacy() {
        return false;
    }

    @Override
    public PublishedFile getPublishedFile(PublishArtifact source) {
        return new PublishedFile() {
            @Override
            public String getName() {
                return getDestLeafname();
            }

            @Override
            public String getUri() {
                return getDestFile();
            }
        };
    }

    @Nullable
    @Override
    public SoftwareComponentInternal getComponent() {
        return null;
    }

    @Override
    public File getArtifactFile() {
        return artifacts.getArtifactFile();
    }

    @Override
    public String getApplicationVersion() {
        if (version != null)
            return version;
        throw new InvalidUserDataException("No version or module version specified for productstore publication");
    }

    public FileCollection getPublishableFiles() {
        return artifacts.getFiles();
    }

    private String getDestLeafname() {
        return application + "-" + getApplicationVersion() + ".jar";
    }

    @Override
    public String getDestFile() {
        return application + "/" + getDestLeafname();
    }

    @Override
    public ProductStoreArtifactSet getArtifacts() {
        return artifacts;
    }

    @Override
    public PublicationArtifactSet<ProductStoreArtifact> getPublishableArtifacts() {
        return artifacts;
    }

    @Override
    public void allPublishableArtifacts(Action<? super ProductStoreArtifact> action) {
        artifacts.all(action);
    }

    @Override
    public void whenPublishableArtifactRemoved(Action<? super ProductStoreArtifact> action) {
        artifacts.whenObjectRemoved(action);
    }

    @Override
    public ProductStoreArtifact addDerivedArtifact(ProductStoreArtifact originalArtifact, Factory<File> file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeDerivedArtifact(ProductStoreArtifact artifact) {
        throw new UnsupportedOperationException();
    }
}
