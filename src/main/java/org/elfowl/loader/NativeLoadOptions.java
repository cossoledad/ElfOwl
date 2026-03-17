package org.elfowl.loader;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

public final class NativeLoadOptions {

    private final boolean recursive;
    private final boolean failOnCycle;
    private final boolean strictDependencyResolution;
    private final boolean failFastOnLoadError;
    private final Predicate<Path> libraryFilter;

    private NativeLoadOptions(Builder builder) {
        this.recursive = builder.recursive;
        this.failOnCycle = builder.failOnCycle;
        this.strictDependencyResolution = builder.strictDependencyResolution;
        this.failFastOnLoadError = builder.failFastOnLoadError;
        this.libraryFilter = builder.libraryFilter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static NativeLoadOptions defaults() {
        return builder().build();
    }

    public boolean isRecursive() {
        return recursive;
    }

    public boolean isFailOnCycle() {
        return failOnCycle;
    }

    public boolean isStrictDependencyResolution() {
        return strictDependencyResolution;
    }

    public boolean isFailFastOnLoadError() {
        return failFastOnLoadError;
    }

    public Predicate<Path> getLibraryFilter() {
        return libraryFilter;
    }

    public static final class Builder {
        private boolean recursive = true;
        private boolean failOnCycle = false;
        private boolean strictDependencyResolution = false;
        private boolean failFastOnLoadError = true;
        private Predicate<Path> libraryFilter = NativeLibraryLoader.defaultLibraryFilter();

        public Builder recursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }

        public Builder failOnCycle(boolean failOnCycle) {
            this.failOnCycle = failOnCycle;
            return this;
        }

        public Builder strictDependencyResolution(boolean strictDependencyResolution) {
            this.strictDependencyResolution = strictDependencyResolution;
            return this;
        }

        public Builder failFastOnLoadError(boolean failFastOnLoadError) {
            this.failFastOnLoadError = failFastOnLoadError;
            return this;
        }

        public Builder libraryFilter(Predicate<Path> libraryFilter) {
            this.libraryFilter = Objects.requireNonNull(libraryFilter, "libraryFilter");
            return this;
        }

        public NativeLoadOptions build() {
            return new NativeLoadOptions(this);
        }
    }
}
