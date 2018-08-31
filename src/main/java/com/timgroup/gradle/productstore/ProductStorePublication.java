package com.timgroup.gradle.productstore;

import org.gradle.api.Action;
import org.gradle.api.publish.Publication;

public interface ProductStorePublication extends Publication {
    String getApplication();
    void setApplication(String application);
    String getVersion();
    void setVersion(String version);

    void artifact(Object source);
    void artifact(Object source, Action<? super ProductStoreArtifact> config);

    ProductStoreArtifactSet getArtifacts();
}
