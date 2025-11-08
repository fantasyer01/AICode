class CollisionDetector {
    static checkAllCollisions(game) {
        this.checkBulletTankCollisions(game);
        this.checkBulletMapCollisions(game);
        this.checkTankTankCollisions(game);
    }

    static checkBulletTankCollisions(game) {
        // 检查所有子弹与坦克的碰撞
        const allBullets = [
            ...game.player.bullets,
            ...game.enemyTanks.flatMap(tank => tank.bullets)
        ];

        for (let bullet of allBullets) {
            if (!bullet.active) continue;

            const bulletRect = bullet.getRect();

            // 玩家子弹与敌方坦克碰撞
            if (bullet.owner === 'player') {
                for (let enemy of game.enemyTanks) {
                    if (!enemy.active) continue;
                    
                    if (Utils.checkRectCollision(bulletRect, enemy.getRect())) {
                        bullet.active = false;
                        enemy.takeDamage();
                        game.score += 100;
                        game.updateUI();
                        break;
                    }
                }
            }
            // 敌方子弹与玩家坦克碰撞
            else if (bullet.owner === 'enemy' && game.player.active) {
                if (Utils.checkRectCollision(bulletRect, game.player.getRect())) {
                    bullet.active = false;
                    const remainingLives = game.player.takeDamage();
                    game.updateUI();
                    
                    if (remainingLives <= 0) {
                        game.gameOver(false);
                    }
                }
            }
        }
    }

    static checkBulletMapCollisions(game) {
        // 检查所有子弹与地图的碰撞
        const allBullets = [
            ...game.player.bullets,
            ...game.enemyTanks.flatMap(tank => tank.bullets)
        ];

        for (let bullet of allBullets) {
            if (!bullet.active) continue;

            if (game.map.checkBulletCollision(bullet)) {
                bullet.active = false;
                
                // 检查基地是否被摧毁
                if (game.map.base.destroyed) {
                    game.gameOver(false);
                }
            }
        }
    }

    static checkTankTankCollisions(game) {
        // 检查坦克之间的碰撞（简单实现）
        if (!game.player.active) return;

        const playerRect = game.player.getRect();

        for (let enemy of game.enemyTanks) {
            if (!enemy.active) continue;

            if (Utils.checkRectCollision(playerRect, enemy.getRect())) {
                // 简单的碰撞响应：将敌方坦克推开
                const dx = enemy.x - playerRect.x;
                const dy = enemy.y - playerRect.y;
                
                if (Math.abs(dx) > Math.abs(dy)) {
                    enemy.x += dx > 0 ? 2 : -2;
                } else {
                    enemy.y += dy > 0 ? 2 : -2;
                }
            }
        }
    }
}