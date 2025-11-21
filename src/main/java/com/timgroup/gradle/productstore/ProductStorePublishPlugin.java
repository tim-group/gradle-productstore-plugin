package com.timgroup.gradle.productstore;

import org.gradle.api.*;
import org.gradle.api.internal.artifacts.Module;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.model.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;

import static java.util.Locale.ENGLISH;

public class ProductStorePublishPlugin implements Plugin<Project> {
    private final ObjectFactory objectFactory;
    private final DependencyMetaDataProvider dependencyMetaDataProvider;

    @Inject
    public ProductStorePublishPlugin(ObjectFactory objectFactory, DependencyMetaDataProvider dependencyMetaDataProvider) {
        this.objectFactory = objectFactory;
        this.dependencyMetaDataProvider = dependencyMetaDataProvider;
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
                    task.getIdentityFile().convention(productStoreExtension.getIdentity());
                    task.getStoreUser().convention(productStoreExtension.getUser());
                    task.getStoreHost().convention(productStoreExtension.getHost());
                    task.getStorePath().convention(productStoreExtension.getPath());
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
            return objectFactory.newInstance(DefaultProductStorePublication.class, name, module.getVersion());
        }
    }

    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }
}
