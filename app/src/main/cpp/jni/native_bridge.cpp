#include <jni.h>
#include <string>
#include "../core/engine_manager.h"

// JNI Helper macro to convert Java string to C string
const char *GetStringUTFChars(JNIEnv *env, jstring string) {
    return env->GetStringUTFChars(string, nullptr);
}

// JNI Helper macro to release string
void ReleaseStringUTFChars(JNIEnv *env, jstring string, const char *chars) {
    env->ReleaseStringUTFChars(string, chars);
}

extern "C"
JNIEXPORT void JNICALL
Java_dev_belalkhan_cutthenoise_data_local_llm_NativeLlmBridge_initModel(
        JNIEnv *env,
        jobject /* this */,
        jstring modelPath
) {
    const char *path = GetStringUTFChars(env, modelPath);

    EngineManager::getInstance().loadModel(path);

    ReleaseStringUTFChars(env, modelPath, path);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_belalkhan_cutthenoise_data_local_llm_NativeLlmBridge_runInference(
        JNIEnv *env,
        jobject /* this */,
        jstring prompt,
        jobject callback
) {
    const char *nativePrompt = GetStringUTFChars(env, prompt);

    // Prepare the callback method ID
    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)V");

    // Define the lambda that EngineManager will call
    auto tokenCallback = [&](const char *token) {
        if (onTokenMethod != nullptr) {
            jstring javaToken = env->NewStringUTF(token);
            env->CallVoidMethod(callback, onTokenMethod, javaToken);
            env->DeleteLocalRef(javaToken);
        }
    };

    // Delegate execution to Core Logic
    std::string result = EngineManager::getInstance().infer(nativePrompt, tokenCallback);

    ReleaseStringUTFChars(env, prompt, nativePrompt);
    return env->NewStringUTF(result.c_str());
}