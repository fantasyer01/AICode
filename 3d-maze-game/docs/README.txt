# 3D Maze Explorer

A 3D maze exploration game built with OpenGL, featuring first-person navigation and a 2D minimap overlay.

## Features

- **3D First-Person Maze**: Navigate through a procedurally generated 3D maze
- **Intuitive Controls**: WASD movement with mouse-look camera control
- **2D Minimap**: Real-time overhead view showing your position and the maze layout
- **Collision Detection**: Realistic wall collision with smooth sliding
- **Victory Condition**: Find the exit (marked in red) to win
- **OpenGL Rendering**: Hardware-accelerated 3D graphics

## System Requirements

### Minimum Requirements
- **Operating System**: Windows 7 or higher (64-bit recommended)
- **Processor**: Dual-core CPU, 2.0 GHz
- **Memory**: 2 GB RAM
- **Graphics**: GPU with OpenGL 3.3 support
- **Storage**: 50 MB available space
- **Display**: 1024x768 resolution minimum

### Recommended Requirements
- **Operating System**: Windows 10/11 64-bit
- **Processor**: Quad-core CPU, 3.0 GHz
- **Memory**: 4 GB RAM
- **Graphics**: Dedicated GPU with OpenGL 4.0+ support
- **Display**: 1920x1080 resolution

## Quick Start

### For Users (Pre-built Binary)

1. Ensure your graphics drivers are up to date
2. Navigate to the `build` directory
3. Double-click `MazeGame.exe` to run
4. The game window will open showing the menu
5. Press **ENTER** to start the game
6. Use **W/A/S/D** to move and **mouse** to look around
7. Find the red exit marker to complete the maze
8. Press **ESC** to return to menu or quit

### For Developers

See `BUILD.txt` for detailed build instructions from source.

## Controls

### Menu State
- **ENTER**: Start new game
- **ESC**: Exit application

### Gameplay State
- **W**: Move forward
- **S**: Move backward
- **A**: Strafe left
- **D**: Strafe right
- **Mouse Movement**: Look around (rotate camera)
- **ESC**: Return to menu

### Victory State
- **N**: Start new game (new maze)
- **M**: Return to main menu
- **ESC**: Exit application

## Gameplay Tips

1. **Use the Minimap**: The 2D minimap in the upper-left corner shows the entire maze layout, your position (green), and the exit (red)
2. **Plan Your Route**: Before diving in, check the minimap to find a path to the exit
3. **Wall Sliding**: When moving diagonally into a wall, you'll slide along it for smoother navigation
4. **Camera Control**: Adjust your mouse sensitivity by moving slower or faster as needed

## Troubleshooting

### Application Fails to Start

**Symptom**: Double-clicking the executable does nothing or shows an error.

**Solutions**:
- Ensure all DLL files are in the same directory as the executable
- Install Visual C++ Redistributable (if using MSVC build)
- Check that your system meets minimum requirements

### Black Screen or No Display

**Symptom**: Window opens but shows only black screen.

**Solutions**:
- Update your graphics drivers to the latest version
- Verify your GPU supports OpenGL 3.3 or higher
- Try running as administrator

### Poor Performance / Low FPS

**Symptom**: Game runs slowly or stutters.

**Solutions**:
- Close other applications to free up resources
- Lower your display resolution
- If using integrated graphics, switch to dedicated GPU in graphics settings
- Update graphics drivers

### Controls Not Responding

**Symptom**: Keyboard or mouse input doesn't work.

**Solutions**:
- Click on the game window to ensure it has focus
- Check that no other application is capturing input
- Try pressing ESC to return to menu, then ENTER to restart

### Mouse Cursor Visible During Gameplay

**Symptom**: Cursor is visible when it should be hidden.

**Solutions**:
- This is normal in menu and victory states
- During gameplay, the cursor should be hidden automatically
- If visible during gameplay, try pressing ESC then ENTER to restart

## Technical Details

- **Graphics API**: OpenGL 3.3 Core Profile
- **Window System**: GLFW 3.x
- **Math Library**: GLM (OpenGL Mathematics)
- **Maze Algorithm**: Depth-First Search (DFS) with backtracking
- **Collision**: Circle-AABB intersection with spatial optimization
- **Rendering**: Forward rendering with vertex and fragment shaders

## Project Structure

```
3d-maze-game/
├── build/              # Build output directory
├── docs/               # Documentation
├── include/            # Header files
├── lib/                # External libraries (GLFW, GLAD, GLM)
├── shaders/            # GLSL shader source files
├── src/                # C++ source files
└── CMakeLists.txt      # CMake configuration
```

## License

This project is provided as-is for educational and entertainment purposes.

## Credits

Developed using:
- GLFW for window management
- GLAD for OpenGL function loading
- GLM for mathematics
- OpenGL for graphics rendering

## Version

**Version**: 1.0.0  
**Build Date**: November 2025  
**Platform**: Windows

## Support

For build instructions, see `BUILD.txt`  
For control reference, see `CONTROLS.txt`

---

Enjoy exploring the maze!
