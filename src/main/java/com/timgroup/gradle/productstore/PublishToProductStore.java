package com.timgroup.gradle.productstore;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class PublishToProductStore extends DefaultTask {
    private ProductStorePublicationInternal publication;
    private RegularFileProperty identityFile;
    private Property<String> storeUser;
    private Property<String> storeHost;
    private Property<String> storePath;

    public PublishToProductStore() {
        getInputs().files((Callable<FileCollection>) () -> {
            if (publication != null) {
                return publication.getPublishableFiles();
            }
            else {
                return null;
            }
        }).withPropertyName("publication.publishableFiles");
    }

    public ProductStorePublicationInternal getPublication() {
        return publication;
    }

    public void setPublication(ProductStorePublicationInternal publication) {
        this.publication = publication;
    }

    public RegularFileProperty getIdentityFile() {
        return identityFile;
    }

    public void setIdentityFile(RegularFileProperty identityFile) {
        this.identityFile = identityFile;
    }

    public Property<String> getStoreUser() {
        return storeUser;
    }

    public void setStoreUser(Property<String> storeUser) {
        this.storeUser = storeUser;
    }

    public Property<String> getStoreHost() {
        return storeHost;
    }

    public void setStoreHost(Property<String> storeHost) {
        this.storeHost = storeHost;
    }

    public Property<String> getStorePath() {
        return storePath;
    }

    public void setStorePath(Property<String> storePath) {
        this.storePath = storePath;
    }

    @Inject
    protected ExecActionFactory getExecActionFactory() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void publish() {
        if (publication == null) {
            throw new InvalidUserDataException("The 'publication' property is required");
        }
        if (identityFile == null) {
            throw new InvalidUserDataException("The 'identityFile' property is required");
        }

        String targetPathname = String.format("%s/%s", storePath.get(), publication.getDestFile());
        String targetDir = dirname(targetPathname);
        String target = String.format("%s@%s:%s", storeUser.get(), storeHost.get(), targetPathname);
        String url = String.format("ssh://%s@%s%s", storeUser.get(), storeHost.get(), targetPathname);

        System.out.println("Upload " + url);


        exec("ssh", "-i", identityFile.getAsFile().get().toString(), String.format("%s@%s", storeUser.get(), storeHost.get()), "mkdir -p " + targetDir);
        exec("scp", "-o", "StrictHostKeyChecking=no", "-i", identityFile.getAsFile().get().toString(), publication.getArtifactFile().toString(), target);
    }

    private void exec(String... commandLine) {
        ExecAction execAction = getExecActionFactory().newExecAction();
        execAction.setCommandLine(Arrays.asList(commandLine));
        execAction.execute();
    }

    private static String dirname(String pathname) {
        int lastSlash = pathname.lastIndexOf('/');
        if (lastSlash < 0)
            return ".";
        return pathname.substring(0, lastSlash);
    }
}
