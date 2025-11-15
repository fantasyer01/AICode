# Solar System Explorer

An interactive 3D educational game that simulates the solar system, allowing children to explore and learn about planets.

## Features

- **Free-Flight Navigation**: Control a spacecraft using WASD keys and mouse to explore the solar system
- **Planetary Orbits**: Watch all 8 planets orbit the Sun at different speeds
- **Interactive Learning**: Fly close to planets to trigger information cards with educational facts
- **Beautiful 3D Graphics**: Rendered using OpenGL with realistic lighting and a starfield background

## Controls

- **W** - Move forward
- **A** - Strafe left
- **S** - Move backward
- **D** - Strafe right
- **Mouse** - Look around
- **ESC** - Exit the application

## Building the Project

### Prerequisites

- CMake 3.10 or higher
- MinGW-w64 or MSVC compiler
- OpenGL 3.3+ support

### Build Instructions

1. Navigate to the project directory:
   ```
   cd solar-system-explorer
   ```

2. Configure CMake:
   ```
   cmake -G "MinGW Makefiles" -DCMAKE_MAKE_PROGRAM="D:/msys64/mingw64/bin/mingw32-make.exe" -DCMAKE_C_COMPILER="D:/msys64/mingw64/bin/gcc.exe" -DCMAKE_CXX_COMPILER="D:/msys64/mingw64/bin/g++.exe" -B build
   ```

3. Build the project:
   ```
   cmake --build build
   ```

4. Run the executable:
   ```
   build\SolarSystemExplorer.exe
   ```

## Educational Content

When you fly close to a planet, you'll see information including:
- Planet name and position in the solar system
- Physical characteristics (size, temperature)
- Orbital period
- Unique features

## Technical Details

- **Graphics API**: OpenGL 3.3
- **Libraries**: GLFW (window management), GLM (mathematics), GLAD (OpenGL loading)
- **Language**: C++11
- **Platform**: Windows (tested on Windows 10/11)

## Project Structure

```
solar-system-explorer/
├── include/          - Header files
├── src/              - Source files
├── shaders/          - GLSL shader files
├── data/             - Planet information data
├── lib/              - Third-party libraries
└── docs/             - Documentation
```

## Future Enhancements

- Text rendering for information cards using FreeType
- Moons for major planets
- Asteroid belt
- Saturn's rings visualization
- Interactive quiz mode
- Multiple language support
