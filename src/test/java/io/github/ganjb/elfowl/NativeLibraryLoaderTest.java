package io.github.ganjb.elfowl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeLibraryLoaderTest {

    @AfterEach
    void tearDown() {
        NativeLibraryLoader.resetLoadedStateForTests();
    }

    @Test
    void shouldLoadLibrariesInPlannedOrder() throws IOException {
        Path dir = Files.createTempDirectory("elfowl");
        Path c = Files.createFile(dir.resolve("libc.so"));
        Path b = Files.createFile(dir.resolve("libb.so"));
        Path a = Files.createFile(dir.resolve("liba.so"));

        List<String> loaded = new ArrayList<String>();
        NativeLibraryLoader loader = new NativeLibraryLoader(
            new DirectoryLibraryScanner(),
            new LibraryPlanner(),
            path -> {
                String name = path.getFileName().toString();
                if ("liba.so".equals(name)) {
                    return new LibraryMetadata(path, name, name, Arrays.asList("libb.so"));
                }
                if ("libb.so".equals(name)) {
                    return new LibraryMetadata(path, name, name, Arrays.asList("libc.so"));
                }
                return new LibraryMetadata(path, name, name, Collections.<String>emptyList());
            },
            loaded::add
        );

        NativeLoadResult result = loader.load(dir, NativeLoadOptions.defaults());

        assertEquals(Arrays.asList(c.toString(), b.toString(), a.toString()), loaded);
        assertEquals(Arrays.asList(c, b, a), result.getPlannedOrder());
        assertEquals(Arrays.asList(c, b, a), result.getLoadedLibraries());
    }
}
