class Game {
    constructor() {
        this.canvas = document.getElementById('gameCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.currentLevel = 1;
        this.map = new Map(this.currentLevel);
        this.player = new PlayerTank(CONFIG.PLAYER_INITIAL_X, CONFIG.PLAYER_INITIAL_Y);
        this.enemyTanks = [];
        this.score = 0;
        this.gameState = 'intro'; // 'intro', 'playing', 'paused', 'gameOver'
        this.keys = {};
        
        // Enemy spawning management
        this.totalEnemies = 0;
        this.enemiesSpawned = 0;
        this.lastSpawnTime = 0;
        this.nextSpawnPositionIndex = 0;
        
        this.setupIntro();
        this.init();
        this.setupEventListeners();
    }

    setupIntro() {
        const introScreen = document.getElementById('introScreen');
        const introStart = document.getElementById('introStart');
        
        const startGame = () => {
            introScreen.classList.add('fade-out');
            setTimeout(() => {
                introScreen.style.display = 'none';
                this.gameState = 'playing';
                this.gameLoop();
            }, CONFIG.INTRO_FADE_OUT);
        };
        
        // Click to start
        introStart.addEventListener('click', startGame);
        
        // Space or Enter to start
        document.addEventListener('keydown', (e) => {
            if (this.gameState === 'intro' && (e.code === 'Space' || e.code === 'Enter')) {
                e.preventDefault();
                startGame();
            }
        }, { once: true });
    }

    init() {
        const levelData = LEVELS[this.currentLevel];
        this.totalEnemies = levelData.enemyCount;
        this.enemiesSpawned = 0;
        this.lastSpawnTime = 0;
        this.nextSpawnPositionIndex = 0;
        this.updateUI();
    }

    setupEventListeners() {
        // Keyboard events
        document.addEventListener('keydown', (e) => {
            // Ignore inputs during intro
            if (this.gameState === 'intro') return;
            
            this.keys[e.code] = true;
            
            if (e.code === 'Space') {
                e.preventDefault();
                const bullet = this.player.shoot();
                if (bullet) {
                    this.player.bullets.push(bullet);
                }
            }
            
            if (e.code === 'KeyP') {
                this.togglePause();
            }
        });

        document.addEventListener('keyup', (e) => {
            // Ignore inputs during intro
            if (this.gameState === 'intro') return;
            
            this.keys[e.code] = false;
        });

        // 按钮事件
        document.getElementById('pauseBtn').addEventListener('click', () => {
            this.togglePause();
        });

        document.getElementById('restartBtn').addEventListener('click', () => {
            this.restart();
        });
    }

    spawnEnemyTank() {
        // Check if we still have enemies to spawn
        if (this.enemiesSpawned >= this.totalEnemies) {
            return false;
        }
        
        // Try to find a valid spawn position
        const spawnPositions = CONFIG.ENEMY_SPAWN_POSITIONS_X;
        let attempts = 0;
        let spawnX, spawnY;
        let validPosition = false;
        
        while (attempts < spawnPositions.length && !validPosition) {
            // Use next position in sequence
            spawnX = spawnPositions[this.nextSpawnPositionIndex];
            spawnY = CONFIG.ENEMY_SPAWN_Y;
            
            // Check if position conflicts with map obstacles
            validPosition = this.isValidSpawnPosition(spawnX, spawnY);
            
            // Move to next position for next spawn
            this.nextSpawnPositionIndex = (this.nextSpawnPositionIndex + 1) % spawnPositions.length;
            attempts++;
            
            if (!validPosition) {
                // Try next position
                continue;
            }
        }
        
        if (validPosition) {
            const enemy = new EnemyTank(spawnX, spawnY);
            this.enemyTanks.push(enemy);
            this.enemiesSpawned++;
            this.updateUI();
            return true;
        }
        
        return false;
    }
    
    isValidSpawnPosition(x, y) {
        const tankRect = {
            x: x,
            y: y,
            width: CONFIG.GRID_SIZE,
            height: CONFIG.GRID_SIZE
        };
        
        // Check collision with obstacles
        for (let obstacle of this.map.obstacles) {
            if (obstacle.destroyed) continue;
            if (obstacle.type === 'grass') continue; // Can spawn on grass
            
            const obstacleRect = {
                x: obstacle.x,
                y: obstacle.y,
                width: obstacle.width,
                height: obstacle.height
            };
            
            if (Utils.checkRectCollision(tankRect, obstacleRect)) {
                return false;
            }
        }
        
        // Check collision with existing enemy tanks
        for (let enemy of this.enemyTanks) {
            if (!enemy.active) continue;
            
            if (Utils.checkRectCollision(tankRect, enemy.getRect())) {
                return false;
            }
        }
        
        // Check collision with player
        if (this.player.active && Utils.checkRectCollision(tankRect, this.player.getRect())) {
            return false;
        }
        
        return true;
    }

    processInput() {
        if (this.gameState !== 'playing') return;

        if (this.keys['ArrowUp']) {
            this.player.move('up', this.map, this);
        } else if (this.keys['ArrowDown']) {
            this.player.move('down', this.map, this);
        } else if (this.keys['ArrowLeft']) {
            this.player.move('left', this.map, this);
        } else if (this.keys['ArrowRight']) {
            this.player.move('right', this.map, this);
        }
    }

    update() {
        if (this.gameState !== 'playing') return;
        
        // Spawn enemies at intervals
        const currentTime = Date.now();
        if (this.enemiesSpawned < this.totalEnemies && 
            currentTime - this.lastSpawnTime >= CONFIG.ENEMY_SPAWN_INTERVAL) {
            if (this.spawnEnemyTank()) {
                this.lastSpawnTime = currentTime;
            }
        }

        // Update player
        this.player.update(this.map, null, this);

        // Update enemy tanks
        this.enemyTanks.forEach(tank => tank.update(this.map, this.player, this));
        this.enemyTanks = this.enemyTanks.filter(tank => tank.active);

        // Check collisions
        CollisionDetector.checkAllCollisions(this);

        // Check game end conditions
        this.checkGameEnd();
    }

    checkGameEnd() {
        // Check victory condition - all enemies spawned and destroyed
        if (this.enemiesSpawned >= this.totalEnemies && this.enemyTanks.length === 0) {
            // Check if there's a next level
            if (LEVELS[this.currentLevel + 1]) {
                this.nextLevel();
            } else {
                this.gameOver(true);
            }
            return;
        }

        // Check failure condition
        if (!this.player.active || this.map.base.destroyed) {
            this.gameOver(false);
            return;
        }
    }

    nextLevel() {
        this.currentLevel++;
        this.gameState = 'paused';
        
        // Validate next level exists
        if (!LEVELS[this.currentLevel]) {
            console.error(`Level ${this.currentLevel} does not exist!`);
            this.gameOver(true); // Victory - completed all levels
            return;
        }
        
        // Show level transition
        const gameOverElement = document.getElementById('gameOver');
        const gameResultElement = document.getElementById('gameResult');
        
        const levelData = LEVELS[this.currentLevel];
        gameResultElement.textContent = `Level ${this.currentLevel}: ${levelData.name}`;
        gameResultElement.style.color = '#4CAF50';
        
        document.getElementById('restartBtn').textContent = 'Start Level';
        gameOverElement.classList.remove('hidden');
        
        // Prepare next level
        setTimeout(() => {
            this.loadLevel(this.currentLevel);
            gameOverElement.classList.add('hidden');
            document.getElementById('restartBtn').textContent = '重新开始';
            this.gameState = 'playing';
        }, CONFIG.LEVEL_TRANSITION_DELAY);
    }

    loadLevel(levelNumber) {
        this.map = new Map(levelNumber);
        
        // Preserve player lives when advancing levels
        const currentLives = this.player ? this.player.lives : CONFIG.PLAYER_INITIAL_LIVES;
        this.player = new PlayerTank(CONFIG.PLAYER_INITIAL_X, CONFIG.PLAYER_INITIAL_Y);
        
        // Only reset lives if starting from level 1 or player had died
        if (levelNumber === 1 || currentLives <= 0) {
            this.player.lives = CONFIG.PLAYER_INITIAL_LIVES;
        } else {
            this.player.lives = currentLives; // Preserve lives from previous level
        }
        
        this.enemyTanks = [];
        
        const levelData = LEVELS[levelNumber];
        this.totalEnemies = levelData.enemyCount;
        this.enemiesSpawned = 0;
        this.lastSpawnTime = Date.now(); // Start spawn timer immediately
        this.nextSpawnPositionIndex = 0;
        this.updateUI();
    }

    render() {
        // 清空画布
        this.ctx.fillStyle = '#2a2a2a';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // 绘制地图
        this.map.draw(this.ctx);

        // 绘制玩家
        this.player.draw(this.ctx);

        // 绘制敌方坦克
        this.enemyTanks.forEach(tank => tank.draw(this.ctx));

        // 绘制游戏状态
        if (this.gameState === 'paused') {
            this.drawPauseScreen();
        }
    }

    drawPauseScreen() {
        this.ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        this.ctx.fillStyle = 'white';
        this.ctx.font = '36px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('游戏暂停', this.canvas.width / 2, this.canvas.height / 2);
        this.ctx.font = '18px Arial';
        this.ctx.fillText('按 P 键继续', this.canvas.width / 2, this.canvas.height / 2 + 40);
    }

    gameLoop() {
        // Only run game loop if not in intro
        if (this.gameState !== 'intro') {
            this.processInput();
            this.update();
            this.render();
        }
        
        requestAnimationFrame(() => this.gameLoop());
    }

    togglePause() {
        if (this.gameState === 'playing') {
            this.gameState = 'paused';
            document.getElementById('pauseBtn').textContent = '继续';
        } else if (this.gameState === 'paused') {
            this.gameState = 'playing';
            document.getElementById('pauseBtn').textContent = '暂停';
        }
    }

    gameOver(isVictory) {
        this.gameState = 'gameOver';
        
        const gameOverElement = document.getElementById('gameOver');
        const gameResultElement = document.getElementById('gameResult');
        
        gameResultElement.textContent = isVictory ? '胜利!' : '游戏结束';
        gameResultElement.style.color = isVictory ? '#4CAF50' : '#FF5252';
        
        gameOverElement.classList.remove('hidden');
    }

    restart() {
        this.currentLevel = 1;
        
        this.loadLevel(this.currentLevel);
        this.score = 0;
        this.gameState = 'playing';
        
        document.getElementById('gameOver').classList.add('hidden');
        document.getElementById('pauseBtn').textContent = '暂停';
        
        this.updateUI();
    }

    updateUI() {
        document.getElementById('lives').textContent = this.player.lives;
        // Show total enemies remaining (spawned + to spawn)
        const remainingEnemies = (this.totalEnemies - this.enemiesSpawned) + this.enemyTanks.length;
        document.getElementById('enemies').textContent = remainingEnemies;
        document.getElementById('score').textContent = this.score;
        
        // Update level display if exists
        const levelDisplay = document.getElementById('level');
        if (levelDisplay) {
            const levelData = LEVELS[this.currentLevel];
            levelDisplay.textContent = `${this.currentLevel} - ${levelData.name}`;
        }
    }
}

// 启动游戏
window.addEventListener('load', () => {
    new Game();
});