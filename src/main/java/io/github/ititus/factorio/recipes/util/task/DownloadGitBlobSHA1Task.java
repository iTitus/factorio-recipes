package io.github.ititus.factorio.recipes.util.task;

import io.github.ititus.io.IO;
import io.github.ititus.math.hash.Hashing;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

public class DownloadGitBlobSHA1Task implements Callable<Void> {

    private final Path file;
    private final String expectedSHA, url;
    private final boolean offlineMode;

    public DownloadGitBlobSHA1Task(Path file, String expectedSHA, String url, boolean offlineMode) {
        this.file = file;
        this.expectedSHA = expectedSHA;
        this.url = url;
        this.offlineMode = offlineMode;
    }

    @Override
    public Void call() {
        if (Files.isDirectory(file)) {
            throw new RuntimeException();
        }

        String expectedSHA = this.expectedSHA;

        Path parentFile = file.getParent();
        try {
            Files.createDirectories(parentFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Path shaFile = file.resolveSibling(file.getFileName() + ".sha1");
        if (Files.isDirectory(shaFile)) {
            throw new RuntimeException();
        }

        boolean download = false, createSHAFile = false;

        if (Files.isRegularFile(file)) {
            String sha;
            if (Files.isRegularFile(shaFile)) {
                try {
                    sha = Files.readString(shaFile);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                sha = Hashing.SHA_1.computeGitBlobSHA1(file);
                createSHAFile = true;
            }

            if (offlineMode && expectedSHA == null) {
                expectedSHA = sha;
            }

            if (!Objects.equals(sha, expectedSHA)) {
                download = true;
            }
        } else {
            if (offlineMode) {
                throw new RuntimeException("offline mode is activated but file was not found");
            }
            download = true;
        }

        if (download) {
            IO.copyUrlToPath(url, file);
        }
        if (download || createSHAFile) {
            try {
                Files.writeString(shaFile, expectedSHA);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return null;
    }

}
