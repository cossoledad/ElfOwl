package io.github.ganjb.elfowl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LibraryMetadata {

    private final Path path;
    private final String basename;
    private final String soname;
    private final List<String> neededLibraries;

    public LibraryMetadata(Path path, String basename, String soname, List<String> neededLibraries) {
        this.path = Objects.requireNonNull(path, "path");
        this.basename = Objects.requireNonNull(basename, "basename");
        this.soname = soname;
        this.neededLibraries = Collections.unmodifiableList(new ArrayList<String>(
            neededLibraries == null ? Collections.<String>emptyList() : neededLibraries
        ));
    }

    public Path getPath() {
        return path;
    }

    public String getBasename() {
        return basename;
    }

    public String getSoname() {
        return soname;
    }

    public List<String> getNeededLibraries() {
        return neededLibraries;
    }
}
