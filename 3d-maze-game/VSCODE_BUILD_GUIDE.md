# Complete VSCode Build Guide - 3D Maze Explorer

This guide provides step-by-step instructions to compile and run the 3D Maze Explorer game in Visual Studio Code on Windows.

---

## Phase 1: Install Prerequisites (30 minutes)

### 1.1 Install Visual Studio Code
1. Download from https://code.visualstudio.com/
2. Run installer with default settings
3. Launch VSCode

### 1.2 Install VSCode Extensions
Open VSCode, press `Ctrl+Shift+X` to open Extensions, install:
- **C/C++** (Microsoft) - for C++ IntelliSense
- **CMake Tools** (Microsoft) - for CMake integration
- **CMake** (twxs) - for CMake syntax highlighting

### 1.3 Install CMake
1. Download: https://cmake.org/download/
   - Get "Windows x64 Installer" (cmake-3.x.x-windows-x86_64.msi)
2. Run installer
3. **IMPORTANT**: Check ✅ "Add CMake to system PATH for all users"
4. Complete installation
5. **Verify**: Open PowerShell and run:
   ```powershell
   cmake --version
   ```
   Should show: `cmake version 3.x.x`

### 1.4 Install MinGW-w64 Compiler
1. Download MSYS2: https://www.msys2.org/
   - Get "msys2-x86_64-xxxxxxxx.exe"
2. Run installer, install to `C:\msys64` (default)
3. After installation, MSYS2 terminal opens automatically
4. Update package database:
   ```bash
   pacman -Syu
   ```
   Press ENTER when prompted, close terminal when done
5. Reopen MSYS2 terminal, install compiler:
   ```bash
   pacman -S mingw-w64-x86_64-gcc
   pacman -S mingw-w64-x86_64-gdb
   ```
6. **Add to PATH**:
   - Open Windows Settings → System → About → Advanced system settings
   - Click "Environment Variables"
   - Under "System variables", find "Path", click "Edit"
   - Click "New", add: `C:\msys64\mingw64\bin`
   - Click OK on all dialogs
7. **Restart PowerShell/VSCode** for PATH changes to take effect
8. **Verify**: Open new PowerShell:
   ```powershell
   g++ --version
   gdb --version
   ```
   Both should show version information

---

## Phase 2: Download Libraries (20 minutes)

### 2.1 Create lib Directory Structure
Open PowerShell in project root (`D:\code\AICode\3d-maze-game`):
```powershell
New-Item -ItemType Directory -Force -Path "lib\glfw\include"
New-Item -ItemType Directory -Force -Path "lib\glfw\lib-mingw-w64"
New-Item -ItemType Directory -Force -Path "lib\glad\include"
New-Item -ItemType Directory -Force -Path "lib\glm"
```

### 2.2 Download and Setup GLFW
1. Open browser: https://www.glfw.org/download.html
2. Click **"Windows pre-compiled binaries"** (64-bit)
3. Download ZIP file (e.g., `glfw-3.3.9.bin.WIN64.zip`)
4. Extract ZIP to a temporary location
5. Copy files:
   ```
   From extracted folder:
   glfw-3.3.9.bin.WIN64/include/GLFW/
   → Copy to: D:\code\AICode\3d-maze-game\lib\glfw\include\GLFW\
   
   glfw-3.3.9.bin.WIN64/lib-mingw-w64/
   → Copy to: D:\code\AICode\3d-maze-game\lib\glfw\lib-mingw-w64\
   ```
6. **Verify**: `lib\glfw\include\GLFW\glfw3.h` should exist

### 2.3 Download and Setup GLAD
1. Open browser: https://glad.dav1d.de/
2. Configure:
   - Language: **C/C++**
   - Specification: **OpenGL**
   - API → gl: **Version 3.3** (or higher, e.g., 4.6)
   - Profile: **Core**
   - ✅ Check: **Generate a loader**
3. Click **GENERATE** button
4. Click **Download** (glad.zip)
5. Extract ZIP to temporary location
6. Copy files:
   ```
   From extracted glad folder:
   include/glad/ → Copy to: D:\code\AICode\3d-maze-game\lib\glad\include\glad\
   include/KHR/  → Copy to: D:\code\AICode\3d-maze-game\lib\glad\include\KHR\
   src/glad.c    → Copy to: D:\code\AICode\3d-maze-game\src\glad.c
   ```
7. **⚠️ CRITICAL**: Verify `src\glad.c` exists (needed for compilation!)

