package ik.ffm1.loader.forge;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

class ModFileSystem extends FileSystem {
    
    static final ModFileSystem INSTANCE = new ModFileSystem();

    @Override
    public FileSystemProvider provider() {
        return ModFileSystemProvider.INSTANCE;
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSeparator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(String first, String... more) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

}
