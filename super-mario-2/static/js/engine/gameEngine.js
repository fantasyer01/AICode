// Game Engine - Core game loop and state management
import { EntityManager } from '../managers/entityManager.js';
import { LevelManager } from '../managers/levelManager.js';
import { InputHandler } from '../managers/inputHandler.js';
import { AudioManager } from '../managers/audioManager.js';
import { CollisionDetection } from './collisionDetection.js';
import { Renderer } from './renderer.js';
import { HUD } from '../ui/hud.js';
import { MenuSystem } from '../ui/menu.js';

export class GameEngine {
    constructor(canvas) {
        this.canvas = canvas;
        
        // Initialize managers
        this.entityManager = new EntityManager();
        this.levelManager = new LevelManager(this.entityManager);
        this.inputHandler = new InputHandler();
        this.audioManager = new AudioManager();
        this.collisionDetection = new CollisionDetection(this.entityManager);
        this.renderer = new Renderer(canvas, this.entityManager);
        this.hud = new HUD();
        this.menuSystem = new MenuSystem(this);

        // Game state
        this.gameState = {
            status: 'menu', // menu, loading, playing, paused, gameOver, levelComplete, victory
            score: 0,
            lives: 3,
            currentLevel: 0,
            levelName: 'LEVEL 1-1'
        };

        // Frame timing
        this.lastTime = 0;
        this.deltaTime = 0;
        this.animationId = null;

        // Pause handling
        this.previousPauseState = false;
    }

    init() {
        // Show loading screen
        this.menuSystem.showLoadingScreen();

        // Simulate asset loading
        let progress = 0;
        const loadInterval = setInterval(() => {
            progress += 10;
            this.menuSystem.updateLoadingProgress(progress);

            if (progress >= 100) {
                clearInterval(loadInterval);
                setTimeout(() => {
                    this.menuSystem.showMainMenu();
                    this.gameState.status = 'menu';
                }, 500);
            }
        }, 100);
    }

    startGame() {
        this.gameState.score = 0;
        this.gameState.lives = 3;
        this.gameState.currentLevel = 0;
        this.loadLevel(0);
        this.gameState.status = 'playing';
        this.hud.show();
        this.start();
    }

    restartGame() {
        this.gameState.score = 0;
        this.gameState.lives = 3;
        this.loadLevel(0);
        this.gameState.status = 'playing';
        this.hud.show();
        this.start();
    }

    loadLevel(levelIndex) {
        if (this.levelManager.loadLevel(levelIndex)) {
            const level = this.levelManager.getCurrentLevel();
            this.gameState.levelName = level.name;
            this.gameState.currentLevel = levelIndex;
            
            // Reset player lives to match game state
            if (this.entityManager.player) {
                this.entityManager.player.lives = this.gameState.lives;
            }
        }
    }

    restartLevel() {
        this.levelManager.restartLevel();
        const level = this.levelManager.getCurrentLevel();
        if (this.entityManager.player) {
            this.entityManager.player.lives = this.gameState.lives;
        }
        this.gameState.status = 'playing';
        this.start();
    }

    loadNextLevel() {
        if (this.levelManager.hasNextLevel()) {
            this.levelManager.loadNextLevel();
            const level = this.levelManager.getCurrentLevel();
            this.gameState.levelName = level.name;
            this.gameState.currentLevel++;
            this.gameState.status = 'playing';
            this.start();
        } else {
            this.gameState.status = 'victory';
            this.menuSystem.showVictory(this.gameState.score);
        }
    }

    pauseGame() {
        if (this.gameState.status === 'playing') {
            this.gameState.status = 'paused';
            this.menuSystem.showPauseMenu();
            this.stop();
        }
    }

    resumeGame() {
        if (this.gameState.status === 'paused') {
            this.gameState.status = 'playing';
            this.start();
        }
    }

    quitToMenu() {
        this.gameState.status = 'menu';
        this.hud.hide();
        this.stop();
    }

    start() {
        if (!this.animationId) {
            this.lastTime = performance.now();
            this.gameLoop();
        }
    }

    stop() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
    }

    gameLoop(currentTime = 0) {
        this.animationId = requestAnimationFrame(this.gameLoop.bind(this));

        // Calculate delta time
        this.deltaTime = currentTime - this.lastTime;
        this.lastTime = currentTime;

        // Update and render
        this.update(this.deltaTime);
        this.render();
    }

    update(deltaTime) {
        if (this.gameState.status !== 'playing') return;

        const level = this.levelManager.getCurrentLevel();
        if (!level) return;

        // Check pause input
        if (this.inputHandler.keys.pause && !this.previousPauseState) {
            this.pauseGame();
        }
        this.previousPauseState = this.inputHandler.keys.pause;

        // Check restart input
        if (this.inputHandler.keys.restart) {
            this.restartLevel();
            return;
        }

        // Update entities
        this.entityManager.update(this.inputHandler, deltaTime);

        // Check collisions
        this.collisionDetection.checkCollisions();

        // Check collectible collection and update score
        const scoreGained = this.collisionDetection.checkCollectibleCollisions(this.entityManager.player);
        if (scoreGained > 0) {
            this.gameState.score += scoreGained;
            this.audioManager.playSound('coin');
        }

        // Check goal reached
        if (this.collisionDetection.checkGoalCollision(this.entityManager.player)) {
            this.gameState.status = 'levelComplete';
            this.menuSystem.showLevelComplete(this.gameState.score);
            this.stop();
            return;
        }

        // Check if player is out of bounds
        if (this.collisionDetection.checkOutOfBounds(this.entityManager.player, level.height)) {
            this.entityManager.player.takeDamage();
            if (this.entityManager.player.isAlive) {
                // Respawn player
                this.entityManager.player.reset(
                    level.playerStart.x,
                    level.playerStart.y
                );
            }
        }

        // Update game state from player
        if (this.entityManager.player) {
            this.gameState.lives = this.entityManager.player.lives;

            // Check game over
            if (!this.entityManager.player.isAlive) {
                this.gameState.status = 'gameOver';
                this.menuSystem.showGameOver(this.gameState.score);
                this.stop();
                return;
            }
        }

        // Update HUD
        this.hud.update(this.gameState);

        // Update input handler
        this.inputHandler.update();
    }

    render() {
        const level = this.levelManager.getCurrentLevel();
        if (!level) return;

        // Update camera
        this.renderer.updateCamera(level);

        // Render scene
        this.renderer.render(level);
    }
}
