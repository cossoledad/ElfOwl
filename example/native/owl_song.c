#include <stdio.h>

const char* owl_seed_message(void);
const char* owl_leaf_message(void);

const char* owl_song_message(void) {
    static char buffer[256];
    snprintf(buffer, sizeof(buffer), "song[%s|%s]", owl_seed_message(), owl_leaf_message());
    return buffer;
}
