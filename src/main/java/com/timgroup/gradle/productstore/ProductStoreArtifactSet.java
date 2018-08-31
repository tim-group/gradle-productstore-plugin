package com.timgroup.gradle.productstore;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;

import java.io.File;

public interface ProductStoreArtifactSet extends DomainObjectSet<ProductStoreArtifact> {
    ProductStoreArtifact artifact(Object source);
    ProductStoreArtifact artifact(Object source, Action<? super ProductStoreArtifact> config);

    File getArtifactFile();
}
