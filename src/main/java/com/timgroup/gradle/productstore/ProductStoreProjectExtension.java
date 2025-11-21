package com.timgroup.gradle.productstore;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class ProductStoreProjectExtension {
    private final Property<String> host;
    private final Property<String> user;
    private final Property<String> path;
    private final RegularFileProperty identity;

    public ProductStoreProjectExtension(ObjectFactory objectFactory) {
        host = objectFactory.property(String.class);
        user = objectFactory.property(String.class);
        path = objectFactory.property(String.class);
        identity = objectFactory.fileProperty();

        host.set("productstore");
        user.set("productstore");
        path.set("/opt/ProductStore");
    }

    public Property<String> getHost() {
        return host;
    }

    public Property<String> getUser() {
        return user;
    }

    public Property<String> getPath() {
        return path;
    }

    public RegularFileProperty getIdentity() {
        return identity;
    }
}
