// Main - Game initialization and startup
import { GameEngine } from './engine/gameEngine.js';

// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('game-canvas');
    
    if (!canvas) {
        console.error('Canvas element not found!');
        return;
    }

    // Create and initialize game engine
    const game = new GameEngine(canvas);
    game.init();

    // Handle window resize
    window.addEventListener('resize', () => {
        game.renderer.resize();
    });

    // Make game accessible globally for debugging
    window.game = game;
});
