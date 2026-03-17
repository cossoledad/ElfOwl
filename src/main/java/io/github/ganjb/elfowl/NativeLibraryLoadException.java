package io.github.ganjb.elfowl;

public class NativeLibraryLoadException extends RuntimeException {

    public NativeLibraryLoadException(String message) {
        super(message);
    }

    public NativeLibraryLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
