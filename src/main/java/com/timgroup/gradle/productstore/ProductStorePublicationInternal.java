package com.timgroup.gradle.productstore;

import org.gradle.api.file.FileCollection;
import org.gradle.api.publish.internal.PublicationInternal;

import java.io.File;

public interface ProductStorePublicationInternal extends ProductStorePublication, PublicationInternal {
    File getArtifactFile();
    String getDestFile();
    String getApplicationVersion();

    FileCollection getPublishableFiles();
}
