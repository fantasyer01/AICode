# VSCode Setup Checklist - 3D Maze Explorer

Use this checklist to ensure everything is properly set up before building.

## ☐ Phase 1: Software Installation

### CMake
- [ ] Downloaded CMake from https://cmake.org/download/
- [ ] Installed with "Add to PATH" option checked
- [ ] Verified: `cmake --version` shows version in PowerShell
- [ ] Restarted PowerShell/VSCode after installation

### MinGW-w64 Compiler
- [ ] Downloaded MSYS2 from https://www.msys2.org/
- [ ] Installed to C:\msys64
- [ ] Ran `pacman -Syu` to update
- [ ] Ran `pacman -S mingw-w64-x86_64-gcc`
- [ ] Ran `pacman -S mingw-w64-x86_64-gdb`
- [ ] Added `C:\msys64\mingw64\bin` to system PATH
- [ ] Verified: `g++ --version` shows GCC version
- [ ] Verified: `gdb --version` shows debugger version

### Visual Studio Code
- [ ] Downloaded VSCode from https://code.visualstudio.com/
- [ ] Installed with default options
- [ ] Launched successfully

### VSCode Extensions
- [ ] Installed: **C/C++** (Microsoft)
- [ ] Installed: **CMake Tools** (Microsoft)
- [ ] Installed: **CMake** (twxs)
- [ ] All extensions activated (check Extensions panel)

---

## ☐ Phase 2: Library Setup

