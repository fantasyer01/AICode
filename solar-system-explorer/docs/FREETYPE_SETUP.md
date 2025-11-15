# FreeType Setup Guide for Solar System Explorer

## Option 1: Download Pre-built FreeType for MinGW (Recommended)

1. Download FreeType from: https://github.com/ubawurinna/freetype-windows-binaries
   - Get the latest release (freetype-windows-binaries-X.X.X.zip)
   - Or use the release files from the releases page

2. Extract the archive

3. Copy files to the project:
   ```
   freetype-windows-binaries/
   ├── include/
   │   └── freetype2/     -> Copy to solar-system-explorer/lib/freetype/include/
   └── release dll/
       └── win64/
           └── freetype.dll  -> Copy to solar-system-explorer/build/
   └── release static/
       └── win64/
           └── freetype.lib  -> Copy to solar-system-explorer/lib/freetype/lib/
   ```

## Option 2: Use MSYS2 Package Manager

If you have MSYS2 installed at D:/msys64/:

```bash
pacman -S mingw-w64-x86_64-freetype
```

Then copy from MSYS2:
- Headers: D:/msys64/mingw64/include/freetype2/ -> lib/freetype/include/
- Library: D:/msys64/mingw64/lib/libfreetype.a -> lib/freetype/lib/

## Directory Structure After Setup

```
solar-system-explorer/
├── lib/
│   └── freetype/
│       ├── include/
│       │   └── freetype/
│       │       ├── freetype.h
│       │       ├── ftglyph.h
│       │       └── ... (other headers)
│       └── lib/
│           └── libfreetype.a  (or freetype.lib)
```

## Font File

You'll also need a TrueType font file. Windows fonts work great:
- Copy `C:\Windows\Fonts\arial.ttf` to `solar-system-explorer/data/fonts/arial.ttf`

Or download a free font like Roboto:
- https://fonts.google.com/specimen/Roboto

## Next Steps

Once you have FreeType set up, I'll update the code to use it for text rendering.

**Please let me know when you have FreeType downloaded, and I'll proceed with the implementation!**
