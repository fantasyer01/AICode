// Entity Manager - Manages all game entities
export class EntityManager {
    constructor() {
        this.player = null;
        this.platforms = [];
        this.enemies = [];
        this.collectibles = [];
        this.goal = null;
    }

    setPlayer(player) {
        this.player = player;
    }

    addPlatform(platform) {
        this.platforms.push(platform);
    }

    addEnemy(enemy) {
        this.enemies.push(enemy);
    }

    addCollectible(collectible) {
        this.collectibles.push(collectible);
    }

    setGoal(goal) {
        this.goal = goal;
    }

    update(inputHandler, deltaTime) {
        // Update player
        if (this.player) {
            this.player.update(inputHandler, deltaTime);
        }

        // Update enemies
        for (const enemy of this.enemies) {
            if (enemy.isAlive) {
                enemy.update();
            }
        }

        // Update collectibles
        for (const collectible of this.collectibles) {
            if (!collectible.isCollected) {
                collectible.update();
            }
        }

        // Update goal
        if (this.goal) {
            this.goal.update();
        }
    }

    clear() {
        this.player = null;
        this.platforms = [];
        this.enemies = [];
        this.collectibles = [];
        this.goal = null;
    }

    getAliveEnemies() {
        return this.enemies.filter(enemy => enemy.isAlive);
    }

    getUncollectedCollectibles() {
        return this.collectibles.filter(c => !c.isCollected);
    }
}
