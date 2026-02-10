#include <jni.h>
#include <string>
#include <vector>
#include <mutex>
#include <android/log.h>

extern "C" {
#include "llama.h"
}

#define TAG "CutTheNoiseJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static llama_model * g_model = nullptr;
static std::mutex g_mutex;

static constexpr int N_CTX = 1024; // Increased context for analysis
static constexpr int MAX_GEN = 256; // Increased generation limit for full report

extern "C"
JNIEXPORT void JNICALL
Java_dev_belalkhan_cutthenoise_llm_NativeBridge_initModel(
        JNIEnv * env,
        jobject,
        jstring modelPath
) {
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_model != nullptr) return;

    const char * path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Loading model from: %s", path);

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.use_mmap = false;

    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(modelPath, path);

    if (!g_model) {
        LOGE("Failed to load model!");
        return;
    }
    LOGI("Model loaded successfully.");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_belalkhan_cutthenoise_llm_NativeBridge_runInference(
        JNIEnv * env,
        jobject,
        jstring prompt,
        jobject callback
) {
    std::lock_guard<std::mutex> lock(g_mutex);

    if (!g_model) return env->NewStringUTF("Error: Model not initialized");

    const char * input = env->GetStringUTFChars(prompt, nullptr);
    std::string text(input);
    env->ReleaseStringUTFChars(prompt, input);

    // 1. Create Fresh Context (Crucial for stability)
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = N_CTX;
    ctx_params.n_threads = 4;
    ctx_params.n_threads_batch = 4;

    llama_context * ctx = llama_init_from_model(g_model, ctx_params);
    if (!ctx) return env->NewStringUTF("Error: Failed to create context");

    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)V");

    const struct llama_vocab * vocab = llama_model_get_vocab(g_model);

    // 2. Tokenize
    std::vector<llama_token> tokens(N_CTX);
    int n_tokens = llama_tokenize(vocab, text.c_str(), (int)text.size(), tokens.data(), (int)tokens.size(), true, false);

    if (n_tokens <= 0) {
        llama_free(ctx);
        return env->NewStringUTF("Error: Tokenization failed");
    }

    // 3. Decode Prompt
    llama_batch batch = llama_batch_init(n_tokens, 0, 1);
    for (int i = 0; i < n_tokens; i++) {
        batch.token[i]     = tokens[i];
        batch.pos[i]       = i;
        batch.n_seq_id[i]  = 1;
        batch.seq_id[i][0] = 0;
        batch.logits[i]    = (i == n_tokens - 1);
    }
    batch.n_tokens = n_tokens;

    if (llama_decode(ctx, batch) != 0) {
        llama_batch_free(batch);
        llama_free(ctx);
        return env->NewStringUTF("Error: Decode failed");
    }
    llama_batch_free(batch);

    // 4. Generation Loop
    llama_sampler * sampler = llama_sampler_init_greedy();
    std::string output = "";

    llama_batch gen_batch = llama_batch_init(1, 0, 1);

    for (int i = 0; i < MAX_GEN; i++) {
        llama_token next = llama_sampler_sample(sampler, ctx, -1);

        if (llama_vocab_is_eog(vocab, next)) break;

        char buf[128] = {0};
        int len = llama_token_to_piece(vocab, next, buf, sizeof(buf), 0, true);

        if (len > 0) {
            output.append(buf, len);
            if (onTokenMethod != nullptr) {
                jstring javaToken = env->NewStringUTF(buf);
                env->CallVoidMethod(callback, onTokenMethod, javaToken);
                env->DeleteLocalRef(javaToken);
            }
        }

        gen_batch.token[0]     = next;
        gen_batch.pos[0]       = n_tokens + i;
        gen_batch.n_seq_id[0]  = 1;
        gen_batch.seq_id[0][0] = 0;
        gen_batch.logits[0]    = true;
        gen_batch.n_tokens     = 1;

        if (llama_decode(ctx, gen_batch) != 0) break;
    }

    // 5. Cleanup
    llama_batch_free(gen_batch);
    llama_sampler_free(sampler);
    llama_free(ctx);

    return env->NewStringUTF(output.c_str());
}