package com.timgroup.gradle.productstore;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.tooling.BuildActionFailureException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Internal
    public ProductStorePublicationInternal getPublication() {
        return publication;
    }

    public void setPublication(ProductStorePublicationInternal publication) {
        this.publication = publication;
    }

    @Input
    public RegularFileProperty getIdentityFile() {
        return identityFile;
    }

    public void setIdentityFile(RegularFileProperty identityFile) {
        this.identityFile = identityFile;
    }

    @Input
    public Property<String> getStoreUser() {
        return storeUser;
    }

    public void setStoreUser(Property<String> storeUser) {
        this.storeUser = storeUser;
    }

    @Input
    public Property<String> getStoreHost() {
        return storeHost;
    }

    public void setStoreHost(Property<String> storeHost) {
        this.storeHost = storeHost;
    }

    @Input
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

        if (storeHost.get().equals("localhost")) {
            System.out.println("Copy " + publication.getArtifactFile() + " to " + targetPathname);
            try {
                Files.createDirectories(Paths.get(targetDir));
                Files.copy(publication.getArtifactFile().toPath(), Paths.get(targetPathname));
            } catch (IOException e) {
                throw new BuildActionFailureException("Failed to copy file to target: " + e, e);
            }
        }
        else {
            String url = String.format("ssh://%s@%s%s", storeUser.get(), storeHost.get(), targetPathname);
            String target = String.format("%s@%s:%s", storeUser.get(), storeHost.get(), targetPathname);

            System.out.println("Upload " + url);

            exec("ssh", "-i", identityFile.getAsFile().get().toString(), String.format("%s@%s", storeUser.get(), storeHost.get()), "mkdir -p " + targetDir);
            exec("scp", "-o", "StrictHostKeyChecking=no", "-i", identityFile.getAsFile().get().toString(), publication.getArtifactFile().toString(), target);
            exec("ssh", "-i", identityFile.getAsFile().get().toString(), String.format("%s@%s", storeUser.get(), storeHost.get()), "chmod 0444 " + targetPathname);
        }
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
