package org.elfowl.example;

import org.elfowl.loader.NativeLibraryLoader;
import org.elfowl.loader.NativeLoadOptions;
import org.elfowl.loader.NativeLoadResult;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class NativeDemo {

    private NativeDemo() {
    }

    public static void main(String[] args) {
        Path nativeDirectory = args.length > 0
            ? Paths.get(args[0]).toAbsolutePath().normalize()
            : Paths.get("example/build/native").toAbsolutePath().normalize();

        NativeLoadResult result = NativeLibraryLoader.loadDirectory(
            nativeDirectory,
            NativeLoadOptions.builder()
                .recursive(false)
                .strictDependencyResolution(false)
                .failOnCycle(true)
                .build()
        );

        System.out.println("Native directory: " + nativeDirectory);
        System.out.println("Scanned libraries: " + result.getScannedLibraries().size());
        System.out.println("Planned order:");
        for (Path path : result.getPlannedOrder()) {
            System.out.println("  - " + path.getFileName());
        }
        System.out.println("Unresolved external dependencies: " + result.getUnresolvedDependencies());
        System.out.println("JNI message: " + nativeMessage());
    }

    private static native String nativeMessage();
}
