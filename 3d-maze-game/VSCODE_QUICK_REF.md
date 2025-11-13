# VSCode Quick Build Reference

## TL;DR - Fast Track

1. **Install** (if not done):
   - VSCode + Extensions (C++, CMake Tools)
   - CMake (add to PATH)
   - MinGW-w64 via MSYS2 (add to PATH)

2. **Download Libraries** (see `.vscode/library-download-guide.md`):
   - GLFW → `lib/glfw/`
   - GLAD → `lib/glad/` + copy `glad.c` to `src/`
   - GLM → `lib/glm/`

3. **Build in VSCode**:
   - Open folder: `D:\code\AICode\3d-maze-game`
   - Press `F7` (build)
   - Press `Shift+F5` (run)

---

## Essential Commands

### Configure
```powershell
cmake -G "MinGW Makefiles" -S . -B build
```

### Build
```powershell
cmake --build build
```

### Run
```powershell
.\build\MazeGame.exe
```

### Clean
```powershell
Remove-Item -Recurse -Force build
```

---

## VSCode Shortcuts

- `F7` - Build
- `Shift+F5` - Run without debugging
- `F5` - Debug
- `Ctrl+Shift+B` - Build task
- `Ctrl+`` - Toggle terminal
- `Ctrl+Shift+P` - Command palette

---

## Common Issues

**"CMake not found"**
→ Add CMake to PATH, restart VSCode

**"No kit selected"**
→ `Ctrl+Shift+P` → "CMake: Select a Kit" → Choose MinGW

**"Cannot find GLFW"**
→ Check `lib/glfw/include/GLFW/glfw3.h` exists

**"glad.c not found"**
→ Copy from GLAD download to `src/glad.c` (CRITICAL!)

**Game crashes on launch**
→ Update GPU drivers for OpenGL 3.3 support

---

## File Checklist

Before building, verify these exist:
```
lib/glfw/include/GLFW/glfw3.h
lib/glfw/lib-mingw-w64/libglfw3.a
lib/glad/include/glad/glad.h
lib/glm/glm/glm.hpp
src/glad.c  ⚠️ MUST EXIST
```

---

## Full Guide

See `VSCODE_BUILD_GUIDE.md` for complete step-by-step instructions.
