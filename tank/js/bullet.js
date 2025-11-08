class Bullet {
    constructor(x, y, direction, speed, owner) {
        this.x = x;
        this.y = y;
        this.width = 4;
        this.height = 4;
        this.direction = direction;
        this.speed = speed;
        this.owner = owner; // 'player' or 'enemy'
        this.active = true;
        
        // 根据方向调整子弹位置
        this.adjustPosition();
    }

    adjustPosition() {
        // 根据坦克方向调整子弹起始位置，使其从炮管射出
        switch(this.direction) {
            case 'up':
                this.x += 13; // 坦克宽度30，子弹宽度4，居中：(30-4)/2=13
                this.y -= 2;
                break;
            case 'down':
                this.x += 13;
                this.y += 32; // 坦克高度30 + 2
                break;
            case 'left':
                this.x -= 2;
                this.y += 13;
                break;
            case 'right':
                this.x += 32;
                this.y += 13;
                break;
        }
    }

    update() {
        if (!this.active) return;

        const vector = Utils.getDirectionVector(this.direction);
        this.x += vector.x * this.speed;
        this.y += vector.y * this.speed;

        // Check boundary
        if (this.x < 0 || this.x > CONFIG.CANVAS_WIDTH || this.y < 0 || this.y > CONFIG.CANVAS_HEIGHT) {
            this.active = false;
        }
    }

    draw(ctx) {
        if (!this.active) return;

        ctx.fillStyle = this.owner === 'player' ? '#4CAF50' : '#FF5252';
        ctx.fillRect(this.x, this.y, this.width, this.height);
        
        // 添加发光效果
        ctx.shadowColor = this.owner === 'player' ? '#4CAF50' : '#FF5252';
        ctx.shadowBlur = 10;
        ctx.fillRect(this.x, this.y, this.width, this.height);
        ctx.shadowBlur = 0;
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