# Text Rendering in OpenGL - Explanation

## Current Implementation

The current menu system uses **colored rectangular boxes** to represent text. Each box represents one character position in the words "START GAME" and "EXIT GAME".

### Why Boxes Instead of Actual Text?

Rendering actual readable text in OpenGL requires one of these approaches:

1. **Bitmap/Raster Fonts** - Pre-rendered character images
2. **TrueType Fonts (TTF)** - Using libraries like FreeType
3. **Signed Distance Field (SDF) Fonts** - Advanced technique
4. **Geometric Letter Shapes** - Drawing each letter manually

All of these are complex and require either:
- Additional libraries (FreeType, STB_truetype)
- Pre-generated font texture atlases
- Hundreds of lines of code to render each letter geometrically

### Current Menu Representation

```
┌────────────────────────────────┐
│                                │
│  3D MAZE EXPLORER  (blue boxes)│
│                                │
│   ████████████████             │ ← These represent "START GAME"
│   ►                            │ ← Yellow arrow shows selection
│                                │
│   ████████████                 │ ← These represent "EXIT GAME"  
│                                │
│  USE ARROW KEYS (small boxes)  │
└────────────────────────────────┘
```

### How to Identify Menu Items

**Visual Cues:**
1. **Position**: Top row = "Start Game", Bottom row = "Exit Game"
2. **Yellow Arrow (►)**: Points to current selection
3. **Brightness**: Selected item is bright white, unselected is dim gray
4. **Console Output**: Clearly labels what each row represents

## To Add Actual Text Rendering

If you want to see actual readable letters like "Start Game" instead of boxes, you have these options:

### Option 1: Use FreeType Library (Recommended)

**Pros:**
- Professional text rendering
- Supports any TrueType font
- Clean, readable text

**Cons:**
- Requires installing FreeType library
- More complex setup
- Additional code (~200-300 lines)

**Steps:**
1. Download FreeType library
2. Add to CMakeLists.txt
3. Create texture atlas for font characters
4. Modify Renderer to use font textures

### Option 2: Use STB_truetype (Header-only)

**Pros:**
- Single header file (stb_truetype.h)
- No external dependencies
- Easier to integrate

**Cons:**
- Still requires font texture generation
- More manual work
- ~150-200 lines of code needed

### Option 3: Bitmap Font Atlas

**Pros:**
- Simple implementation
- Fast rendering
- No external dependencies

**Cons:**
- Requires pre-generated font image
- Fixed font size
- Less flexible

### Option 4: Keep Current System (Simplest)

**Pros:**
- Already implemented
- No dependencies
- Works immediately
- Console labels make it clear

**Cons:**
- Not traditional text rendering
- Boxes instead of letters

## Recommendation

For a simple maze game, the **current box-based system is sufficient** because:

1. ✅ The console clearly labels what each option is
2. ✅ Visual feedback (arrow, brightness) shows selection
3. ✅ Position makes it obvious (top vs bottom)
4. ✅ No complex dependencies needed
5. ✅ Works on any system with OpenGL

If you really want actual text, I recommend:
- **Quick solution**: Add more console output explaining the menu
- **Better solution**: Integrate STB_truetype (single header file)
- **Best solution**: Use FreeType library (professional quality)

## Current Code Comments

The code includes comments like:
```cpp
// Render "START GAME" - Using large boxes to simulate text
```

This makes it clear that the boxes **represent** the text "START GAME", even though they're not rendering actual letters.

## Console Output

The console output helps users understand the menu:
```
MENU GUIDE:
  The top row of boxes = 'START GAME'
  The bottom row of boxes = 'EXIT GAME'
  Yellow arrow (►) shows current selection
```

This bridges the gap between the visual representation and user understanding.

## Summary

- **Current**: Boxes represent text positions (simple, works)
- **Desired**: Actual readable letters (complex, needs library)
- **Solution**: Either accept current system OR integrate a text library

The choice depends on whether visual polish is worth the added complexity!
