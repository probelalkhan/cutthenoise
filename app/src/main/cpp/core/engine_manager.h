#ifndef ENGINE_MANAGER_H
#define ENGINE_MANAGER_H

#include <string>
#include <functional>
#include <mutex>
#include "llama.h"

// Defines a callback type for streaming tokens back to the bridge
using TokenCallback = std::function<void(const char *)>;

class EngineManager {
public:
    static EngineManager &getInstance();

    // Loads the model from the file path
    bool loadModel(const char *modelPath);

    // Runs inference and streams result via callback
    std::string infer(const char *prompt, TokenCallback callback);

private:
    EngineManager() = default;

    ~EngineManager() = default;

    EngineManager(const EngineManager &) = delete;

    EngineManager &operator=(const EngineManager &) = delete;

    llama_model *g_model = nullptr;
    std::mutex g_mutex;

    // Constants
    const int N_CTX = 1024;
    const int MAX_GEN = 256;
};

#endif // ENGINE_MANAGER_H