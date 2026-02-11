cmake_minimum_required(VERSION 3.22.1)

project("cutthenoise")

# 1. Configure llama.cpp flags (Disable clutter)
# We set these BEFORE adding the subdirectory so it knows not to build junk.
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)

# 2. Build the library using its own build system
# This automatically handles GGML_VERSION, CPU flags, and include paths.
add_subdirectory(libs/llama.cpp)

# 3. Define YOUR library (The Bridge + Core)
add_library(
        llm
        SHARED
        jni/native_bridge.cpp
        core/engine_manager.cpp
)

# 4. Link Dependencies
# Linking against 'llama' automatically pulls in the correct include headers
# and compiler definitions managed by the submodule.
target_link_libraries(
        llm
        llama
        android
        log
)