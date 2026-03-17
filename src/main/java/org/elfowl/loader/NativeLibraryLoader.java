package org.elfowl.loader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class NativeLibraryLoader {

    private static final Set<String> LOADED_PATHS = ConcurrentHashMap.newKeySet();

    private final DirectoryLibraryScanner scanner;
    private final LibraryPlanner planner;
    private final LibraryMetadataParser parser;
    private final Loader loader;

    public NativeLibraryLoader() {
        this(new DirectoryLibraryScanner(), new LibraryPlanner(), new ElfLibraryMetadataParser(), new SystemLoader());
    }

    NativeLibraryLoader(
        DirectoryLibraryScanner scanner,
        LibraryPlanner planner,
        LibraryMetadataParser parser,
        Loader loader
    ) {
        this.scanner = scanner;
        this.planner = planner;
        this.parser = parser;
        this.loader = loader;
    }

    public static NativeLoadResult loadDirectory(Path directory) {
        return new NativeLibraryLoader().load(directory, NativeLoadOptions.defaults());
    }

    public static NativeLoadResult loadDirectory(Path directory, NativeLoadOptions options) {
        return new NativeLibraryLoader().load(directory, options);
    }

    public NativeLoadResult load(Path directory, NativeLoadOptions options) {
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(options, "options");

        List<Path> libraries = scanner.scan(directory, options.isRecursive(), options.getLibraryFilter());
        LibraryPlan plan = planner.plan(libraries, parser, options);

        List<Path> loaded = new ArrayList<Path>();
        for (Path library : plan.getLoadOrder()) {
            String absolutePath = library.toAbsolutePath().normalize().toString();
            if (!LOADED_PATHS.add(absolutePath)) {
                loaded.add(library);
                continue;
            }
            try {
                loader.load(absolutePath);
                loaded.add(library);
            } catch (Throwable t) {
                LOADED_PATHS.remove(absolutePath);
                if (options.isFailFastOnLoadError()) {
                    throw new NativeLibraryLoadException("Failed to load native library: " + absolutePath, t);
                }
            }
        }

        return new NativeLoadResult(
            libraries,
            plan.getLoadOrder(),
            loaded,
            plan.getCycles(),
            plan.getUnresolvedDependencies()
        );
    }

    public static Predicate<Path> defaultLibraryFilter() {
        return path -> {
            String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
            return name.endsWith(".so")
                || name.contains(".so.")
                || name.endsWith(".dylib")
                || name.endsWith(".dll");
        };
    }

    public static void resetLoadedStateForTests() {
        LOADED_PATHS.clear();
    }

    interface Loader {
        void load(String absolutePath);
    }

    private static final class SystemLoader implements Loader {
        @Override
        public void load(String absolutePath) {
            System.load(absolutePath);
        }
    }
}