### GLFW Library
- [ ] Downloaded from https://www.glfw.org/download.html
- [ ] Extracted ZIP file
- [ ] Created folder: `lib\glfw\include`
- [ ] Copied: `glfw-x.x.x\include\GLFW\` → `lib\glfw\include\GLFW\`
- [ ] Created folder: `lib\glfw\lib-mingw-w64`
- [ ] Copied: `glfw-x.x.x\lib-mingw-w64\` → `lib\glfw\lib-mingw-w64\`
- [ ] Verified: `lib\glfw\include\GLFW\glfw3.h` exists
- [ ] Verified: `lib\glfw\lib-mingw-w64\libglfw3.a` exists

### GLAD Library
- [ ] Generated at https://glad.dav1d.de/ (OpenGL 3.3+, Core, C/C++)
- [ ] Downloaded ZIP file
- [ ] Extracted ZIP file
- [ ] Created folder: `lib\glad\include`
- [ ] Copied: `glad\include\glad\` → `lib\glad\include\glad\`
- [ ] Copied: `glad\include\KHR\` → `lib\glad\include\KHR\`
- [ ] **CRITICAL**: Copied `glad\src\glad.c` → `src\glad.c`
- [ ] Verified: `lib\glad\include\glad\glad.h` exists
- [ ] Verified: `lib\glad\include\KHR\khrplatform.h` exists
- [ ] Verified: `src\glad.c` exists

### GLM Library
- [ ] Downloaded from https://github.com/g-truc/glm/releases
- [ ] Extracted ZIP file
- [ ] Created folder: `lib\glm`
- [ ] Copied: `glm-x.x.x\glm\` → `lib\glm\glm\`
- [ ] Verified: `lib\glm\glm\glm.hpp` exists

### Library Verification
- [ ] Ran verification script (see below)
- [ ] All libraries show ✓ green checkmarks

**Verification Script:**
```powershell
$files = @(
    "lib\glfw\include\GLFW\glfw3.h",
    "lib\glfw\lib-mingw-w64\libglfw3.a",
    "lib\glad\include\glad\glad.h",
    "lib\glad\include\KHR\khrplatform.h",
    "lib\glm\glm\glm.hpp",
    "src\glad.c"
)
foreach ($f in $files) {
    if (Test-Path $f) { Write-Host "✓ $f" -ForegroundColor Green }
    else { Write-Host "✗ $f MISSING" -ForegroundColor Red }
}
```

---

## ☐ Phase 3: VSCode Project Setup

### Open Project
- [ ] Launched VSCode
- [ ] Clicked File → Open Folder
- [ ] Selected `D:\code\AICode\3d-maze-game`
- [ ] Clicked "Yes, I trust the authors" if prompted

### VSCode Configuration
- [ ] `.vscode` folder exists with 5 files
- [ ] `settings.json` exists
- [ ] `tasks.json` exists
- [ ] `launch.json` exists
- [ ] `library-download-guide.md` exists
- [ ] `README.md` exists in `.vscode` folder

### CMake Kit Selection
- [ ] CMake Tools extension activated
- [ ] Clicked status bar to select kit
- [ ] Selected "GCC x.x.x mingw64" kit
- [ ] Or ran: `Ctrl+Shift+P` → "CMake: Select a Kit"

---

## ☐ Phase 4: Build and Test

### Configure
- [ ] CMake configured automatically, OR
- [ ] Manually ran: `Ctrl+Shift+P` → "CMake: Configure"
- [ ] Output shows "Configuring project: 3d-maze-game - done"
- [ ] No errors in Output panel

### Build
- [ ] Pressed `F7` to build, OR
- [ ] Pressed `Ctrl+Shift+B`, OR
- [ ] Clicked "Build" in status bar
- [ ] Build completes: `[100%] Built target MazeGame`
- [ ] No errors in Problems panel
- [ ] File `build\MazeGame.exe` created

### First Run
- [ ] Pressed `Shift+F5` to run, OR
- [ ] Clicked run button (▶) in status bar
- [ ] Game window opens (1024x768)
- [ ] Console shows welcome message
- [ ] Background is dark blue-gray (menu state)

### Gameplay Test
- [ ] Clicked game window to focus
- [ ] Pressed ENTER key
- [ ] Maze loads, mouse cursor disappears
- [ ] Minimap visible in upper-left corner
- [ ] Can move with W/A/S/D
- [ ] Can look around with mouse
- [ ] Can see walls (gray color)
- [ ] Green dot (player) visible on minimap
- [ ] Red dot (exit) visible on minimap
- [ ] Pressing ESC returns to menu

---

## ☐ Phase 5: Debug Setup (Optional)

### Debug Configuration
- [ ] Set breakpoint (click left of line number)
- [ ] Pressed `F5` to start debugging
- [ ] Debugger launches successfully
- [ ] Can step through code
- [ ] Variables show in Debug panel

---

## ☐ Troubleshooting (If Issues)

### If Build Fails
- [ ] Checked all libraries are in place (Phase 2)
- [ ] Verified `src\glad.c` exists
- [ ] Ran clean: `Ctrl+Shift+P` → Tasks: Run Task → CMake: Clean
- [ ] Reconfigured: `Ctrl+Shift+P` → CMake: Delete Cache and Reconfigure
- [ ] Rebuilt: `F7`

### If Run Fails
- [ ] Updated graphics drivers
- [ ] Verified OpenGL 3.3 support
- [ ] Checked `build\shaders\` folder exists
- [ ] Checked `build\shaders\vertex.glsl` exists
- [ ] Checked `build\shaders\fragment.glsl` exists

---

## ✅ Success Criteria

You're ready to develop when:
- ✅ `build\MazeGame.exe` exists
- ✅ Game launches without errors
- ✅ Can see menu screen
- ✅ Can start game with ENTER
- ✅ Can move and look around
- ✅ Minimap displays correctly
- ✅ No error messages in console

---

## 📚 Quick Reference

**Build**: `F7`  
**Run**: `Shift+F5`  
**Debug**: `F5`  
**Clean**: `Ctrl+Shift+P` → Tasks → CMake: Clean  
**Configure**: `Ctrl+Shift+P` → CMake: Configure  

**Full Guide**: `VSCODE_BUILD_GUIDE.md`  
**Quick Ref**: `VSCODE_QUICK_REF.md`  
**Library Help**: `.vscode\library-download-guide.md`  

---

## 🎯 Next Steps After Setup

1. **Play the game** to understand it
2. **Explore the code** in VSCode
3. **Modify settings** (maze size, colors, etc.)
4. **Add features** (timer, score, etc.)
5. **Build in Release** for better performance:
   - Change `settings.json`: `CMAKE_BUILD_TYPE: "Release"`
   - Rebuild

---

## 📞 Need Help?

- **Build issues**: See `docs\BUILD.txt`
- **VSCode setup**: See `VSCODE_BUILD_GUIDE.md`
- **Library problems**: See `.vscode\library-download-guide.md`
- **Gameplay**: See `docs\CONTROLS.txt`
- **Technical**: See `IMPLEMENTATION_SUMMARY.txt`

---

**Print this checklist and check off items as you complete them!** ✓
