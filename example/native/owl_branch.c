#include <stdio.h>

const char* owl_seed_message(void);

const char* owl_branch_message(void) {
    static char buffer[128];
    snprintf(buffer, sizeof(buffer), "branch->%s", owl_seed_message());
    return buffer;
}
