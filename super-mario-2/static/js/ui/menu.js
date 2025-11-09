// Menu System - Manages all game menus
export class MenuSystem {
    constructor(gameEngine) {
        this.gameEngine = gameEngine;

        // Menu elements
        this.loadingScreen = document.getElementById('loading-screen');
        this.mainMenu = document.getElementById('main-menu');
        this.pauseMenu = document.getElementById('pause-menu');
        this.gameOverScreen = document.getElementById('gameover-screen');
        this.levelCompleteScreen = document.getElementById('levelcomplete-screen');
        this.victoryScreen = document.getElementById('victory-screen');
        this.instructionsPanel = document.getElementById('instructions-panel');

        // Bind button events
        this.bindEvents();
    }

    bindEvents() {
        // Main menu
        document.getElementById('start-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.gameEngine.startGame();
        });

        document.getElementById('instructions-btn').addEventListener('click', () => {
            this.showInstructions();
        });

        document.getElementById('back-btn').addEventListener('click', () => {
            this.hideInstructions();
        });

        // Pause menu
        document.getElementById('resume-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.gameEngine.resumeGame();
        });

        document.getElementById('restart-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.gameEngine.restartLevel();
        });

        document.getElementById('quit-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.showMainMenu();
            this.gameEngine.quitToMenu();
        });

        // Game over screen
        document.getElementById('tryagain-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.gameEngine.restartGame();
        });

        document.getElementById('mainmenu-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.showMainMenu();
            this.gameEngine.quitToMenu();
        });

        // Level complete screen
        document.getElementById('nextlevel-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.gameEngine.loadNextLevel();
        });

        // Victory screen
        document.getElementById('victory-menu-btn').addEventListener('click', () => {
            this.hideAllMenus();
            this.showMainMenu();
            this.gameEngine.quitToMenu();
        });
    }

    hideAllMenus() {
        this.loadingScreen.classList.add('hidden');
        this.mainMenu.classList.add('hidden');
        this.pauseMenu.classList.add('hidden');
        this.gameOverScreen.classList.add('hidden');
        this.levelCompleteScreen.classList.add('hidden');
        this.victoryScreen.classList.add('hidden');
    }

    showLoadingScreen() {
        this.hideAllMenus();
        this.loadingScreen.classList.remove('hidden');
    }

    updateLoadingProgress(percent) {
        const progressBar = document.getElementById('loading-progress');
        progressBar.style.width = percent + '%';
    }

    showMainMenu() {
        this.hideAllMenus();
        this.mainMenu.classList.remove('hidden');
        this.hideInstructions();
    }

    showInstructions() {
        this.instructionsPanel.classList.remove('hidden');
        document.getElementById('start-btn').style.display = 'none';
        document.getElementById('instructions-btn').style.display = 'none';
    }

    hideInstructions() {
        this.instructionsPanel.classList.add('hidden');
        document.getElementById('start-btn').style.display = 'block';
        document.getElementById('instructions-btn').style.display = 'block';
    }

    showPauseMenu() {
        this.hideAllMenus();
        this.pauseMenu.classList.remove('hidden');
    }

    showGameOver(score) {
        this.hideAllMenus();
        document.getElementById('final-score').textContent = score.toString().padStart(6, '0');
        this.gameOverScreen.classList.remove('hidden');
    }

    showLevelComplete(score) {
        this.hideAllMenus();
        document.getElementById('level-score').textContent = score.toString().padStart(6, '0');
        this.levelCompleteScreen.classList.remove('hidden');
    }

    showVictory(score) {
        this.hideAllMenus();
        document.getElementById('victory-score').textContent = score.toString().padStart(6, '0');
        this.victoryScreen.classList.remove('hidden');
    }
}
