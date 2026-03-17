#include <jni.h>
#include <stdio.h>

const char* owl_song_message(void);
const char* owl_bridge_message(void);

const char* owl_entry_message(void) {
    static char buffer[512];
    snprintf(buffer, sizeof(buffer), "entry{%s + %s}", owl_song_message(), owl_bridge_message());
    return buffer;
}

JNIEXPORT jstring JNICALL Java_org_elfowl_example_NativeDemo_nativeMessage(JNIEnv* env, jclass clazz) {
    (void) clazz;
    return (*env)->NewStringUTF(env, owl_entry_message());
}
