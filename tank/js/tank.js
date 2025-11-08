class Tank {
    constructor(x, y, direction, speed, type) {
        this.x = x;
        this.y = y;
        this.width = CONFIG.GRID_SIZE;
        this.height = CONFIG.GRID_SIZE;
        this.direction = direction;
        this.speed = speed;
        this.type = type; // 'player' or 'enemy'
        this.bullets = [];
        this.lastShotTime = 0;
        this.shotCooldown = CONFIG.SHOT_COOLDOWN;
        this.active = true;
    }

    move(direction, map) {
        if (!this.active) return;

        const oldX = this.x;
        const oldY = this.y;
        this.direction = direction;

        const vector = Utils.getDirectionVector(direction);
        this.x += vector.x * this.speed;
        this.y += vector.y * this.speed;

        // Boundary check
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        if (this.x > CONFIG.CANVAS_WIDTH - this.width) this.x = CONFIG.CANVAS_WIDTH - this.width;
        if (this.y > CONFIG.CANVAS_HEIGHT - this.height) this.y = CONFIG.CANVAS_HEIGHT - this.height;

        // Obstacle collision check
        if (map && this.checkCollisionWithMap(map)) {
            this.x = oldX;
            this.y = oldY;
        }
    }

    checkCollisionWithMap(map) {
        const tankRect = this.getRect();
        
        // Check collision with obstacles (not grass)
        for (let obstacle of map.obstacles) {
            if (obstacle.destroyed) continue;
            if (obstacle.type === 'grass') continue; // Tanks can pass through grass
            
            const obstacleRect = {
                x: obstacle.x,
                y: obstacle.y,
                width: obstacle.width,
                height: obstacle.height
            };
            
            if (Utils.checkRectCollision(tankRect, obstacleRect)) {
                return true;
            }
        }
        
        // Check collision with base
        if (map.base && !map.base.destroyed) {
            const baseRect = map.getBaseRect();
            if (Utils.checkRectCollision(tankRect, baseRect)) {
                return true;
            }
        }
        
        return false;
    }

    shoot() {
        if (!this.active) return null;

        const currentTime = Date.now();
        if (currentTime - this.lastShotTime < this.shotCooldown) {
            return null;
        }

        this.lastShotTime = currentTime;
        const bulletSpeed = this.type === 'player' ? CONFIG.PLAYER_BULLET_SPEED : CONFIG.ENEMY_BULLET_SPEED;
        return new Bullet(this.x, this.y, this.direction, bulletSpeed, this.type);
    }

    update(map, player) {
        if (!this.active) return;

        // 更新子弹
        this.bullets = this.bullets.filter(bullet => bullet.active);
        this.bullets.forEach(bullet => bullet.update());

        // 敌方坦克AI
        if (this.type === 'enemy') {
            this.updateAI(map, player);
        }
    }

    updateAI(map, player) {
        // 随机改变方向
        if (Math.random() < 0.02) {
            const directions = ['up', 'down', 'left', 'right'];
            this.direction = directions[Utils.random(0, 3)];
        }

        this.move(this.direction, map);

        // 随机射击
        if (Math.random() < 0.02 && player && player.active) {
            const bullet = this.shoot();
            if (bullet) {
                this.bullets.push(bullet);
            }
        }
    }

    draw(ctx) {
        if (!this.active) return;

        // 绘制坦克主体
        ctx.fillStyle = this.type === 'player' ? '#4CAF50' : '#FF5252';
        ctx.fillRect(this.x, this.y, this.width, this.height);

        // 绘制炮管
        ctx.fillStyle = '#333';
        let barrelX = this.x + 13;
        let barrelY = this.y + 13;
        let barrelWidth = 4;
        let barrelHeight = 10;

        switch(this.direction) {
            case 'up':
                barrelY = this.y - 10;
                barrelHeight = 10;
                break;
            case 'down':
                barrelY = this.y + 30;
                barrelHeight = 10;
                break;
            case 'left':
                barrelX = this.x - 10;
                barrelWidth = 10;
                barrelHeight = 4;
                break;
            case 'right':
                barrelX = this.x + 30;
                barrelWidth = 10;
                barrelHeight = 4;
                break;
        }

        ctx.fillRect(barrelX, barrelY, barrelWidth, barrelHeight);

        // 绘制子弹
        this.bullets.forEach(bullet => bullet.draw(ctx));
    }

    getRect() {
        return {
            x: this.x,
            y: this.y,
            width: this.width,
            height: this.height
        };
    }
}

class PlayerTank extends Tank {
    constructor(x, y) {
        super(x, y, 'up', CONFIG.PLAYER_SPEED, 'player');
        this.lives = CONFIG.PLAYER_INITIAL_LIVES;
    }

    takeDamage() {
        this.lives--;
        if (this.lives <= 0) {
            this.active = false;
        }
        return this.lives;
    }
}

class EnemyTank extends Tank {
    constructor(x, y) {
        super(x, y, 'down', CONFIG.ENEMY_SPEED, 'enemy');
        const directions = ['up', 'down', 'left', 'right'];
        this.direction = directions[Utils.random(0, 3)];
    }

    takeDamage() {
        this.active = false;
        return true; // 被摧毁
    }
}