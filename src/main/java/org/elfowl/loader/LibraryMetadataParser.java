package org.elfowl.loader;

import java.nio.file.Path;

public interface LibraryMetadataParser {

    LibraryMetadata parse(Path path);
}
