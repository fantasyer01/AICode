cmake_minimum_required(VERSION 3.10)
project(SolarSystemExplorer)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add source files (including glad.c)
file(GLOB SOURCES "src/*.cpp")
list(APPEND SOURCES "src/glad.c")

# Add executable (WIN32 flag hides console window on Windows)
if(WIN32)
    add_executable(SolarSystemExplorer WIN32 ${SOURCES})
else()
    add_executable(SolarSystemExplorer ${SOURCES})
endif()

# Include directories
target_include_directories(SolarSystemExplorer PRIVATE
    ${CMAKE_SOURCE_DIR}/include
    ${CMAKE_SOURCE_DIR}/lib/glfw/include
    ${CMAKE_SOURCE_DIR}/lib/glad/include
    ${CMAKE_SOURCE_DIR}/lib/glm
    ${CMAKE_SOURCE_DIR}/lib/freetype/include
)

# Link libraries
find_package(OpenGL REQUIRED)

# Detect compiler and link appropriate GLFW library
if(MINGW)
    target_link_libraries(SolarSystemExplorer PRIVATE
        OpenGL::GL
        ${CMAKE_SOURCE_DIR}/lib/glfw/lib-mingw-w64/libglfw3.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libfreetype.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libharfbuzz.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libfreetype.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libgraphite2.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libpng.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libz.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libbz2.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libbrotlidec.a
        ${CMAKE_SOURCE_DIR}/lib/freetype/lib/libbrotlicommon.a
        gdi32
        user32
        kernel32
        rpcrt4
        dwrite
        usp10
    )
elseif(MSVC)
    target_link_libraries(SolarSystemExplorer PRIVATE
        OpenGL::GL
        ${CMAKE_SOURCE_DIR}/lib/glfw/lib-vc2019/glfw3.lib
        gdi32
        user32
        kernel32
    )
else()
    # Fallback for other compilers
    target_link_libraries(SolarSystemExplorer PRIVATE
        OpenGL::GL
        ${CMAKE_SOURCE_DIR}/lib/glfw/lib-mingw-w64/libglfw3.a
        gdi32
        user32
        kernel32
    )
endif()

# Copy shaders to build directory
file(COPY ${CMAKE_SOURCE_DIR}/shaders DESTINATION ${CMAKE_BINARY_DIR})

# Copy data files to build directory
file(COPY ${CMAKE_SOURCE_DIR}/data DESTINATION ${CMAKE_BINARY_DIR})
