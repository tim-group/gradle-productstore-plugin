package com.timgroup.gradle.productstore;

import org.gradle.api.publish.Publication;

public interface ProductStorePublication extends Publication {
    String getApplication();
    void setApplication(String application);
    String getVersion();
    void setVersion(String version);
}
