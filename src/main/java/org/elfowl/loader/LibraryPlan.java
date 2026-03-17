package org.elfowl.loader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class LibraryPlan {

    private final List<Path> loadOrder;
    private final List<List<Path>> cycles;
    private final Map<Path, List<String>> unresolvedDependencies;

    LibraryPlan(List<Path> loadOrder, List<List<Path>> cycles, Map<Path, List<String>> unresolvedDependencies) {
        this.loadOrder = Collections.unmodifiableList(new ArrayList<Path>(loadOrder));
        this.cycles = Collections.unmodifiableList(new ArrayList<List<Path>>(cycles));
        this.unresolvedDependencies = Collections.unmodifiableMap(unresolvedDependencies);
    }

    List<Path> getLoadOrder() {
        return loadOrder;
    }

    List<List<Path>> getCycles() {
        return cycles;
    }

    Map<Path, List<String>> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }
}
