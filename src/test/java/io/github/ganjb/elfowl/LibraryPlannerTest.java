package io.github.ganjb.elfowl;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LibraryPlannerTest {

    @Test
    void shouldProduceDependencyFirstOrder() {
        Path c = Paths.get("/tmp/libc.so");
        Path b = Paths.get("/tmp/libb.so");
        Path a = Paths.get("/tmp/liba.so");

        LibraryPlanner planner = new LibraryPlanner();
        LibraryPlan plan = planner.plan(
            Arrays.asList(a, b, c),
            parserOf(new LibraryMetadata(a, "liba.so", "liba.so", Arrays.asList("libb.so")),
                new LibraryMetadata(b, "libb.so", "libb.so", Arrays.asList("libc.so")),
                new LibraryMetadata(c, "libc.so", "libc.so", Collections.<String>emptyList())),
            NativeLoadOptions.defaults()
        );

        assertEquals(Arrays.asList(c, b, a), plan.getLoadOrder());
    }

    @Test
    void shouldPreferDependencyInSameDirectory() {
        Path localDep = Paths.get("/app/feature/libcrypto.so");
        Path rootDep = Paths.get("/app/libcrypto.so");
        Path user = Paths.get("/app/feature/libfeature.so");

        LibraryPlanner planner = new LibraryPlanner();
        LibraryPlan plan = planner.plan(
            Arrays.asList(user, localDep, rootDep),
            parserOf(new LibraryMetadata(user, "libfeature.so", "libfeature.so", Arrays.asList("libcrypto.so")),
                new LibraryMetadata(localDep, "libcrypto.so", "libcrypto.so", Collections.<String>emptyList()),
                new LibraryMetadata(rootDep, "libcrypto.so", "libcrypto.so", Collections.<String>emptyList())),
            NativeLoadOptions.defaults()
        );

        assertEquals(Arrays.asList(localDep, rootDep, user), plan.getLoadOrder());
    }

    @Test
    void shouldExposeCyclesWhenConfiguredToTolerateThem() {
        Path a = Paths.get("/tmp/liba.so");
        Path b = Paths.get("/tmp/libb.so");

        LibraryPlanner planner = new LibraryPlanner();
        LibraryPlan plan = planner.plan(
            Arrays.asList(a, b),
            parserOf(new LibraryMetadata(a, "liba.so", "liba.so", Arrays.asList("libb.so")),
                new LibraryMetadata(b, "libb.so", "libb.so", Arrays.asList("liba.so"))),
            NativeLoadOptions.builder().failOnCycle(false).build()
        );

        assertEquals(1, plan.getCycles().size());
        assertEquals(Arrays.asList(a, b), plan.getLoadOrder());
    }

    @Test
    void shouldFailWhenStrictDependencyResolutionEnabled() {
        Path a = Paths.get("/tmp/liba.so");

        LibraryPlanner planner = new LibraryPlanner();
        assertThrows(NativeLibraryLoadException.class, () -> planner.plan(
            Collections.singletonList(a),
            parserOf(new LibraryMetadata(a, "liba.so", "liba.so", Arrays.asList("libmissing.so"))),
            NativeLoadOptions.builder().strictDependencyResolution(true).build()
        ));
    }

    private static LibraryMetadataParser parserOf(LibraryMetadata... metadata) {
        Map<Path, LibraryMetadata> map = new HashMap<Path, LibraryMetadata>();
        for (LibraryMetadata value : metadata) {
            map.put(value.getPath(), value);
        }
        return path -> map.get(path);
    }
}
