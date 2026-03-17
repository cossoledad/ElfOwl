package org.elfowl.loader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class NativeLoadResult {

    private final List<Path> scannedLibraries;
    private final List<Path> plannedOrder;
    private final List<Path> loadedLibraries;
    private final List<List<Path>> cycles;
    private final Map<Path, List<String>> unresolvedDependencies;

    NativeLoadResult(
        List<Path> scannedLibraries,
        List<Path> plannedOrder,
        List<Path> loadedLibraries,
        List<List<Path>> cycles,
        Map<Path, List<String>> unresolvedDependencies
    ) {
        this.scannedLibraries = Collections.unmodifiableList(new ArrayList<Path>(scannedLibraries));
        this.plannedOrder = Collections.unmodifiableList(new ArrayList<Path>(plannedOrder));
        this.loadedLibraries = Collections.unmodifiableList(new ArrayList<Path>(loadedLibraries));
        this.cycles = Collections.unmodifiableList(new ArrayList<List<Path>>(cycles));
        this.unresolvedDependencies = Collections.unmodifiableMap(unresolvedDependencies);
    }

    public List<Path> getScannedLibraries() {
        return scannedLibraries;
    }

    public List<Path> getPlannedOrder() {
        return plannedOrder;
    }

    public List<Path> getLoadedLibraries() {
        return loadedLibraries;
    }

    public List<List<Path>> getCycles() {
        return cycles;
    }

    public Map<Path, List<String>> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }
}
