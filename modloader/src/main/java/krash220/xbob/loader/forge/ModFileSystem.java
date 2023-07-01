package krash220.xbob.loader.forge;

import java.nio.file.spi.FileSystemProvider;

import krash220.xbob.loader.fs.AbstractFileSystem;

class ModFileSystem extends AbstractFileSystem {

    static final ModFileSystem INSTANCE = new ModFileSystem();

    @Override
    public FileSystemProvider provider() {
        return ModFileSystemProvider.INSTANCE;
    }
}
