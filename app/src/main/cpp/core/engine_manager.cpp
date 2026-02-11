#include "engine_manager.h"
#include <vector>
#include <android/log.h>

#define TAG "CutTheNoiseCore"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

EngineManager& EngineManager::getInstance() {
    static EngineManager instance;
    return instance;
}

bool EngineManager::loadModel(const char* modelPath) {
    std::lock_guard<std::mutex> lock(g_mutex);

    if (g_model != nullptr) {
        LOGI("Model already loaded.");
        return true;
    }

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.use_mmap = false;

    g_model = llama_model_load_from_file(modelPath, model_params);

    if (!g_model) {
        LOGE("Failed to load model from path: %s", modelPath);
        return false;
    }

    LOGI("Model loaded successfully.");
    return true;
}

std::string EngineManager::infer(const char* prompt, TokenCallback callback) {
    std::lock_guard<std::mutex> lock(g_mutex);

    if (!g_model) return "Error: Model not initialized";

    // 1. Create a fresh context for this request
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = N_CTX;
    ctx_params.n_threads = 4;
    ctx_params.n_threads_batch = 4;

    llama_context* ctx = llama_init_from_model(g_model, ctx_params);
    if (!ctx) return "Error: Failed to create context";

    const struct llama_vocab* vocab = llama_model_get_vocab(g_model);
    std::string text(prompt);

    // 2. Tokenize
    std::vector<llama_token> tokens(N_CTX);
    int n_tokens = llama_tokenize(vocab, text.c_str(), (int)text.size(), tokens.data(), (int)tokens.size(), true, false);

    if (n_tokens <= 0) {
        llama_free(ctx);
        return "Error: Tokenization failed";
    }

    // 3. Decode Prompt (Batching for responsiveness)
    int n_chunk = 8;
    for (int i = 0; i < n_tokens; i += n_chunk) {
        int n_eval = n_tokens - i;
        if (n_eval > n_chunk) n_eval = n_chunk;

        llama_batch batch = llama_batch_init(n_eval, 0, 1);
        for (int j = 0; j < n_eval; j++) {
            int pos = i + j;
            batch.token[j] = tokens[pos];
            batch.pos[j] = pos;
            batch.n_seq_id[j] = 1;
            batch.seq_id[j][0] = 0;
            batch.logits[j] = (pos == n_tokens - 1);
        }
        batch.n_tokens = n_eval;

        if (llama_decode(ctx, batch) != 0) {
            llama_batch_free(batch);
            llama_free(ctx);
            return "Error: Prompt decode failed";
        }
        llama_batch_free(batch);
    }

    // 4. Generation Loop
    llama_sampler* sampler = llama_sampler_init_greedy();
    std::string output = "";

    llama_batch gen_batch = llama_batch_init(1, 0, 1);

    for (int i = 0; i < MAX_GEN; i++) {
        llama_token next = llama_sampler_sample(sampler, ctx, -1);

        if (llama_vocab_is_eog(vocab, next)) break;

        char buf[128] = {0};
        int len = llama_token_to_piece(vocab, next, buf, sizeof(buf), 0, true);

        if (len > 0) {
            output.append(buf, len);
            // Send token back to bridge
            if (callback) {
                callback(buf);
            }
        }

        gen_batch.token[0] = next;
        gen_batch.pos[0] = n_tokens + i;
        gen_batch.n_seq_id[0] = 1;
        gen_batch.seq_id[0][0] = 0;
        gen_batch.logits[0] = true;
        gen_batch.n_tokens = 1;

        if (llama_decode(ctx, gen_batch) != 0) break;
    }

    // 5. Cleanup
    llama_batch_free(gen_batch);
    llama_sampler_free(sampler);
    llama_free(ctx);

    return output;
}