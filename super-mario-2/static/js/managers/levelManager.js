// Level Manager - Handles level loading and progression
import { Player } from '../entities/player.js';
import { Enemy } from '../entities/enemy.js';
import { Platform, Collectible, Goal } from '../entities/platform.js';
import { level1 } from '../data/level1.js';
import { level2 } from '../data/level2.js';

export class LevelManager {
    constructor(entityManager) {
        this.entityManager = entityManager;
        this.levels = [level1, level2];
        this.currentLevelIndex = 0;
        this.currentLevel = null;
    }

    loadLevel(levelIndex) {
        if (levelIndex < 0 || levelIndex >= this.levels.length) {
            return false;
        }

        this.currentLevelIndex = levelIndex;
        this.currentLevel = this.levels[levelIndex];

        // Clear existing entities
        this.entityManager.clear();

        // Create player
        const player = new Player(
            this.currentLevel.playerStart.x,
            this.currentLevel.playerStart.y
        );
        this.entityManager.setPlayer(player);

        // Create platforms
        for (const platformData of this.currentLevel.platforms) {
            const platform = new Platform(
                platformData.x,
                platformData.y,
                platformData.width,
                platformData.height,
                platformData.type
            );
            this.entityManager.addPlatform(platform);
        }

        // Create enemies
        for (const enemyData of this.currentLevel.enemies) {
            const enemy = new Enemy(
                enemyData.x,
                enemyData.y,
                enemyData.type,
                enemyData.patrolMin,
                enemyData.patrolMax
            );
            this.entityManager.addEnemy(enemy);
        }

        // Create collectibles
        for (const collectibleData of this.currentLevel.collectibles) {
            const collectible = new Collectible(
                collectibleData.x,
                collectibleData.y,
                collectibleData.type,
                collectibleData.value
            );
            this.entityManager.addCollectible(collectible);
        }

        // Create goal
        const goal = new Goal(
            this.currentLevel.goalPosition.x,
            this.currentLevel.goalPosition.y
        );
        this.entityManager.setGoal(goal);

        return true;
    }

    getCurrentLevel() {
        return this.currentLevel;
    }

    hasNextLevel() {
        return this.currentLevelIndex < this.levels.length - 1;
    }

    loadNextLevel() {
        return this.loadLevel(this.currentLevelIndex + 1);
    }

    restartLevel() {
        return this.loadLevel(this.currentLevelIndex);
    }

    getTotalLevels() {
        return this.levels.length;
    }
}
