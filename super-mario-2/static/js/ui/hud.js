// HUD System - Manages heads-up display
export class HUD {
    constructor() {
        this.scoreElement = document.getElementById('score-value');
        this.livesElement = document.getElementById('lives-value');
        this.levelNameElement = document.getElementById('level-name');
        this.hudContainer = document.getElementById('hud');
    }

    show() {
        this.hudContainer.classList.remove('hidden');
    }

    hide() {
        this.hudContainer.classList.add('hidden');
    }

    updateScore(score) {
        this.scoreElement.textContent = score.toString().padStart(6, '0');
    }

    updateLives(lives) {
        this.livesElement.textContent = lives;
    }

    updateLevel(levelName) {
        this.levelNameElement.textContent = levelName;
    }

    update(gameState) {
        this.updateScore(gameState.score);
        this.updateLives(gameState.lives);
        this.updateLevel(gameState.levelName);
    }
}
