class Map {
    constructor(levelNumber = 1) {
        this.gridSize = CONFIG.GRID_SIZE;
        this.obstacles = [];
        this.base = null;
        this.baseProtection = []; // Store base protection bricks separately
        this.levelNumber = levelNumber;
        this.levelData = LEVELS[levelNumber] || LEVELS[1];
        this.generateMapFromArray();
    }

    generateMapFromArray() {
        // Clear obstacles
        this.obstacles = [];
        this.baseProtection = [];
        
        const mapData = this.levelData.map;
        const rows = mapData.length;
        const cols = mapData[0].length;
        
        // Parse map data and create obstacles
        for (let row = 0; row < rows; row++) {
            for (let col = 0; col < cols; col++) {
                const tileType = mapData[row][col];
                const x = col * this.gridSize;
                const y = row * this.gridSize;
                
                switch(tileType) {
                    case TILE_TYPES.BRICK:
                        this.obstacles.push({
                            x: x,
                            y: y,
                            width: this.gridSize,
                            height: this.gridSize,
                            type: 'brick',
                            destroyed: false
                        });
                        break;
                    
                    case TILE_TYPES.STEEL:
                        this.obstacles.push({
                            x: x,
                            y: y,
                            width: this.gridSize,
                            height: this.gridSize,
                            type: 'steel',
                            destroyed: false
                        });
                        break;
                    
                    case TILE_TYPES.WATER:
                        this.obstacles.push({
                            x: x,
                            y: y,
                            width: this.gridSize,
                            height: this.gridSize,
                            type: 'water',
                            destroyed: false
                        });
                        break;
                    
                    case TILE_TYPES.GRASS:
                        this.obstacles.push({
                            x: x,
                            y: y,
                            width: this.gridSize,
                            height: this.gridSize,
                            type: 'grass',
                            destroyed: false
                        });
                        break;
                    
                    case TILE_TYPES.BASE:
                        // Base is always centered at bottom
                        break;
                    
                    case TILE_TYPES.EMPTY:
                    default:
                        // Empty space, do nothing
                        break;
                }
            }
        }
        
        // Always create base at center bottom
        const centerCol = Math.floor(cols / 2);
        const bottomRow = rows - 2; // Second from bottom
        
        this.base = {
            x: centerCol * this.gridSize,
            y: bottomRow * this.gridSize,
            width: this.gridSize,
            height: this.gridSize,
            destroyed: false
        };
        
        // Create brick protection around base (8 bricks surrounding it)
        const protectionPositions = [
            { dx: -1, dy: -1 }, { dx: 0, dy: -1 }, { dx: 1, dy: -1 }, // Top row
            { dx: -1, dy: 0 },                     { dx: 1, dy: 0 }   // Middle row (left and right)
        ];
        
        protectionPositions.forEach(pos => {
            const brick = {
                x: this.base.x + (pos.dx * this.gridSize),
                y: this.base.y + (pos.dy * this.gridSize),
                width: this.gridSize,
                height: this.gridSize,
                type: 'brick',
                destroyed: false,
                isBaseProtection: true
            };
            this.baseProtection.push(brick);
            this.obstacles.push(brick); // Also add to obstacles for collision detection
        });
    }

    draw(ctx) {
        // Draw obstacles
        this.obstacles.forEach(obstacle => {
            if (!obstacle.destroyed) {
                switch(obstacle.type) {
                    case 'brick':
                        // Special color for base protection bricks
                        if (obstacle.isBaseProtection) {
                            ctx.fillStyle = '#8B4513'; // Peru color for base protection
                        } else {
                            ctx.fillStyle = '#8B4513'; // Brown brick
                        }
                        break;
                    case 'steel':
                        ctx.fillStyle = '#666'; // Gray steel
                        break;
                    case 'water':
                        ctx.fillStyle = '#1E90FF'; // Blue water
                        break;
                    case 'grass':
                        ctx.fillStyle = '#228B22'; // Green grass
                        break;
                    default:
                        ctx.fillStyle = '#666';
                }
                
                ctx.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
                
                // Add texture
                if (obstacle.type === 'brick') {
                    ctx.strokeStyle = obstacle.isBaseProtection ? '#D2691E' : '#A52A2A';
                } else if (obstacle.type === 'steel') {
                    ctx.strokeStyle = '#888';
                } else if (obstacle.type === 'water') {
                    ctx.strokeStyle = '#4169E1';
                } else if (obstacle.type === 'grass') {
                    ctx.strokeStyle = '#32CD32';
                }
                ctx.lineWidth = 1;
                ctx.strokeRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            }
        });

        // Draw base
        if (this.base && !this.base.destroyed) {
            ctx.fillStyle = '#FFD700'; // Gold base
            ctx.fillRect(this.base.x, this.base.y, this.base.width, this.base.height);
            
            // Base decoration - eagle emblem
            ctx.fillStyle = '#FF6B6B';
            ctx.beginPath();
            ctx.arc(this.base.x + 15, this.base.y + 15, 8, 0, Math.PI * 2);
            ctx.fill();
            
            // Add base border
            ctx.strokeStyle = '#FFA500';
            ctx.lineWidth = 2;
            ctx.strokeRect(this.base.x, this.base.y, this.base.width, this.base.height);
        } else if (this.base && this.base.destroyed) {
            // Draw destroyed base
            ctx.fillStyle = '#333';
            ctx.fillRect(this.base.x, this.base.y, this.base.width, this.base.height);
            
            // Draw X mark
            ctx.strokeStyle = '#FF0000';
            ctx.lineWidth = 3;
            ctx.beginPath();
            ctx.moveTo(this.base.x + 5, this.base.y + 5);
            ctx.lineTo(this.base.x + 25, this.base.y + 25);
            ctx.moveTo(this.base.x + 25, this.base.y + 5);
            ctx.lineTo(this.base.x + 5, this.base.y + 25);
            ctx.stroke();
        }
    }

    getBaseRect() {
        return {
            x: this.base.x,
            y: this.base.y,
            width: this.base.width,
            height: this.base.height
        };
    }

    checkBulletCollision(bullet) {
        const bulletRect = bullet.getRect();
        
        for (let obstacle of this.obstacles) {
            if (obstacle.destroyed) continue;
            if (obstacle.type === 'grass') continue; // Bullets pass through grass
            
            const obstacleRect = {
                x: obstacle.x,
                y: obstacle.y,
                width: obstacle.width,
                height: obstacle.height
            };
            
            if (Utils.checkRectCollision(bulletRect, obstacleRect)) {
                // Only brick and water can be destroyed by bullets
                if (obstacle.type === 'brick') {
                    obstacle.destroyed = true;
                }
                // Bullets are destroyed by steel and water
                if (obstacle.type === 'steel' || obstacle.type === 'water') {
                    return true;
                }
                return true;
            }
        }
        
        // Check collision with base
        if (this.base && !this.base.destroyed) {
            const baseRect = this.getBaseRect();
            if (Utils.checkRectCollision(bulletRect, baseRect)) {
                this.base.destroyed = true;
                return true;
            }
        }
        
        return false;
    }
}