### 2.4 Download and Setup GLM
1. Open browser: https://github.com/g-truc/glm/releases
2. Download latest release ZIP (e.g., `glm-0.9.9.8.zip`)
3. Extract ZIP to temporary location
4. Copy folder:
   ```
   From extracted folder:
   glm-0.9.9.8/glm/ → Copy to: D:\code\AICode\3d-maze-game\lib\glm\glm\
   ```
5. **Verify**: `lib\glm\glm\glm.hpp` should exist

### 2.5 Verify Library Setup
Run this PowerShell script in project root:
```powershell
Write-Host "Checking library setup..." -ForegroundColor Cyan
$allGood = $true

$checks = @(
    "lib\glfw\include\GLFW\glfw3.h",
    "lib\glfw\lib-mingw-w64\libglfw3.a",
    "lib\glad\include\glad\glad.h",
    "lib\glad\include\KHR\khrplatform.h",
    "lib\glm\glm\glm.hpp",
    "src\glad.c"
)

foreach ($file in $checks) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        Write-Host "✗ $file MISSING!" -ForegroundColor Red
        $allGood = $false
    }
}

if ($allGood) {
    Write-Host "`nAll libraries are set up correctly!" -ForegroundColor Green
} else {
    Write-Host "`nSome libraries are missing. Please review setup." -ForegroundColor Red
}
```

---

## Phase 3: Open Project in VSCode

### 3.1 Open Folder
1. Launch VSCode
2. File → Open Folder...
3. Navigate to `D:\code\AICode\3d-maze-game`
4. Click "Select Folder"

### 3.2 Trust Workspace
- If prompted about trust, click "Yes, I trust the authors"

### 3.3 CMake Tools Extension Activation
1. VSCode may ask "Would you like to configure project 3d-maze-game?"
2. Click **"Yes"**
3. If asked to select a kit, choose **"GCC x.x.x mingw64"** (from MinGW)

---

## Phase 4: Configure and Build (5 minutes)

### Method 1: Using CMake Tools Extension (Easiest)

#### 4.1 Configure Project
1. Look at the bottom status bar in VSCode
2. Click the **CMake** icon or text showing "[No Kit Selected]"
3. Select **"GCC x.x.x mingw64"** from the list
4. CMake Tools will automatically configure
5. Wait for "Configuring project: 3d-maze-game - done" in Output panel

#### 4.2 Build Project
1. Look at bottom status bar
2. Click **"Build"** button (or hammer icon)
3. Or press **F7** keyboard shortcut
4. Or click status bar showing "[MazeGame]" → "Build"
5. Watch the Output panel for build progress
6. Should see: `[100%] Built target MazeGame`

#### 4.3 Run the Game
1. Press **Shift+F5** to run without debugging
2. Or bottom status bar → Click **"▶ Run"** icon next to "MazeGame"
3. Game window should open!

### Method 2: Using Tasks (Alternative)

#### Configure
1. Press `Ctrl+Shift+P` (Command Palette)
2. Type: "Tasks: Run Task"
3. Select: **"CMake: Configure"**
4. Wait for completion

#### Build
1. Press `Ctrl+Shift+B` (default build shortcut)
2. Or Command Palette → "Tasks: Run Build Task"
3. Select: **"CMake: Build"**
4. Wait for `[100%] Built target MazeGame`

#### Run
1. Press `Ctrl+Shift+P`
2. Type: "Tasks: Run Task"
3. Select: **"Build and Run"**
4. Game launches!

### Method 3: Using Integrated Terminal (Manual)

#### Configure and Build
1. Open Terminal in VSCode: `Ctrl+`` (backtick)
2. Make sure you're in project root
3. Run commands:
   ```powershell
   # Configure
   cmake -G "MinGW Makefiles" -S . -B build
   
   # Build
   cmake --build build
   
   # Run
   .\build\MazeGame.exe
   ```

---

## Phase 5: Verify and Play

### 5.1 First Run
1. Game window opens (1024x768)
2. Background is dark bluish-gray (menu state)
3. Console shows:
   ```
   === 3D Maze Explorer ===
   Press ENTER in menu to start game
   Controls:
     W/A/S/D - Move
     Mouse - Look around
     ESC - Menu/Quit
   ========================
   ```

### 5.2 Start Playing
1. Click on game window to focus
2. Press **ENTER** key
3. Background changes, you're now in the maze
4. Mouse cursor disappears (captured for camera)
5. Look around with mouse
6. Move with W/A/S/D
7. Check upper-left corner for minimap (green dot = you, red dot = exit)

### 5.3 Controls
- **W/A/S/D**: Move forward/left/backward/right
- **Mouse**: Look around
- **ESC**: Return to menu
- **N**: New game (when victory)
- **M**: Menu (when victory)

---

## Troubleshooting

### Build Errors

