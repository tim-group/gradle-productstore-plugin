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

    @Inject
    public ProductStorePublishPlugin(Instantiator instantiator, DependencyMetaDataProvider dependencyMetaDataProvider, ImmutableAttributesFactory immutableAttributesFactory, FileCollectionFactory fileCollectionFactory) {
        this.instantiator = instantiator;
        this.dependencyMetaDataProvider = dependencyMetaDataProvider;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.fileCollectionFactory = fileCollectionFactory;
    }

    @Override
    public void apply(@Nonnull Project project) {
        project.getPluginManager().apply(PublishingPlugin.class);

        project.getExtensions().configure(PublishingExtension.class, extension -> {
            extension.getPublications().registerFactory(ProductStorePublication.class, new PublicationFactory());
        });

        project.getExtensions().create("productstore", ProductStoreProjectExtension.class, project.getObjects(), project.getLayout());
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
                    task.setIdentityFile(productStoreExtension.getIdentity());
                    task.setStoreUser(productStoreExtension.getUser());
                    task.setStoreHost(productStoreExtension.getHost());
                    task.setStorePath(productStoreExtension.getPath());
                    task.setDescription("Publishes ProductStore publication '" + publicationName + "'");
                });
                publishLifecycleTask.dependsOn(publishTaskName);
            }
        }
    }

    private class PublicationFactory implements NamedDomainObjectFactory<ProductStorePublication> {
        @Override
        @Nonnull
        public ProductStorePublication create(@Nonnull String name) {
            Module module = dependencyMetaDataProvider.getModule();
            return new DefaultProductStorePublication(name, module.getGroup(), module.getVersion(),
                    immutableAttributesFactory, instantiator, fileCollectionFactory);
        }
    }

    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }
}
