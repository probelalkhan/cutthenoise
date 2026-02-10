cmake_minimum_required(VERSION 3.22.1)
project(cutthenoise)

# Disable everything we do not need
set(LLAMA_BUILD_TESTS OFF)
set(LLAMA_BUILD_EXAMPLES OFF)
set(LLAMA_BUILD_SERVER OFF)
set(LLAMA_BUILD_TOOLS OFF)

# Build llama.cpp properly
add_subdirectory(llama.cpp)

# Your JNI library
add_library(
        llm
        SHARED
        llm_jni.cpp
)

target_link_libraries(
        llm
        llama
        log
)
