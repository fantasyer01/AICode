# Super Platformer Game - Quick Start Guide

## How to Run the Game

1. **Start the Flask Server:**
   ```bash
   cd D:\code\AICode\chinese-poetry-app
   python app.py
   ```

2. **Access the Game:**
   - Open your web browser
   - Navigate to: `http://localhost:5000/game`

3. **Alternative Access:**
   - Main poetry app: `http://localhost:5000/`
   - Test page (with game link): `http://localhost:5000/test`

## Game Controls

- **Arrow Left / A**: Move Left
- **Arrow Right / D**: Move Right  
- **Arrow Up / W / Spacebar**: Jump
- **P**: Pause Game
- **R**: Restart Level

## Gameplay

- Jump on enemies to defeat them
- Collect coins for points
- Reach the flag to complete each level
- You have 3 lives - avoid getting hit from the side!
- Complete 2 levels to win the game

## Features Implemented

✅ Player movement with physics (acceleration, friction)
✅ Variable jump height (hold jump button for higher jumps)
✅ 2 complete levels with different layouts
✅ Walker and stationary enemy types
✅ Collectible coins
✅ Score tracking and HUD display
✅ Camera system that follows the player
✅ Collision detection (platforms, enemies, collectibles)
✅ Menu system (main menu, pause, game over, level complete, victory)
✅ Loading screen
✅ Lives system with invincibility frames

## Technical Details

- **Technology Stack**: HTML5 Canvas, Vanilla JavaScript ES6+, Flask
- **Architecture**: Modular game engine with separate systems
- **Physics**: 60 FPS game loop with delta time calculations
- **Graphics**: Retro pixel art style with simple geometric shapes

Enjoy playing! 🎮
