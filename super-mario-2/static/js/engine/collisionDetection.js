// Collision Detection System
export class CollisionDetection {
    constructor(entityManager) {
        this.entityManager = entityManager;
    }

    checkCollisions() {
        const player = this.entityManager.player;
        if (!player || !player.isAlive) return;

        // Reset grounded state
        player.isGrounded = false;

        // Check platform collisions
        this.checkPlatformCollisions(player);

        // Check enemy collisions
        this.checkEnemyCollisions(player);

        // Check collectible collisions
        this.checkCollectibleCollisions(player);

        // Check goal collision
        this.checkGoalCollision(player);

        // Check enemy-platform collisions
        this.checkEnemyPlatformCollisions();

        // Check enemy-enemy collisions
        this.checkEnemyEnemyCollisions();
    }

    checkPlatformCollisions(player) {
        const playerBounds = player.getBounds();

        for (const platform of this.entityManager.platforms) {
            if (!platform.isCollidable) continue;

            const platformBounds = platform.getBounds();

            if (this.isColliding(playerBounds, platformBounds)) {
                this.resolvePlatformCollision(player, platform);
            }
        }
    }

    resolvePlatformCollision(player, platform) {
        const playerBounds = player.getBounds();
        const platformBounds = platform.getBounds();

        // Calculate overlap on each axis
        const overlapLeft = playerBounds.right - platformBounds.left;
        const overlapRight = platformBounds.right - playerBounds.left;
        const overlapTop = playerBounds.bottom - platformBounds.top;
        const overlapBottom = platformBounds.bottom - playerBounds.top;

        // Find minimum overlap
        const minOverlap = Math.min(overlapLeft, overlapRight, overlapTop, overlapBottom);

        // Resolve collision based on smallest overlap
        if (minOverlap === overlapTop && player.velocity.y > 0) {
            // Landing on top of platform
            player.position.y = platformBounds.top - player.size.height;
            player.velocity.y = 0;
            player.isGrounded = true;
        } else if (minOverlap === overlapBottom && player.velocity.y < 0) {
            // Hitting head on bottom of platform
            player.position.y = platformBounds.bottom;
            player.velocity.y = 0;
        } else if (minOverlap === overlapLeft) {
            // Hitting left side of platform
            player.position.x = platformBounds.left - player.size.width;
            player.velocity.x = 0;
        } else if (minOverlap === overlapRight) {
            // Hitting right side of platform
            player.position.x = platformBounds.right;
            player.velocity.x = 0;
        }
    }

    checkEnemyCollisions(player) {
        const playerBounds = player.getBounds();

        for (const enemy of this.entityManager.enemies) {
            if (!enemy.isAlive) continue;

            const enemyBounds = enemy.getBounds();

            if (this.isColliding(playerBounds, enemyBounds)) {
                // Check if player is stomping enemy (landing on top)
                if (player.velocity.y > 0 && 
                    playerBounds.bottom - enemy.velocity.y <= enemyBounds.top + 10) {
                    // Player stomps enemy
                    enemy.defeat();
                    player.bounceOnEnemy();
                    return { type: 'enemyDefeated', enemy };
                } else {
                    // Player takes damage
                    if (!player.invincible) {
                        player.takeDamage();
                        return { type: 'playerHit' };
                    }
                }
            }
        }
    }

    checkCollectibleCollisions(player) {
        const playerBounds = player.getBounds();
        let scoreGained = 0;

        for (const collectible of this.entityManager.collectibles) {
            if (collectible.isCollected) continue;

            const collectibleBounds = collectible.getBounds();

            if (this.isColliding(playerBounds, collectibleBounds)) {
                collectible.collect();
                scoreGained += collectible.value;
            }
        }

        return scoreGained;
    }

    checkGoalCollision(player) {
        if (!this.entityManager.goal) return false;

        const playerBounds = player.getBounds();
        const goalBounds = this.entityManager.goal.getBounds();

        if (this.isColliding(playerBounds, goalBounds)) {
            if (!this.entityManager.goal.reached) {
                this.entityManager.goal.reach();
                return true;
            }
        }

        return false;
    }

    checkEnemyPlatformCollisions() {
        for (const enemy of this.entityManager.enemies) {
            if (!enemy.isAlive) continue;

            enemy.velocity.y += 0.6; // Apply gravity
            let isGrounded = false;

            const enemyBounds = enemy.getBounds();

            for (const platform of this.entityManager.platforms) {
                if (!platform.isCollidable) continue;

                const platformBounds = platform.getBounds();

                if (this.isColliding(enemyBounds, platformBounds)) {
                    // Only resolve vertical collisions for enemies
                    if (enemy.velocity.y > 0 && 
                        enemyBounds.bottom - enemy.velocity.y <= platformBounds.top + 10) {
                        enemy.position.y = platformBounds.top - enemy.size.height;
                        enemy.velocity.y = 0;
                        isGrounded = true;
                    }
                }
            }

            // If enemy is walker and about to walk off edge, reverse direction
            if (enemy.type === 'walker' && isGrounded) {
                const futureX = enemy.position.x + enemy.velocity.x * 10;
                const futureGrounded = this.checkGroundAhead(
                    futureX, 
                    enemy.position.y + enemy.size.height + 10,
                    enemy.size.width
                );

                if (!futureGrounded) {
                    enemy.reverseDirection();
                }
            }
        }
    }

    checkGroundAhead(x, y, width) {
        for (const platform of this.entityManager.platforms) {
            const platformBounds = platform.getBounds();
            if (x + width > platformBounds.left && 
                x < platformBounds.right &&
                Math.abs(y - platformBounds.top) < 20) {
                return true;
            }
        }
        return false;
    }

    checkEnemyEnemyCollisions() {
        const enemies = this.entityManager.getAliveEnemies();

        for (let i = 0; i < enemies.length; i++) {
            for (let j = i + 1; j < enemies.length; j++) {
                const enemy1 = enemies[i];
                const enemy2 = enemies[j];

                if (this.isColliding(enemy1.getBounds(), enemy2.getBounds())) {
                    // Enemies bounce off each other
                    if (enemy1.type === 'walker') enemy1.reverseDirection();
                    if (enemy2.type === 'walker') enemy2.reverseDirection();
                }
            }
        }
    }

    isColliding(bounds1, bounds2) {
        return bounds1.left < bounds2.right &&
               bounds1.right > bounds2.left &&
               bounds1.top < bounds2.bottom &&
               bounds1.bottom > bounds2.top;
    }

    checkOutOfBounds(player, levelHeight) {
        // Check if player fell below the level
        if (player.position.y > levelHeight) {
            return true;
        }
        return false;
    }
}
