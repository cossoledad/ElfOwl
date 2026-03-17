#include <stdio.h>

const char* owl_branch_message(void);

const char* owl_bridge_message(void) {
    static char buffer[128];
    snprintf(buffer, sizeof(buffer), "bridge->%s", owl_branch_message());
    return buffer;
}
