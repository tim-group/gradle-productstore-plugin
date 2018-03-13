package com.timgroup.gradle.productstore;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.component.SoftwareComponentInternal;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.collections.MinimalFileSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.typeconversion.NotationParser;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Set;

public class DefaultProductStorePublication implements ProductStorePublicationInternal {
    private final String name;
    private final String moduleGroup;
    private final ImmutableAttributesFactory immutableAttributesFactory;
    private final NotationParser<Object, MavenArtifact> artifactNotationParser;
    private final FileCollection files;

    private String application;
    private String version;
    private boolean alias;
    private SoftwareComponentInternal component;
    private MavenArtifact mavenArtifact;

    public DefaultProductStorePublication(String name, String moduleGroup, String moduleVersion,
                                          ImmutableAttributesFactory immutableAttributesFactory, NotationParser<Object, MavenArtifact> artifactNotationParser,
                                          FileCollectionFactory fileCollectionFactory) {
        this.name = name;
        this.moduleGroup = moduleGroup;
        this.version = moduleVersion;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.artifactNotationParser = artifactNotationParser;
        this.files = fileCollectionFactory.create(new AbstractTaskDependency() {
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                if (mavenArtifact != null) {
                    context.add(mavenArtifact);
                }
            }
        }, new MinimalFileSet() {
            @Override
            public String getDisplayName() {
                return "artifacts for ProductStore publication '" + name + "'";
            }

            @Override
            public Set<File> getFiles() {
                if (mavenArtifact == null) {
                    return Collections.emptySet();
                }
                else {
                    return Collections.singleton(mavenArtifact.getFile());
                }
            }
        });
    }

    @Override
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
        return new DefaultModuleVersionIdentifier(moduleGroup, application, getApplicationVersion());
    }

    public void from(SoftwareComponent component) {
        if (this.component != null) {
            throw new InvalidUserDataException(String.format("ProductStore publication '%s' cannot include multiple components", name));
        }
        this.component = (SoftwareComponentInternal) component;

    }

    public void artifact(Object source) {
        mavenArtifact = artifactNotationParser.parseNotation(source);
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
        return component;
    }

    @Override
    public File getArtifactFile() {
        if (mavenArtifact == null) {
            throw new IllegalStateException("No artifact specified to get file location of");
        }
        return mavenArtifact.getFile();
    }

    @Override
    public String getApplicationVersion() {
        if (version != null)
            return version;
        throw new InvalidUserDataException("No version or module version specified for productstore publication");
    }

    public FileCollection getPublishableFiles() {
        return files;
    }

    private String getDestLeafname() {
        return application + "-" + getApplicationVersion() + ".jar";
    }

    @Override
    public String getDestFile() {
        return application + "/" + getDestLeafname();
    }
}