#### ❌ "CMake not found"
**Solution**: 
- Verify CMake is in PATH: `cmake --version`
- Restart VSCode after installing CMake
- Check Environment Variables → Path includes CMake bin folder

#### ❌ "No CMake kits available"
**Solution**:
- Press `Ctrl+Shift+P` → "CMake: Scan for Kits"
- Select MinGW kit when prompted
- If still not found, check MinGW installation and PATH

#### ❌ "Cannot find GLFW" or library errors
**Solution**:
- Verify library folder structure (see Phase 2.5)
- Run the verification script
- Ensure folder names match exactly (case-sensitive)

#### ❌ "glad.c: No such file"
**Solution**:
- **CRITICAL**: Copy `glad.c` from GLAD download to `src\glad.c`
- This file MUST exist for compilation
- Re-download GLAD if you lost the file

#### ❌ Linker errors "undefined reference to glCreateShader"
**Solution**:
- Ensure `glad.c` is in `src\` folder
- Clean and rebuild:
  ```powershell
  Remove-Item -Recurse -Force build
  cmake -G "MinGW Makefiles" -S . -B build
  cmake --build build
  ```

### Runtime Errors

#### ❌ "Missing DLL" error when running
**Solution**:
- Copy `C:\msys64\mingw64\bin\*.dll` files to build folder
- Or add `C:\msys64\mingw64\bin` to system PATH

#### ❌ Black screen or crash on launch
**Solution**:
- Update graphics drivers
- Verify OpenGL 3.3 support: Download OpenGL Extensions Viewer
- Check shaders folder copied to build directory

#### ❌ "Failed to initialize GLAD"
**Solution**:
- Graphics drivers outdated
- OpenGL not supported on your GPU
- Try updating GPU drivers from manufacturer website

---

## Keyboard Shortcuts Summary

| Action | Shortcut |
|--------|----------|
| Configure CMake | `Ctrl+Shift+P` → "CMake: Configure" |
| Build Project | `Ctrl+Shift+B` or `F7` |
| Run | `Shift+F5` |
| Debug | `F5` |
| Open Terminal | `Ctrl+`` |
| Command Palette | `Ctrl+Shift+P` |
| Clean Build | Tasks → "CMake: Clean" |

---

## Project Structure in VSCode

```
3d-maze-game/
├── .vscode/                     # VSCode configuration
│   ├── settings.json            # CMake and C++ settings
│   ├── tasks.json               # Build tasks
│   ├── launch.json              # Debug configuration
│   └── library-download-guide.md
├── src/                         # Source files (shown in Explorer)
│   ├── *.cpp
│   └── glad.c ⚠️ REQUIRED
├── include/                     # Headers
├── shaders/                     # GLSL shaders
├── lib/                         # External libraries
│   ├── glfw/
│   ├── glad/
│   └── glm/
├── build/                       # Build output (generated)
│   └── MazeGame.exe
└── CMakeLists.txt
```

---

## Development Workflow

### Daily Workflow
1. Open VSCode
2. Project auto-configures (if settings.json configured)
3. Make code changes
4. Press `Ctrl+Shift+B` to build
5. Press `Shift+F5` to run

### Clean Build
1. `Ctrl+Shift+P` → "Tasks: Run Task"
2. Select "CMake: Clean"
3. Then build normally (`Ctrl+Shift+B`)

### Debugging
1. Set breakpoints (click left of line numbers)
2. Press `F5` to start debugging
3. Use Debug toolbar to step through code

---

## Success Checklist

Before building, verify:
- ✅ CMake installed and in PATH
- ✅ MinGW GCC installed and in PATH
- ✅ VSCode extensions installed (C++, CMake Tools)
- ✅ All three libraries downloaded and placed correctly
- ✅ `src\glad.c` exists
- ✅ Project opened in VSCode as folder
- ✅ CMake kit selected (MinGW)

After successful build:
- ✅ `build\MazeGame.exe` exists
- ✅ `build\shaders\` folder exists
- ✅ No error messages in Output panel
- ✅ Game runs and shows menu

---

## Next Steps

1. **Customize**: Modify maze size in `src/Game.cpp` (line creating Maze)
2. **Experiment**: Change colors in `src/Renderer.cpp`
3. **Extend**: Add features like timer, score, etc.
4. **Share**: Build in Release mode for distribution

---

## Support Resources

- **VSCode CMake**: https://code.visualstudio.com/docs/cpp/cmake-linux
- **Project Documentation**: See `docs/` folder
- **Build Issues**: See `docs/BUILD.txt`
- **Controls**: See `docs/CONTROLS.txt`

---

**You're all set! Build the game and start exploring the maze! 🎮**
