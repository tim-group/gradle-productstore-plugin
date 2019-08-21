package com.timgroup.gradle.productstore;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.artifacts.Module;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.internal.file.PathToFileResolver;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.Path;
import org.gradle.model.RuleSource;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;

import static java.util.Locale.ENGLISH;

public class ProductStorePublishPlugin implements Plugin<Project> {
    private final Instantiator instantiator;
    private final DependencyMetaDataProvider dependencyMetaDataProvider;
    private final ImmutableAttributesFactory immutableAttributesFactory;
    private final FileCollectionFactory fileCollectionFactory;
    private final PathToFileResolver pathToFileResolver;

    @Inject
    public ProductStorePublishPlugin(Instantiator instantiator, DependencyMetaDataProvider dependencyMetaDataProvider, ImmutableAttributesFactory immutableAttributesFactory, FileCollectionFactory fileCollectionFactory, PathToFileResolver pathToFileResolver) {
        this.instantiator = instantiator;
        this.dependencyMetaDataProvider = dependencyMetaDataProvider;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.fileCollectionFactory = fileCollectionFactory;
        this.pathToFileResolver = pathToFileResolver;
    }

    @Override
    public void apply(@Nonnull Project project) {
        project.getPluginManager().apply(PublishingPlugin.class);

        project.getExtensions().configure(PublishingExtension.class, extension -> {
            extension.getPublications().registerFactory(ProductStorePublication.class, new PublicationFactory());
        });

        project.getExtensions().create("productstore", ProductStoreProjectExtension.class, project.getObjects());
    }

    @SuppressWarnings("unused")
    static class Rules extends RuleSource {
        @Model
        ProductStoreProjectExtension productstore(ExtensionContainer extensions) {
            return extensions.getByType(ProductStoreProjectExtension.class);
        }

        @Mutate
        public void realizePublishingTasks(ModelMap<Task> tasks, PublishingExtension extension, @Path("buildDir") File buildDir, ProductStoreProjectExtension productStoreExtension) {
            PublicationContainer publications = extension.getPublications();
            Task publishLifecycleTask = tasks.get(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME);

            NamedDomainObjectSet<ProductStorePublicationInternal> productStorePublications = publications.withType(ProductStorePublicationInternal.class);

            for (ProductStorePublicationInternal publication : productStorePublications) {
                String publicationName = publication.getName();
                String publishTaskName = "publishTo" + capitalize(publicationName);
                tasks.create(publishTaskName, PublishToProductStore.class, task -> {
                    task.setPublication(publication);
                    task.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
                    task.getIdentityFile().set(productStoreExtension.getIdentity());
                    task.getStoreUser().set(productStoreExtension.getUser());
                    task.getStoreHost().set(productStoreExtension.getHost());
                    task.getStorePath().set(productStoreExtension.getPath());
                    task.setDescription("Publishes ProductStore publication '" + publicationName + "'");
                });
                publishLifecycleTask.dependsOn(publishTaskName);
            }
        }
    }

    private final class PublicationFactory implements NamedDomainObjectFactory<ProductStorePublication> {
        @Override
        @Nonnull
        public ProductStorePublication create(@Nonnull String name) {
            Module module = dependencyMetaDataProvider.getModule();
            return instantiator.newInstance(DefaultProductStorePublication.class,
                                            name, module.getVersion(),
                                            immutableAttributesFactory, instantiator, fileCollectionFactory, pathToFileResolver);
        }
    }

    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }
}
