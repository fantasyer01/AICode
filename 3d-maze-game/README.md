# 3D Maze Explorer

A 3D first-person maze exploration game built with OpenGL, C++, and modern graphics programming techniques.

![Game Type](https://img.shields.io/badge/Type-3D%20Game-blue)
![Language](https://img.shields.io/badge/Language-C%2B%2B-orange)
![Graphics](https://img.shields.io/badge/Graphics-OpenGL%203.3-green)
![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey)

## Overview

3D Maze Explorer is an immersive first-person maze navigation game featuring:
- **Procedurally generated mazes** using Depth-First Search algorithm
- **3D perspective rendering** with OpenGL 3.3+
- **Real-time 2D minimap** overlay for strategic navigation
- **Physics-based collision detection** with smooth wall sliding
- **Mouse and keyboard controls** for fluid movement

## Screenshots

```
[Menu Screen] → [3D Maze View with Minimap] → [Victory Screen]
```

## Key Features

### 🎮 Gameplay
- Navigate through a randomly generated 3D maze from first-person perspective
- Find the exit marker (red cube) to complete the level
- Smooth WASD + mouse controls for movement and camera
- No time limits - explore at your own pace

### 🗺️ 2D Minimap
- Always-visible top-down view in upper-left corner
- See entire maze layout, your position, and exit location
- Green marker = Player position
- Red marker = Exit location
- White lines = Walls

### 🎨 Graphics
- OpenGL 3.3 Core Profile rendering
- Vertex and fragment shaders for efficient rendering
- Perspective projection for 3D scene
- Orthographic projection for 2D minimap overlay
- Depth testing for proper 3D rendering

### 🧱 Collision System
- Circle-AABB collision detection
- Spatial partitioning for performance
- Smooth wall sliding behavior
- No wall clipping or falling through floors

## Quick Start

### Option 1: Run Pre-built Binary (Recommended for Players)

1. Download the latest release
2. Extract to a folder
3. Double-click `MazeGame.exe`
4. Press ENTER to start
5. Use W/A/S/D to move, mouse to look
6. Find the red exit!

### Option 2: Build from Source (For Developers)

**Prerequisites:**
- CMake 3.10+
- C++ compiler (MinGW-w64 or MSVC)
- OpenGL 3.3 compatible GPU

**Step 1: Get the Libraries**

See `LIBRARIES_SETUP.txt` for detailed instructions. You need:
- GLFW 3.x (window management)
- GLAD (OpenGL loader)
- GLM (mathematics)

**Step 2: Build**

```powershell
# Navigate to project directory
cd 3d-maze-game

# Create build directory
mkdir build
cd build

# Configure (MinGW)
cmake -G "MinGW Makefiles" ..

# Or configure (Visual Studio 2022)
cmake -G "Visual Studio 17 2022" -A x64 ..

# Build
cmake --build .

# Run
.\MazeGame.exe
```

For detailed build instructions, see `docs/BUILD.txt`.

## Documentation

- **README.txt** - User manual and troubleshooting
- **BUILD.txt** - Complete build instructions for developers
- **CONTROLS.txt** - Comprehensive control reference
- **LIBRARIES_SETUP.txt** - Library download and setup guide

## Controls

| Action | Control |
|--------|---------|
| Move Forward/Backward | W / S |
| Strafe Left/Right | A / D |
| Look Around | Mouse |
| Start Game (Menu) | ENTER |
| Return to Menu | ESC |
| New Game (Victory) | N |

See `docs/CONTROLS.txt` for complete reference.

## Project Structure

```
3d-maze-game/
├── CMakeLists.txt           # Build configuration
├── LIBRARIES_SETUP.txt      # Library setup guide
├── README.md                # This file
├── docs/                    # User documentation
│   ├── README.txt
│   ├── BUILD.txt
│   └── CONTROLS.txt
├── include/                 # Header files
│   ├── Collision.h          # Collision detection system
│   ├── Game.h               # Game state manager
│   ├── Maze.h               # Maze generation and data
│   ├── Player.h             # Player controller
│   └── Renderer.h           # OpenGL rendering
├── src/                     # C++ source files
│   ├── Collision.cpp
│   ├── Game.cpp
│   ├── Maze.cpp
│   ├── Player.cpp
│   ├── Renderer.cpp
│   ├── main.cpp
│   └── glad.c               # (from GLAD download)
├── shaders/                 # GLSL shaders
│   ├── vertex.glsl          # Vertex shader
│   └── fragment.glsl        # Fragment shader
└── lib/                     # External libraries (not included)
    ├── glfw/                # Window management
    ├── glad/                # OpenGL loader
    └── glm/                 # Mathematics
```

## Technical Details

### Architecture

The game follows a modular object-oriented design:

- **Game**: Main game loop and state management (Menu, Playing, Victory)
- **Maze**: DFS-based procedural maze generation
- **Player**: First-person camera controller with WASD movement
- **Renderer**: OpenGL rendering pipeline (3D scene + 2D minimap)
- **CollisionSystem**: Circle-AABB intersection with spatial optimization

### Maze Generation

Uses **Depth-First Search (DFS) with backtracking**:
1. Start with a grid of cells, all walls present
2. Pick random starting cell, mark as visited
3. Choose random unvisited neighbor, remove wall between them
4. Recursively visit that neighbor
5. Backtrack when no unvisited neighbors remain
6. Results in a perfect maze (exactly one path between any two points)

### Rendering Pipeline

**3D Scene:**
1. Clear color and depth buffers
2. Set perspective projection matrix
3. Set view matrix from player camera
4. Render floor, walls, ceiling, exit marker
5. Use vertex/fragment shaders for transformation and coloring

**2D Minimap:**
1. Save viewport, switch to upper-left corner
2. Set orthographic projection
3. Render maze walls as 2D quads
4. Render player and exit markers
5. Restore viewport

### Performance

- **Target Frame Rate**: 60 FPS
- **Typical Performance**: 60+ FPS on modern hardware
- **Maze Generation**: < 100ms for 15x15 grid
- **Memory Usage**: < 100MB
- **GPU Requirements**: OpenGL 3.3 support (2010+ hardware)

## System Requirements

**Minimum:**
- Windows 7 64-bit
- Dual-core CPU, 2.0 GHz
- 2 GB RAM
- GPU with OpenGL 3.3 support
- 50 MB disk space

**Recommended:**
- Windows 10/11 64-bit
- Quad-core CPU, 3.0 GHz
- 4 GB RAM
- Dedicated GPU with OpenGL 4.0+
- 100 MB disk space

## Technologies Used

| Component | Technology | Purpose |
|-----------|------------|---------|
| Programming Language | C++11 | Core game logic |
| Graphics API | OpenGL 3.3 Core | 3D rendering |
| Window Management | GLFW 3.x | Window creation, input handling |
| OpenGL Loader | GLAD | Load OpenGL functions |
| Math Library | GLM | Vector/matrix operations |
| Build System | CMake 3.10+ | Cross-platform builds |
| Shading Language | GLSL 330 | Vertex/fragment shaders |

## Development

### Building

See `docs/BUILD.txt` for comprehensive build instructions.

### Code Style

- C++11 standard
- Object-oriented design with clear separation of concerns
- Header files in `include/`, implementations in `src/`
- Shader code in `shaders/` directory
- CMake for build configuration

### Testing

Manual testing recommended:
- Verify maze generation creates solvable mazes
- Test collision detection at various angles
- Confirm minimap accuracy
- Check performance on target hardware

## Known Limitations

- Text rendering not implemented (menu/victory use color backgrounds)
- Fixed maze size (15x15 cells)
- No sound/music
- Windows-only (could be ported to Linux/macOS with minor changes)
- No gamepad support
- Fixed controls (not customizable)

## Future Enhancements

Potential features for future versions:
- [ ] Text rendering for proper menu/UI
- [ ] Multiple difficulty levels (different maze sizes)
- [ ] Timer and scoring system
- [ ] Sound effects and background music
- [ ] Textured walls and floors
- [ ] Multiple maze themes/environments
- [ ] Save/load game state
- [ ] Leaderboard for fastest completions
- [ ] Gamepad controller support
- [ ] Customizable controls
- [ ] Cross-platform support (Linux, macOS)

## Troubleshooting

### Build Issues

**CMake can't find libraries:**
- Verify library folder structure (see LIBRARIES_SETUP.txt)
- Check paths in CMakeLists.txt
- Ensure glad.c is in src/ directory

**Compilation errors:**
- Verify C++ compiler is installed and in PATH
- Check CMake version (3.10+ required)
- Ensure all header files are present

### Runtime Issues

**Application won't start:**
- Update graphics drivers
- Verify OpenGL 3.3 support
- Check for missing DLL files

**Black screen:**
- Update graphics drivers
- Verify shaders directory copied to build folder
- Check OpenGL compatibility

See `docs/README.txt` for complete troubleshooting guide.

## License

This project is provided as-is for educational and entertainment purposes.

## Acknowledgments

- **GLFW** - Marcus Geelnard, Camilla Löwy, and contributors
- **GLAD** - David Herberth
- **GLM** - G-Truc Creation
- **LearnOpenGL** - Joey de Vries (tutorials and inspiration)

## Contact & Support

For issues, questions, or contributions:
1. Check documentation in `docs/` directory
2. Review build instructions in BUILD.txt
3. Verify library setup per LIBRARIES_SETUP.txt

## Version History

**v1.0.0** (November 2025)
- Initial release
- Core gameplay implementation
- 3D maze with first-person navigation
- 2D minimap overlay
- Collision detection
- Menu and victory states
- Complete documentation

---

**Enjoy exploring the maze!** 🎮🗺️
