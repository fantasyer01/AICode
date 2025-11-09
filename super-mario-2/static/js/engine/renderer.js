// Renderer - Handles all canvas drawing operations
export class Renderer {
    constructor(canvas, entityManager) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.entityManager = entityManager;
        
        // Camera
        this.camera = {
            x: 0,
            y: 0,
            width: canvas.width,
            height: canvas.height
        };

        // Set canvas size
        this.resize();
    }

    resize() {
        this.canvas.width = 800;
        this.canvas.height = 600;
        this.camera.width = this.canvas.width;
        this.camera.height = this.canvas.height;
    }

    updateCamera(level) {
        const player = this.entityManager.player;
        if (!player) return;

        // Center camera on player horizontally with some lag
        const targetX = player.position.x - this.camera.width / 2 + player.size.width / 2;
        this.camera.x += (targetX - this.camera.x) * 0.1;

        // Clamp camera to level bounds
        this.camera.x = Math.max(0, Math.min(this.camera.x, level.width - this.camera.width));
        this.camera.y = Math.max(0, Math.min(this.camera.y, level.height - this.camera.height));
    }

    render(level) {
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw background
        this.ctx.fillStyle = level.background;
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw platforms
        this.drawPlatforms();

        // Draw collectibles
        this.drawCollectibles();

        // Draw goal
        this.drawGoal();

        // Draw enemies
        this.drawEnemies();

        // Draw player
        this.drawPlayer();
    }

    drawPlatforms() {
        for (const platform of this.entityManager.platforms) {
            const x = platform.position.x - this.camera.x;
            const y = platform.position.y - this.camera.y;

            // Only draw if visible
            if (x + platform.size.width < 0 || x > this.camera.width) continue;

            this.ctx.fillStyle = '#8B4513';
            this.ctx.fillRect(x, y, platform.size.width, platform.size.height);

            // Draw brick pattern
            this.ctx.strokeStyle = '#654321';
            this.ctx.lineWidth = 2;
            const brickWidth = 32;
            const brickHeight = 16;

            for (let bx = 0; bx < platform.size.width; bx += brickWidth) {
                for (let by = 0; by < platform.size.height; by += brickHeight) {
                    this.ctx.strokeRect(x + bx, y + by, brickWidth, brickHeight);
                }
            }
        }
    }

    drawPlayer() {
        const player = this.entityManager.player;
        if (!player || !player.isAlive) return;

        const x = player.position.x - this.camera.x;
        const y = player.position.y - this.camera.y;

        // Flicker effect when invincible
        if (player.invincible && Math.floor(player.invincibleTimer / 10) % 2 === 0) {
            this.ctx.globalAlpha = 0.5;
        }

        // Draw player as a simple colored rectangle
        this.ctx.fillStyle = '#FF0000';
        this.ctx.fillRect(x, y, player.size.width, player.size.height);

        // Draw eyes based on direction
        this.ctx.fillStyle = '#FFFFFF';
        const eyeSize = 6;
        const eyeY = y + 10;

        if (player.direction === 'right') {
            this.ctx.fillRect(x + 20, eyeY, eyeSize, eyeSize);
        } else {
            this.ctx.fillRect(x + 6, eyeY, eyeSize, eyeSize);
        }

        this.ctx.globalAlpha = 1.0;
    }

    drawEnemies() {
        for (const enemy of this.entityManager.enemies) {
            if (!enemy.isAlive) continue;

            const x = enemy.position.x - this.camera.x;
            const y = enemy.position.y - this.camera.y;

            // Only draw if visible
            if (x + enemy.size.width < 0 || x > this.camera.width) continue;

            // Draw enemy
            this.ctx.fillStyle = '#8B008B';
            this.ctx.fillRect(x, y, enemy.size.width, enemy.size.height);

            // Draw eyes
            this.ctx.fillStyle = '#FFFFFF';
            const eyeSize = 4;
            const eyeY = y + 8;

            if (enemy.direction === 'right') {
                this.ctx.fillRect(x + 20, eyeY, eyeSize, eyeSize);
            } else {
                this.ctx.fillRect(x + 8, eyeY, eyeSize, eyeSize);
            }
        }
    }

    drawCollectibles() {
        for (const collectible of this.entityManager.collectibles) {
            if (collectible.isCollected) continue;

            const x = collectible.position.x - this.camera.x;
            const y = collectible.position.y - this.camera.y;

            // Only draw if visible
            if (x + collectible.size.width < 0 || x > this.camera.width) continue;

            // Draw coin with animation
            const offset = Math.sin(collectible.animationFrame) * 3;

            this.ctx.fillStyle = '#FFD700';
            this.ctx.beginPath();
            this.ctx.arc(
                x + collectible.size.width / 2,
                y + collectible.size.height / 2 + offset,
                collectible.size.width / 2,
                0,
                Math.PI * 2
            );
            this.ctx.fill();

            // Draw inner circle
            this.ctx.fillStyle = '#FFA500';
            this.ctx.beginPath();
            this.ctx.arc(
                x + collectible.size.width / 2,
                y + collectible.size.height / 2 + offset,
                collectible.size.width / 4,
                0,
                Math.PI * 2
            );
            this.ctx.fill();
        }
    }

    drawGoal() {
        const goal = this.entityManager.goal;
        if (!goal) return;

        const x = goal.position.x - this.camera.x;
        const y = goal.position.y - this.camera.y;

        // Draw flag pole
        this.ctx.fillStyle = '#000000';
        this.ctx.fillRect(x + 14, y, 4, goal.size.height);

        // Draw flag
        this.ctx.fillStyle = goal.reached ? '#00FF00' : '#FF0000';
        this.ctx.beginPath();
        this.ctx.moveTo(x + 18, y + 5);
        this.ctx.lineTo(x + 18 + 20, y + 15);
        this.ctx.lineTo(x + 18, y + 25);
        this.ctx.closePath();
        this.ctx.fill();
    }

    worldToScreen(worldX, worldY) {
        return {
            x: worldX - this.camera.x,
            y: worldY - this.camera.y
        };
    }

    screenToWorld(screenX, screenY) {
        return {
            x: screenX + this.camera.x,
            y: screenY + this.camera.y
        };
    }
}
