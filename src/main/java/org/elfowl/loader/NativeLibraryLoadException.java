package org.elfowl.loader;

public class NativeLibraryLoadException extends RuntimeException {

    public NativeLibraryLoadException(String message) {
        super(message);
    }

    public NativeLibraryLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
