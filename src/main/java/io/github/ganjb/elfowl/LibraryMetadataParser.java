package io.github.ganjb.elfowl;

import java.nio.file.Path;

public interface LibraryMetadataParser {

    LibraryMetadata parse(Path path);
}
