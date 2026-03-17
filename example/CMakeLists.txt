cmake_minimum_required(VERSION 3.16)
project(elfowl_example C)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI REQUIRED)

add_library(owl_seed SHARED native/owl_seed.c)
set_target_properties(owl_seed PROPERTIES OUTPUT_NAME owl_seed)

add_library(owl_branch SHARED native/owl_branch.c)
target_link_libraries(owl_branch PRIVATE owl_seed)
set_target_properties(owl_branch PROPERTIES OUTPUT_NAME owl_branch)

add_library(owl_leaf SHARED native/owl_leaf.c)
target_link_libraries(owl_leaf PRIVATE owl_branch)
set_target_properties(owl_leaf PROPERTIES OUTPUT_NAME owl_leaf)

add_library(owl_song SHARED native/owl_song.c)
target_link_libraries(owl_song PRIVATE owl_seed owl_leaf)
set_target_properties(owl_song PROPERTIES OUTPUT_NAME owl_song)

add_library(owl_bridge SHARED native/owl_bridge.c)
target_link_libraries(owl_bridge PRIVATE owl_branch)
set_target_properties(owl_bridge PROPERTIES OUTPUT_NAME owl_bridge)

add_library(owl_entry SHARED native/owl_entry.c)
target_include_directories(owl_entry PRIVATE ${JNI_INCLUDE_DIRS})
target_link_libraries(owl_entry PRIVATE owl_song owl_bridge)
set_target_properties(owl_entry PROPERTIES OUTPUT_NAME owl_entry)

if(UNIX AND NOT APPLE)
    foreach(target owl_seed owl_branch owl_leaf owl_song owl_bridge owl_entry)
        target_link_options(${target} PRIVATE "-Wl,-rpath,$ORIGIN")
    endforeach()
endif()
