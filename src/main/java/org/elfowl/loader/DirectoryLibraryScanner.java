package org.elfowl.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class DirectoryLibraryScanner {

    List<Path> scan(Path directory, boolean recursive, Predicate<Path> filter) {
        if (!Files.isDirectory(directory)) {
            throw new NativeLibraryLoadException("Native library directory does not exist: " + directory);
        }

        try (Stream<Path> stream = recursive ? Files.walk(directory) : Files.list(directory)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(filter)
                .map(Path::toAbsolutePath)
                .sorted(Comparator.comparing(Path::toString))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new NativeLibraryLoadException("Failed to scan native library directory: " + directory, e);
        }
    }
}
