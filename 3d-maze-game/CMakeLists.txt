cmake_minimum_required(VERSION 3.10)
project(MazeGame)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add source files (including glad.c)
file(GLOB SOURCES "src/*.cpp")
list(APPEND SOURCES "src/glad.c")

# Add executable
add_executable(MazeGame ${SOURCES})

# Include directories
target_include_directories(MazeGame PRIVATE
    ${CMAKE_SOURCE_DIR}/include
    ${CMAKE_SOURCE_DIR}/lib/glfw/include
    ${CMAKE_SOURCE_DIR}/lib/glad/include
    ${CMAKE_SOURCE_DIR}/lib/glm
)

# Link libraries
find_package(OpenGL REQUIRED)

# Detect compiler and link appropriate GLFW library
if(MINGW)
    target_link_libraries(MazeGame PRIVATE
        OpenGL::GL
        ${CMAKE_SOURCE_DIR}/lib/glfw/lib-mingw-w64/libglfw3.a
        gdi32
        user32
        kernel32
    )
elseif(MSVC)
    target_link_libraries(MazeGame PRIVATE
        OpenGL::GL
        ${CMAKE_SOURCE_DIR}/lib/glfw/lib-vc2019/glfw3.lib
        gdi32
        user32
        kernel32
    )
else()
    # Fallback for other compilers
    target_link_libraries(MazeGame PRIVATE
        OpenGL::GL
        ${CMAKE_SOURCE_DIR}/lib/glfw/lib-mingw-w64/libglfw3.a
        gdi32
        user32
        kernel32
    )
endif()

# Copy shaders to build directory
file(COPY ${CMAKE_SOURCE_DIR}/shaders DESTINATION ${CMAKE_BINARY_DIR})
